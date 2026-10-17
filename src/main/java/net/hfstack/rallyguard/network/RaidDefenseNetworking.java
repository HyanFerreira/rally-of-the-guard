package net.hfstack.rallyguard.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.api.command.GuardBatchCommandResult;
import net.hfstack.rallyguard.network.payload.RaidResponseC2SPayload;
import net.hfstack.rallyguard.network.payload.RaidBehaviorC2SPayload;
import net.hfstack.rallyguard.network.payload.RaidBehaviorS2CPayload;
import net.hfstack.rallyguard.raid.RaidOpportunityDetector;
import net.hfstack.rallyguard.raid.RaidOpportunityKey;
import net.hfstack.rallyguard.raid.RaidOpportunityPolicy;
import net.hfstack.rallyguard.raid.RaidDefenseActions;
import net.hfstack.rallyguard.raid.RaidBehavior;
import net.hfstack.rallyguard.raid.RaidBehaviors;
import net.hfstack.rallyguard.raid.RaidResponseAction;
import net.hfstack.rallyguard.service.GuardCommandTargets;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.raid.Raid;

public final class RaidDefenseNetworking {
    private static boolean registered;

    private RaidDefenseNetworking() {
    }

    public static synchronized void registerServer() {
        if (registered) return;
        registered = true;
        ServerPlayNetworking.registerGlobalReceiver(RaidResponseC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handle(player, payload));
        });
        ServerPlayNetworking.registerGlobalReceiver(RaidBehaviorC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleBehavior(player, payload));
        });
    }

    private static void handleBehavior(ServerPlayer player, RaidBehaviorC2SPayload payload) {
        if (payload.behaviorId() != RaidBehaviorC2SPayload.QUERY) {
            RaidBehavior behavior = RaidBehavior.fromWireId(payload.behaviorId()).orElse(null);
            if (behavior == null) {
                reject(player);
                return;
            }
            RaidBehaviors.set(player, behavior);
            player.sendOverlayMessage(Component.translatable(
                    "gui.rallyguard.raid.behavior.saved", Component.translatable(behavior.translationKey())
            ));
        }
        ServerPlayNetworking.send(player, new RaidBehaviorS2CPayload(RaidBehaviors.get(player).wireId()));
    }

    private static void handle(ServerPlayer player, RaidResponseC2SPayload payload) {
        RaidResponseAction action = RaidResponseAction.fromWireId(payload.actionId()).orElse(null);
        RaidOpportunityKey key;
        try {
            key = payload.key();
        } catch (IllegalArgumentException exception) {
            reject(player);
            return;
        }
        if (action == null || !RaidOpportunityDetector.consume(player, key)) {
            reject(player);
            return;
        }

        ServerLevel level = player.level();
        Raid raid = level.dimension().identifier().toString().equals(key.dimensionId())
                ? level.getRaids().get(key.raidId()) : null;
        int loadedGuards = GuardCommandTargets.loadedScopedGuards(player, GuardCommandScope.all()).size();
        boolean valid = raid != null
                && raid.getCenter().equals(key.center())
                && RaidOpportunityPolicy.eligible(
                        raid.isActive() && !raid.isOver() && !raid.isStopped(),
                        player.isSpectator(),
                        player.distanceToSqr(key.center().getCenter()),
                        loadedGuards,
                        false
                );
        if (!valid) {
            reject(player);
            return;
        }

        if (action == RaidResponseAction.IGNORE) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.raid.ignored"));
            return;
        }

        GuardBatchCommandResult result = RaidDefenseActions.execute(action, player, raid);
        player.sendOverlayMessage(Component.translatable(
                "gui.rallyguard.raid.batch_result",
                result.succeeded(),
                result.rejected(),
                result.unavailable()
        ));
    }

    private static void reject(ServerPlayer player) {
        player.sendOverlayMessage(Component.translatable("gui.rallyguard.raid.response_invalid"));
    }
}
