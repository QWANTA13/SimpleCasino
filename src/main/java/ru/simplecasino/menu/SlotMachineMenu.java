package ru.simplecasino.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import ru.simplecasino.registry.ModMenus;

import java.util.Random;

public class SlotMachineMenu extends AbstractContainerMenu {
    public static final int[] BETS = {1, 5, 10, 20};

    private final ContainerData data;
    private final Random random = new Random();

    public SlotMachineMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv);
    }

    public SlotMachineMenu(int id, Inventory playerInv) {
        super(ModMenus.SLOT_MACHINE.get(), id);
        this.data = new SimpleContainerData(6);
        data.set(3, 0);
        data.set(0, SlotSymbols.CHERRY);
        data.set(1, SlotSymbols.LEMON);
        data.set(2, SlotSymbols.SEVEN);
        addDataSlots(data);
    }

    public int getReel(int index) {
        return data.get(index);
    }

    public int getBetIndex() {
        return data.get(3);
    }

    public int getBetAmount() {
        int i = getBetIndex();
        if (i < 0 || i >= BETS.length) {
            return BETS[0];
        }
        return BETS[i];
    }

    public boolean isSpinning() {
        return data.get(4) != 0;
    }

    public int getLastWin() {
        return data.get(5);
    }

    public void cycleBet() {
        int next = (getBetIndex() + 1) % BETS.length;
        data.set(3, next);
    }

    public boolean trySpin(ServerPlayer player) {
        if (isSpinning()) {
            return false;
        }

        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.simplecasino.empty_hand"),
                    true);
            return false;
        }

        Item betItem = held.getItem();
        int bet = getBetAmount();

        if (!takeItems(player, betItem, bet)) {
            player.displayClientMessage(
                    Component.translatable("message.simplecasino.not_enough_items"),
                    true);
            return false;
        }

       data.set(4, 1);
        data.set(5, 0);

        // 125 равных комбинаций (5×5×5)
        int r0 = random.nextInt(SlotSymbols.COUNT);
        int r1 = random.nextInt(SlotSymbols.COUNT);
        int r2 = random.nextInt(SlotSymbols.COUNT);

        data.set(0, r0);
        data.set(1, r1);
        data.set(2, r2);

        int win = 0;
        if (r0 == r1 && r1 == r2) {
            win = bet * SlotSymbols.tripleMultiplier(r0);
        } else if (r0 == r1 || r1 == r2 || r0 == r2) {
            win = bet / 2;
        }

        if (win > 0) {
            giveItems(player, betItem, win);
        }
        data.set(5, win);
        data.set(4, 0);
        return true;
    }

        int win = 0;
        if (r0 == r1 && r1 == r2) {
            win = bet * SlotSymbols.tripleMultiplier(r0);
        } else if (r0 == r1 || r1 == r2 || r0 == r2) {
            win = bet / 2;
        }

        if (win > 0) {
            giveItems(player, betItem, win);
        }
        data.set(5, win);
        data.set(4, 0);
        return true;
    }

    private int rollWeightedSymbol() {
        int roll = random.nextInt(100);
        if (roll < 35) {
            return SlotSymbols.CHERRY;
        }
        if (roll < 60) {
            return SlotSymbols.LEMON;
        }
        if (roll < 78) {
            return SlotSymbols.DIAMOND;
        }
        if (roll < 92) {
            return SlotSymbols.SEVEN;
        }
        return SlotSymbols.BAR;
    }

    private static int countItem(ServerPlayer player, Item item) {
        int total = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static boolean takeItems(ServerPlayer player, Item item, int amount) {
        if (countItem(player, item) < amount) {
            return false;
        }
        int left = amount;
        Inventory inv = player.getInventory();

        ItemStack main = player.getMainHandItem();
        if (main.is(item)) {
            int take = Math.min(left, main.getCount());
            main.shrink(take);
            left -= take;
        }

        for (int i = 0; i < inv.getContainerSize() && left > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.is(item)) {
                continue;
            }
            int take = Math.min(left, stack.getCount());
            stack.shrink(take);
            left -= take;
        }
        return left == 0;
    }

    private static void giveItems(ServerPlayer player, Item item, int amount) {
        int left = amount;
        int max = new ItemStack(item).getMaxStackSize();
        while (left > 0) {
            int give = Math.min(left, max);
            ItemStack stack = new ItemStack(item, give);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            left -= give;
        }
    }

    public static final int BET_ALL = BETS.length; // 4

public void cycleBet() {
    int next = (getBetIndex() + 1) % (BETS.length + 1);
    data.set(3, next);
}

public int getBetAmount(Player player) {
    if (!isBetAll()) return getBetAmount();
    // count all of held item in inventory
}
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
