package net.hfstack.rallyguard;

import net.fabricmc.api.ModInitializer;
import net.hfstack.rallyguard.component.ModComponents;
import net.hfstack.rallyguard.combat.stance.CombatStanceAttachments;
import net.hfstack.rallyguard.command.RallyGuardCommands;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.effect.ModEffects;
import net.hfstack.rallyguard.event.GuardVillagersSpawnEggFix;
import net.hfstack.rallyguard.event.CombatStanceTicker;
import net.hfstack.rallyguard.event.DefendAreaTicker;
import net.hfstack.rallyguard.event.GuardAllySafetyHandler;
import net.hfstack.rallyguard.event.GuardRouteTicker;
import net.hfstack.rallyguard.event.HiredGuardsNeutralityHandler;
import net.hfstack.rallyguard.event.InteractGuardHandler;
import net.hfstack.rallyguard.event.MoveOrderTicker;
import net.hfstack.rallyguard.event.RallyFormationTicker;
import net.hfstack.rallyguard.event.RallyFriendlyFireHandler;
import net.hfstack.rallyguard.item.ModItems;
import net.hfstack.rallyguard.interaction.GuardInteractionLocks;
import net.hfstack.rallyguard.network.GuardCommandNetworking;
import net.hfstack.rallyguard.network.NetworkBootstrap;
import net.hfstack.rallyguard.network.MusterRollNetworking;
import net.hfstack.rallyguard.network.MoveHereNetworking;
import net.hfstack.rallyguard.network.DefendAreaNetworking;
import net.hfstack.rallyguard.network.CombatStanceNetworking;
import net.hfstack.rallyguard.network.FormationSelectionNetworking;
import net.hfstack.rallyguard.muster.MusterRollSessions;
import net.hfstack.rallyguard.order.move.MoveOrderAttachments;
import net.hfstack.rallyguard.order.defend.DefendAreaAttachments;
import net.hfstack.rallyguard.profile.GuardProfileAttachments;
import net.hfstack.rallyguard.profile.GuardProfileLifecycle;
import net.hfstack.rallyguard.profile.GuardProfileRepository;
import net.hfstack.rallyguard.route.NamedRouteAttachments;
import net.hfstack.rallyguard.progression.CombatProgressionHandler;
import net.hfstack.rallyguard.screen.ModScreenHandlers;
import net.hfstack.rallyguard.squad.SquadAttachments;
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
        GuardProfileAttachments.initialize();
        CombatStanceAttachments.initialize();
        MoveOrderAttachments.initialize();
        DefendAreaAttachments.initialize();
        SquadAttachments.initialize();
        NamedRouteAttachments.initialize();
        GuardProfileLifecycle.register(GuardProfileRepository.instance());
        ModItems.registerModItems();
        ModEffects.registerModEffects();
        ModScreenHandlers.init();
        RallyGuardCommands.register();
        GuardInteractionLocks.register();

        GuardVillagersSpawnEggFix.register();
        InteractGuardHandler.register();
        RallyFriendlyFireHandler.register();
        GuardAllySafetyHandler.register();
        CombatStanceTicker.register();
        CombatProgressionHandler.register();
        HiredGuardsNeutralityHandler.register();
        GuardRouteTicker.register();
        MoveOrderTicker.register();
        DefendAreaTicker.register();
        RallyFormationTicker.register();

        // ✅ Registra codecs uma vez por lado (client e server)
        NetworkBootstrap.registerTypesOnce();
        MusterRollNetworking.registerServer();
        FormationSelectionNetworking.registerServer();
        MoveHereNetworking.registerServer();
        DefendAreaNetworking.registerServer();
        CombatStanceNetworking.registerServer();
        MusterRollSessions.register();

        // ✅ Depois registra só os receivers do servidor
        GuardCommandNetworking.registerServer();


        LOGGER.info("[{}] iniciado.", MOD_ID);
    }
}
