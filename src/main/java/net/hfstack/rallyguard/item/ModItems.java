package net.hfstack.rallyguard.item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

public class ModItems {

    public static final Item SCROLL_OF_RALLYING = registerItem("scroll_of_rallying",
            new ScrollOfRallyingItem(settings("scroll_of_rallying").stacksTo(1)));

    public static final Item COMMANDERS_LEDGER = registerItem("commanders_ledger",
            new CommandersLedgerItem(settings("commanders_ledger").stacksTo(1)));

    private static Item.Properties settings(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return new Item.Properties().setId(key);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, name), item);
    }

    public static void registerModItems() {
        RallyOfTheGuard.LOGGER.info("Registering Mod Items for " + RallyOfTheGuard.MOD_ID);

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(SCROLL_OF_RALLYING);
            entries.accept(COMMANDERS_LEDGER);
        });
    }
}
