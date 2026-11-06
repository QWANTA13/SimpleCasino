package ru.simplecasino.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ru.simplecasino.registry.ModMenus;

import java.util.Random;

public class SlotMachineMenu extends AbstractContainerMenu {
    public static final int[] BETS = {1, 5, 10, 20};

    // data: reel0, reel1, reel2, betIndex, spinning(0/1), lastWin
    private final ContainerData data;
    private final Random random = new Random();

    public SlotMachineMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv);
    }

    public SlotMachineMenu(int id, Inventory playerInv) {
        super(ModMenus.SLOT_MACHINE.get(), id);
        this.data = new SimpleContainerData(6);

        // default bet = 1 diamond
        data.set(3, 0);
        data.set(0, SlotSymbols.CHERRY);
        data.set(1, SlotSymbols.LEMON);
        data.set(2, SlotSymbols.SEVEN);

        // player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }

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
        if (i < 0 || i >= BETS.length) return BETS[0];
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

    /**
     * Server-side spin. Returns true if spin started.
     */
    public boolean trySpin(ServerPlayer player) {
        if (isSpinning()) return false;

        int bet = getBetAmount();
        if (!takeDiamonds(player, bet)) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("message.simplecasino.not_enough_diamonds"),
                    true);
            return false;
        }

        data.set(4, 1); // spinning flag (for UI)
        data.set(5, 0);

        int r0 = random.nextInt(SlotSymbols.COUNT);
        int r1 = random.nextInt(SlotSymbols.COUNT);
        int r2 = random.nextInt(SlotSymbols.COUNT);
        // slight bias: make pure random; jackpot is rare enough via 1/125

        data.set(0, r0);
        data.set(1, r1);
        data.set(2, r2);

        int win = 0;
        if (r0 == r1 && r1 == r2) {
            win = bet * SlotSymbols.tripleMultiplier(r0);
        } else if (r0 == r1 || r1 == r2 || r0 == r2) {
            // two matching: small consolation = bet back sometimes? keep simple: no win
            win = 0;
        }

        if (win > 0) {
            giveDiamonds(player, win);
        }
        data.set(5, win);
        data.set(4, 0);
        return true;
    }

    private static boolean takeDiamonds(ServerPlayer player, int amount) {
        int left = amount;
        var inv = player.getInventory();
        // count
        int total = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(Items.DIAMOND)) total += stack.getCount();
        }
        if (total < amount) return false;

        for (int i = 0; i < inv.getContainerSize() && left > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.is(Items.DIAMOND)) continue;
            int take = Math.min(left, stack.getCount());
            stack.shrink(take);
            left -= take;
        }
        return true;
    }

    private static void giveDiamonds(ServerPlayer player, int amount) {
        ItemStack stack = new ItemStack(Items.DIAMOND, amount);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
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
