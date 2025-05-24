package net.hfstack.rallyguard.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item SCROLL_OF_RALLYING = registerItem("scroll_of_rallying",
            new ScrollOfRallyingItem(new Item.Settings().maxCount(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(RallyOfTheGuard.MOD_ID, name), item);
    }

    public static void registerModItems() {
        RallyOfTheGuard.LOGGER.info("Registering Mod Items for " + RallyOfTheGuard.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(SCROLL_OF_RALLYING);
        });
    }
}
