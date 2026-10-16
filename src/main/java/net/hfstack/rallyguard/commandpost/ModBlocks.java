package net.hfstack.rallyguard.commandpost;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
    private static final Identifier COMMAND_POST_ID = Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "command_post");
    private static final ResourceKey<Block> COMMAND_POST_KEY = ResourceKey.create(Registries.BLOCK, COMMAND_POST_ID);
    private static final ResourceKey<Item> COMMAND_POST_ITEM_KEY = ResourceKey.create(Registries.ITEM, COMMAND_POST_ID);

    public static final Block COMMAND_POST = Registry.register(
            BuiltInRegistries.BLOCK,
            COMMAND_POST_ID,
            new CommandPostBlock(BlockBehaviour.Properties.of()
                    .setId(COMMAND_POST_KEY)
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD))
    );
    public static final Item COMMAND_POST_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            COMMAND_POST_ID,
            new BlockItem(COMMAND_POST, new Item.Properties().setId(COMMAND_POST_ITEM_KEY))
    );

    private ModBlocks() {
    }

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> entries.accept(COMMAND_POST_ITEM));
    }
}
