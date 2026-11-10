package ru.simplecasino;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import ru.simplecasino.network.ModMessages;
import ru.simplecasino.registry.ModBlocks;
import ru.simplecasino.registry.ModBlockEntities;
import ru.simplecasino.registry.ModCreativeTabs;
import ru.simplecasino.registry.ModMenus;

@Mod(SimpleCasino.MOD_ID)
public class SimpleCasino {
    public static final String MOD_ID = "simplecasino";

    public SimpleCasino() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.register(bus);
        ModBlockEntities.register(bus);
        ModMenus.register(bus);
        ModCreativeTabs.register(bus);
        bus.addListener(ModMessages::register);
    }
}
