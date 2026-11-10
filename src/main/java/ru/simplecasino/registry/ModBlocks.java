package ru.simplecasino.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.simplecasino.SimpleCasino;
import ru.simplecasino.block.SlotMachineBlock;
import ru.simplecasino.blackjack.BlackjackTableBlock;

public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SimpleCasino.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SimpleCasino.MOD_ID);

    public static final RegistryObject<Block> SLOT_MACHINE = BLOCKS.register("slot_machine",
            () -> new SlotMachineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final RegistryObject<Item> SLOT_MACHINE_ITEM = ITEMS.register("slot_machine",
            () -> new BlockItem(SLOT_MACHINE.get(), new Item.Properties()));

    
    public static final RegistryObject<Block> BLACKJACK_TABLE = BLOCKS.register(
            "blackjack_table",
            () -> new BlackjackTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.WOOD)));

    public static final RegistryObject<Item> BLACKJACK_TABLE_ITEM = ITEMS.register(
            "blackjack_table",
            () -> new BlockItem(BLACKJACK_TABLE.get(), new Item.Properties()));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
