package ru.simplecasino.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import ru.simplecasino.menu.SlotMachineMenu;

public class CycleBetPacket {
    public CycleBetPacket() {}

    public CycleBetPacket(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public void handle(CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.containerMenu instanceof SlotMachineMenu menu) {
                menu.cycleBet();
            }
        });
        ctx.setPacketHandled(true);
    }
}
