package ru.simplecasino.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import ru.simplecasino.SimpleCasino;
import ru.simplecasino.menu.SlotMachineMenu;
import ru.simplecasino.registry.ModMenus;

@Mod.EventBusSubscriber(modid = SimpleCasino.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                MenuScreens.register(ModMenus.SLOT_MACHINE.get(), SlotMachineScreen::new));
    }
}
