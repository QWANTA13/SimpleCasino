package ru.simplecasino.blackjack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.simplecasino.registry.ModBlockEntities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Multiplayer blackjack: up to 4 players, winner takes the pot.
 * Bet = main-hand stack when joining.
 */
public class BlackjackTableBlockEntity extends BlockEntity {
    public static final int MAX_PLAYERS = 4;

    public enum Phase {
        WAITING,
        PLAYING,
        FINISHED
    }

    public static final class Seat {
        public final UUID id;
        public final String name;
        public final Item betItem;
        public final int betCount;
        public final List<Integer> hand = new ArrayList<>();
        public boolean stood;
        public boolean bust;

        public Seat(UUID id, String name, Item betItem, int betCount) {
            this.id = id;
            this.name = name;
            this.betItem = betItem;
            this.betCount = betCount;
        }
    }

    private Phase phase = Phase.WAITING;
    private final List<Seat> seats = new ArrayList<>();
    private final List<Integer> deck = new ArrayList<>();
    private int turnIndex = 0;
    private final Random random = new Random();
    private String lastResult = "";

    public BlackjackTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLACKJACK_TABLE.get(), pos, state);
    }

    public Phase getPhase() {
        return phase;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public int getTurnIndex() {
        return turnIndex;
    }

    public String getLastResult() {
        return lastResult;
    }

    public Seat getTurnSeat() {
        if (phase != Phase.PLAYING || seats.isEmpty()) {
            return null;
        }
        if (turnIndex < 0 || turnIndex >= seats.size()) {
            return null;
        }
        return seats.get(turnIndex);
    }

    public boolean isPlayerTurn(UUID id) {
        Seat s = getTurnSeat();
        return s != null && s.id.equals(id);
    }

    public boolean tryJoin(ServerPlayer player) {
        if (phase != Phase.WAITING) {
            player.displayClientMessage(
                    Component.translatable("message.simplecasino.bj_busy"), true);
            return false;
        }
        if (seats.size() >= MAX_PLAYERS) {
            player.displayClientMessage(
                    Component.translatable("message.simplecasino.bj_full"), true);
            return false;
        }
        for (Seat s : seats) {
            if (s.id.equals(player.getUUID())) {
                player.displayClientMessage(
                        Component.translatable("message.simplecasino.bj_already"), true);
                return false;
            }
        }
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty() || held.getCount() < 1) {
            player.displayClientMessage(
                    Component.translatable("message.simplecasino.empty_hand"), true);
            return false;
        }
        Item item = held.getItem();
        int count = held.getCount();
        held.shrink(count);
        seats.add(new Seat(player.getUUID(), player.getGameProfile().getName(), item, count));
        setChanged();
        sync();
        player.displayClientMessage(
                Component.translatable("message.simplecasino.bj_joined", count), true);
        return true;
    }

    public boolean tryLeave(ServerPlayer player) {
        if (phase != Phase.WAITING) {
            return false;
        }
        Iterator<Seat> it = seats.iterator();
        while (it.hasNext()) {
            Seat s = it.next();
            if (s.id.equals(player.getUUID())) {
                give(player, s.betItem, s.betCount);
                it.remove();
                setChanged();
                sync();
                return true;
            }
        }
        return false;
    }

    public boolean tryStart(ServerPlayer player) {
        if (phase != Phase.WAITING) {
            return false;
        }
        if (seats.size() < 2) {
            player.displayClientMessage(
                    Component.translatable("message.simplecasino.bj_need_two"), true);
            return false;
        }
        deck.clear();
        deck.addAll(BlackjackCards.freshShuffledDeck(random));
        for (Seat s : seats) {
            s.hand.clear();
            s.stood = false;
            s.bust = false;
            s.hand.add(draw());
            s.hand.add(draw());
            if (BlackjackCards.isBust(s.hand)) {
                s.bust = true;
                s.stood = true;
            }
        }
        phase = Phase.PLAYING;
        turnIndex = 0;
        advanceToActiveTurn();
        lastResult = "";
        setChanged();
        sync();
        return true;
    }

    public boolean tryHit(ServerPlayer player) {
        if (phase != Phase.PLAYING || !isPlayerTurn(player.getUUID())) {
            return false;
        }
        Seat s = getTurnSeat();
        if (s == null || s.stood || s.bust) {
            return false;
        }
        s.hand.add(draw());
        if (BlackjackCards.isBust(s.hand)) {
            s.bust = true;
            s.stood = true;
            nextTurn();
        }
        setChanged();
        sync();
        return true;
    }

    public boolean tryStand(ServerPlayer player) {
        if (phase != Phase.PLAYING || !isPlayerTurn(player.getUUID())) {
            return false;
        }
        Seat s = getTurnSeat();
        if (s == null) {
            return false;
        }
        s.stood = true;
        nextTurn();
        setChanged();
        sync();
        return true;
    }

    public void tryReset(ServerPlayer player) {
        if (phase == Phase.PLAYING) {
            return;
        }
        // refund if waiting
        if (phase == Phase.WAITING) {
            return;
        }
        phase = Phase.WAITING;
        seats.clear();
        deck.clear();
        turnIndex = 0;
        lastResult = "";
        setChanged();
        sync();
    }

    /** After finished, clear table for next game (no refund). */
    public void clearForNext() {
        phase = Phase.WAITING;
        seats.clear();
        deck.clear();
        turnIndex = 0;
        setChanged();
        sync();
    }

    private void nextTurn() {
        turnIndex++;
        advanceToActiveTurn();
        if (allDone()) {
            finishRound();
        }
    }

    private void advanceToActiveTurn() {
        while (turnIndex < seats.size()) {
            Seat s = seats.get(turnIndex);
            if (!s.stood && !s.bust) {
                return;
            }
            turnIndex++;
        }
    }

    private boolean allDone() {
        if (turnIndex >= seats.size()) {
            return true;
        }
        for (Seat s : seats) {
            if (!s.stood && !s.bust) {
                return false;
            }
        }
        return true;
    }

    private void finishRound() {
        phase = Phase.FINISHED;
        // find best score <= 21
        int best = -1;
        List<Seat> winners = new ArrayList<>();
        for (Seat s : seats) {
            int sc = BlackjackCards.score(s.hand);
            if (sc > 21) {
                continue;
            }
            if (sc > best) {
                best = sc;
                winners.clear();
                winners.add(s);
            } else if (sc == best) {
                winners.add(s);
            }
        }

        int pot = 0;
        Item potItem = null;
        for (Seat s : seats) {
            pot += s.betCount;
            potItem = s.betItem;
        }

        if (winners.isEmpty() || potItem == null) {
            lastResult = "Все перебрали — банк сгорел";
        } else if (winners.size() == 1) {
            Seat w = winners.get(0);
            lastResult = w.name + " забирает " + pot;
            ServerPlayer pl = findPlayer(w.id);
            if (pl != null) {
                give(pl, potItem, pot);
            } else {
                // drop at table
                dropAtTable(potItem, pot);
            }
        } else {
            // split pot as evenly as possible
            int share = pot / winners.size();
            int rest = pot % winners.size();
            StringBuilder sb = new StringBuilder("Ничья: ");
            for (int i = 0; i < winners.size(); i++) {
                Seat w = winners.get(i);
                int giveAmt = share + (i < rest ? 1 : 0);
                sb.append(w.name).append("+").append(giveAmt).append(" ");
                ServerPlayer pl = findPlayer(w.id);
                if (pl != null) {
                    give(pl, potItem, giveAmt);
                } else {
                    dropAtTable(potItem, giveAmt);
                }
            }
            lastResult = sb.toString().trim();
        }
        setChanged();
        sync();
    }

    private int draw() {
        if (deck.isEmpty()) {
            deck.addAll(BlackjackCards.freshShuffledDeck(random));
        }
        return deck.remove(deck.size() - 1);
    }

    private ServerPlayer findPlayer(UUID id) {
        if (level instanceof ServerLevel server) {
            return server.getServer().getPlayerList().getPlayer(id);
        }
        return null;
    }

    private void give(ServerPlayer player, Item item, int amount) {
        int left = amount;
        int max = new ItemStack(item).getMaxStackSize();
        while (left > 0) {
            int n = Math.min(left, max);
            ItemStack stack = new ItemStack(item, n);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            left -= n;
        }
    }

    private void dropAtTable(Item item, int amount) {
        if (level == null || level.isClientSide) {
            return;
        }
        int left = amount;
        int max = new ItemStack(item).getMaxStackSize();
        while (left > 0) {
            int n = Math.min(left, max);
            net.minecraft.world.entity.item.ItemEntity e =
                    new net.minecraft.world.entity.item.ItemEntity(
                            level,
                            worldPosition.getX() + 0.5,
                            worldPosition.getY() + 1.0,
                            worldPosition.getZ() + 0.5,
                            new ItemStack(item, n));
            level.addFreshEntity(e);
            left -= n;
        }
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider regs) {
        super.saveAdditional(tag, regs);
        tag.putString("Phase", phase.name());
        tag.putInt("Turn", turnIndex);
        tag.putString("Result", lastResult == null ? "" : lastResult);
        ListTag seatList = new ListTag();
        for (Seat s : seats) {
            CompoundTag st = new CompoundTag();
            st.putUUID("Id", s.id);
            st.putString("Name", s.name);
            st.putString("Item", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.betItem).toString());
            st.putInt("Bet", s.betCount);
            st.putBoolean("Stood", s.stood);
            st.putBoolean("Bust", s.bust);
            ListTag hand = new ListTag();
            for (int c : s.hand) {
                CompoundTag ct = new CompoundTag();
                ct.putInt("C", c);
                hand.add(ct);
            }
            st.put("Hand", hand);
            seatList.add(st);
        }
        tag.put("Seats", seatList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider regs) {
        super.loadAdditional(tag, regs);
        try {
            phase = Phase.valueOf(tag.getString("Phase"));
        } catch (Exception e) {
            phase = Phase.WAITING;
        }
        turnIndex = tag.getInt("Turn");
        lastResult = tag.getString("Result");
        seats.clear();
        ListTag seatList = tag.getList("Seats", Tag.TAG_COMPOUND);
        for (int i = 0; i < seatList.size(); i++) {
            CompoundTag st = seatList.getCompound(i);
            Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                    net.minecraft.resources.ResourceLocation.parse(st.getString("Item")));
            Seat s = new Seat(st.getUUID("Id"), st.getString("Name"), item, st.getInt("Bet"));
            s.stood = st.getBoolean("Stood");
            s.bust = st.getBoolean("Bust");
            ListTag hand = st.getList("Hand", Tag.TAG_COMPOUND);
            for (int j = 0; j < hand.size(); j++) {
                s.hand.add(hand.getCompound(j).getInt("C"));
            }
            seats.add(s);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider regs) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, regs);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
