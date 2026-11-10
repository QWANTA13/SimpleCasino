package ru.simplecasino.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.simplecasino.SimpleCasino;

public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SimpleCasino.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.simplecasino"))
                    .icon(() -> new ItemStack(ModBlocks.SLOT_MACHINE_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.SLOT_MACHINE_ITEM.get());
                        output.accept(ModBlocks.BLACKJACK_TABLE_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
