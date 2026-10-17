package net.hfstack.rallyguard.raid;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.network.payload.RaidOpportunityS2CPayload;
import net.hfstack.rallyguard.service.GuardCommandTargets;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.raid.Raid;
import net.hfstack.rallyguard.api.command.GuardBatchCommandResult;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.OptionalInt;

public final class RaidOpportunityDetector {
    public static final int SCAN_INTERVAL_TICKS = 20;
    public static final int SESSION_LIFETIME_TICKS = 20 * 30;
    private static final RaidOpportunitySessions SESSIONS = new RaidOpportunitySessions();
    private static final Map<RaidOpportunityKey, TrackedRaid> TRACKED = new HashMap<>();
    private static boolean registered;

    private RaidOpportunityDetector() {
    }

    public static synchronized void register() {
        if (registered) return;
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long tick = server.getTickCount();
            if (tick % SCAN_INTERVAL_TICKS == 0) scan(server, tick);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                SESSIONS.clearPlayer(handler.getPlayer().getUUID()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> clear());
    }

    public static boolean consume(ServerPlayer player, RaidOpportunityKey key) {
        return SESSIONS.consume(player.getUUID(), key, player.level().getServer().getTickCount()).isPresent();
    }

    private static void scan(MinecraftServer server, long tick) {
        SESSIONS.expire(tick);
        RaidReports.expire(server, tick);
        forgetFinishedRaids(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) detectFor(player, tick);
    }

    private static void detectFor(ServerPlayer player, long tick) {
        ServerLevel level = player.level();
        Raid raid = level.getRaids().getNearbyRaid(player.blockPosition(), (int) RaidOpportunityPolicy.MAX_DISTANCE);
        if (raid == null) return;
        OptionalInt raidId = level.getRaids().getId(raid);
        if (raidId.isEmpty()) return;

        RaidOpportunityKey key = key(level, raidId.getAsInt(), raid);
        TRACKED.putIfAbsent(key, new TrackedRaid(level.dimension().identifier().toString(), raidId.getAsInt()));
        int loadedGuards = GuardCommandTargets.loadedScopedGuards(player, GuardCommandScope.all()).size();
        boolean eligible = RaidOpportunityPolicy.eligible(
                raid.isActive() && !raid.isOver() && !raid.isStopped(),
                player.isSpectator(),
                player.distanceToSqr(raid.getCenter().getCenter()),
                loadedGuards,
                SESSIONS.alreadySeen(player.getUUID(), key)
        );
        if (!eligible) return;

        RaidBehavior behavior = RaidBehaviors.get(player);
        if (behavior.automaticAction().isPresent()) {
            SESSIONS.markSeen(player.getUUID(), key);
            RaidResponseAction automaticAction = behavior.automaticAction().orElseThrow();
            GuardBatchCommandResult result = RaidDefenseActions.execute(automaticAction, player, raid);
            RaidReports.start(player.getUUID(), key, result.succeeded(), tick);
            player.sendOverlayMessage(Component.translatable(
                    automaticAction == RaidResponseAction.DEFEND_VILLAGE
                            ? "gui.rallyguard.raid.automatic_defend_result"
                            : "gui.rallyguard.raid.automatic_rally_result",
                    result.succeeded(), result.rejected(), result.unavailable()
            ));
            return;
        }
        if (!ServerPlayNetworking.canSend(player, RaidOpportunityS2CPayload.ID)) return;

        SESSIONS.open(player.getUUID(), key, tick, SESSION_LIFETIME_TICKS);
        ServerPlayNetworking.send(player, new RaidOpportunityS2CPayload(key, loadedGuards, behavior.wireId()));
    }

    private static void forgetFinishedRaids(MinecraftServer server) {
        Iterator<Map.Entry<RaidOpportunityKey, TrackedRaid>> iterator = TRACKED.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<RaidOpportunityKey, TrackedRaid> entry = iterator.next();
            TrackedRaid tracked = entry.getValue();
            ServerLevel level = findLevel(server, tracked.dimensionId());
            Raid raid = level == null ? null : level.getRaids().get(tracked.raidId());
            if (raid != null && raid.isActive() && !raid.isOver() && !raid.isStopped()
                    && raid.getCenter().equals(entry.getKey().center())) continue;
            RaidReports.finish(entry.getKey(), server, raid == null);
            SESSIONS.clearFinished(entry.getKey());
            iterator.remove();
        }
    }

    static ServerLevel findLevel(MinecraftServer server, String dimensionId) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().identifier().toString().equals(dimensionId)) return level;
        }
        return null;
    }

    static RaidOpportunityKey key(ServerLevel level, int raidId, Raid raid) {
        return new RaidOpportunityKey(raidId, level.dimension().identifier().toString(), raid.getCenter());
    }

    private static void clear() {
        SESSIONS.clear();
        TRACKED.clear();
        RaidReports.clear();
    }

    private record TrackedRaid(String dimensionId, int raidId) {
    }
}
