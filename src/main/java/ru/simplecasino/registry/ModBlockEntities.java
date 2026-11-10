package ru.simplecasino.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.simplecasino.SimpleCasino;
import ru.simplecasino.blackjack.BlackjackTableBlockEntity;

public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SimpleCasino.MOD_ID);

    public static final RegistryObject<BlockEntityType<BlackjackTableBlockEntity>> BLACKJACK_TABLE =
            BLOCK_ENTITIES.register("blackjack_table",
                    () -> BlockEntityType.Builder.of(
                            BlackjackTableBlockEntity::new,
                            ModBlocks.BLACKJACK_TABLE.get()
                    ).build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
