package net.hfstack.rallyguard;

import net.fabricmc.api.ModInitializer;
import net.hfstack.rallyguard.component.ModComponents;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.effect.ModEffects;
import net.hfstack.rallyguard.event.GuardVillagersSpawnEggFix;
import net.hfstack.rallyguard.event.GuardRouteTicker;
import net.hfstack.rallyguard.event.HiredGuardsNeutralityHandler;
import net.hfstack.rallyguard.event.InteractGuardHandler;
import net.hfstack.rallyguard.event.RallyFormationTicker;
import net.hfstack.rallyguard.event.RallyFriendlyFireHandler;
import net.hfstack.rallyguard.item.ModItems;
import net.hfstack.rallyguard.network.GuardCommandNetworking;
import net.hfstack.rallyguard.network.NetworkBootstrap;
import net.hfstack.rallyguard.screen.ModScreenHandlers;
import net.hfstack.rallyguard.util.GuardVillagersConfigPatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RallyOfTheGuard implements ModInitializer {
    public static final String MOD_ID = "rallyguard";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        RallyConfig.load();
        GuardVillagersConfigPatcher.patchFollowHeroConfig();

        ModComponents.initialize();
        ModItems.registerModItems();
        ModEffects.registerModEffects();
        ModScreenHandlers.init();

        GuardVillagersSpawnEggFix.register();
        InteractGuardHandler.register();
        RallyFriendlyFireHandler.register();
        HiredGuardsNeutralityHandler.register();
        GuardRouteTicker.register();
        RallyFormationTicker.register();

        // ✅ Registra codecs uma vez por lado (client e server)
        NetworkBootstrap.registerTypesOnce();

        // ✅ Depois registra só os receivers do servidor
        GuardCommandNetworking.registerServer();


        LOGGER.info("[{}] iniciado.", MOD_ID);
    }
}
