package net.hfstack.rallyguard;

import net.fabricmc.api.ModInitializer;
import net.hfstack.rallyguard.component.ModComponents;
import net.hfstack.rallyguard.effect.ModEffects;
import net.hfstack.rallyguard.event.InteractGuardHandler;
import net.hfstack.rallyguard.event.RallyFriendlyFireHandler;   // <- garantir import
import net.hfstack.rallyguard.event.HiredGuardsNeutralityHandler;
import net.hfstack.rallyguard.item.ModItems;
import net.hfstack.rallyguard.util.GuardVillagersConfigPatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RallyOfTheGuard implements ModInitializer {
    public static final String MOD_ID = "rallyguard";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        GuardVillagersConfigPatcher.patchFollowHeroConfig();

        ModComponents.initialize();
        ModItems.registerModItems();
        ModEffects.registerModEffects();

        InteractGuardHandler.register();
        RallyFriendlyFireHandler.register();
        HiredGuardsNeutralityHandler.register();

        LOGGER.info("[{}] iniciado.", MOD_ID);
    }
}
