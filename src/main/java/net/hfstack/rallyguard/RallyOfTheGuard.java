package net.hfstack.rallyguard;

import net.fabricmc.api.ModInitializer;

import net.hfstack.rallyguard.effect.ModEffects;
import net.hfstack.rallyguard.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RallyOfTheGuard implements ModInitializer {
    public static final String MOD_ID = "rallyguard";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.registerModItems();
        ModEffects.registerModEffects();
    }
}