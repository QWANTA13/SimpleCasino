package ru.simplecasino.blackjack;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import ru.simplecasino.registry.ModMenus;

public class BlackjackMenu extends AbstractContainerMenu {
    private final BlackjackTableBlockEntity table;
    private final BlockPos pos;

    public BlackjackMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, getTable(inv, buf.readBlockPos()));
    }

    public BlackjackMenu(int id, Inventory inv, BlackjackTableBlockEntity table) {
        super(ModMenus.BLACKJACK_TABLE.get(), id);
        this.table = table;
        this.pos = table.getBlockPos();
    }

    private static BlackjackTableBlockEntity getTable(Inventory inv, BlockPos pos) {
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (be instanceof BlackjackTableBlockEntity t) {
            return t;
        }
        throw new IllegalStateException("No blackjack table at " + pos);
    }

    public BlackjackTableBlockEntity getTable() {
        return table;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void handleAction(ServerPlayer player, String action) {
        switch (action) {
            case "join" -> table.tryJoin(player);
            case "leave" -> table.tryLeave(player);
            case "start" -> table.tryStart(player);
            case "hit" -> table.tryHit(player);
            case "stand" -> table.tryStand(player);
            case "next" -> {
                if (table.getPhase() == BlackjackTableBlockEntity.Phase.FINISHED) {
                    table.clearForNext();
                }
            }
            default -> {
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}
