package ru.simplecasino.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.simplecasino.SimpleCasino;
import ru.simplecasino.menu.SlotMachineMenu;

public final class ModMenus {
    private ModMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SimpleCasino.MOD_ID);

    public static final RegistryObject<MenuType<SlotMachineMenu>> SLOT_MACHINE =
            MENUS.register("slot_machine",
                    () -> IForgeMenuType.create(SlotMachineMenu::new));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
