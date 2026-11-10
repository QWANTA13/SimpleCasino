package ru.simplecasino.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import ru.simplecasino.blackjack.BlackjackMenu;

public class BlackjackActionPacket {
    private final String action;

    public BlackjackActionPacket(String action) {
        this.action = action;
    }

    public BlackjackActionPacket(FriendlyByteBuf buf) {
        this.action = buf.readUtf(16);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(action, 16);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.containerMenu instanceof BlackjackMenu menu) {
                menu.handleAction(player, action);
            }
        });
        ctx.setPacketHandled(true);
    }
}
