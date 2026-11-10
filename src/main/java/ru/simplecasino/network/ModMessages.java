package ru.simplecasino.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import ru.simplecasino.SimpleCasino;

public final class ModMessages {
    private ModMessages() {}

    private static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(SimpleCasino.MOD_ID, "main"))
            .networkProtocolVersion(1)
            .clientAcceptedVersions((status, version) -> true)
            .serverAcceptedVersions((status, version) -> true)
            .simpleChannel();

    private static int id = 0;

    public static void register(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CHANNEL.messageBuilder(SpinPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .decoder(SpinPacket::new)
                    .encoder(SpinPacket::encode)
                    .consumerMainThread(SpinPacket::handle)
                    .add();

            CHANNEL.messageBuilder(CycleBetPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .decoder(CycleBetPacket::new)
                    .encoder(CycleBetPacket::encode)
                    .consumerMainThread(CycleBetPacket::handle)
                    .add();

            CHANNEL.messageBuilder(BlackjackActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .decoder(BlackjackActionPacket::new)
                    .encoder(BlackjackActionPacket::encode)
                    .consumerMainThread(BlackjackActionPacket::handle)
                    .add();
        });
    }

    public static void sendToServer(Object packet) {
        CHANNEL.send(packet, PacketDistributor.SERVER.noArg());
    }
}
