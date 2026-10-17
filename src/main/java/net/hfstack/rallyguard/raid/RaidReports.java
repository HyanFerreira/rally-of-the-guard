package net.hfstack.rallyguard.raid;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.network.payload.RaidReportS2CPayload;
import net.hfstack.rallyguard.progression.CombatLedgerSnapshot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.raid.Raid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RaidReports {
    public static final long MAX_LIFETIME_TICKS = 20L * 60L * 30L;
    private static final Map<SessionKey, RaidReportSession> SESSIONS = new HashMap<>();

    private RaidReports() {
    }

    public static void start(UUID playerId, RaidOpportunityKey raid, int guardsDeployed, long currentTick) {
        SESSIONS.put(new SessionKey(playerId, raid),
                new RaidReportSession(guardsDeployed, saturatedAdd(currentTick, MAX_LIFETIME_TICKS)));
    }

    public static void recordEnemyDefeat(ServerLevel level, RaidOpportunityKey raid, CombatLedgerSnapshot combat) {
        Map<UUID, GuardEntity> contributors = new HashMap<>();
        combat.guardDamage().keySet().forEach(guardId -> {
            Entity entity = level.getEntityInAnyDimension(guardId);
            if (entity instanceof GuardEntity guard) contributors.put(guardId, guard);
        });
        Set<UUID> creditedOwners = new HashSet<>();
        contributors.values().forEach(guard -> {
            UUID owner = GuardOwnership.getOwner(guard);
            if (owner != null && creditedOwners.add(owner)) {
                session(owner, raid).ifPresent(RaidReportSession::recordEnemyDefeated);
            }
        });
        combat.finalGuard().map(contributors::get).ifPresent(guard -> {
            UUID owner = GuardOwnership.getOwner(guard);
            if (owner != null) session(owner, raid).ifPresent(report ->
                    report.recordKill(guard.getUUID(), guard.getName().getString()));
        });
    }

    public static void recordEnemyDefeat(ServerLevel level, Raid raid, CombatLedgerSnapshot combat) {
        java.util.OptionalInt id = level.getRaids().getId(raid);
        if (id.isPresent()) {
            recordEnemyDefeat(level, new RaidOpportunityKey(
                    id.getAsInt(), level.dimension().identifier().toString(), raid.getCenter()
            ), combat);
        }
    }

    public static void recordGuardLost(GuardEntity guard) {
        UUID owner = GuardOwnership.getOwner(guard);
        if (owner == null || !(guard.level() instanceof ServerLevel level)) return;
        matching(owner, level, guard).forEach(RaidReportSession::recordGuardLost);
    }

    public static void recordGuardUnload(GuardEntity guard) {
        UUID owner = GuardOwnership.getOwner(guard);
        if (owner == null || !(guard.level() instanceof ServerLevel level)) return;
        matching(owner, level, guard).forEach(RaidReportSession::markIncomplete);
    }

    public static void finish(RaidOpportunityKey raid, MinecraftServer server, boolean incomplete) {
        Iterator<Map.Entry<SessionKey, RaidReportSession>> iterator = SESSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<SessionKey, RaidReportSession> entry = iterator.next();
            if (!entry.getKey().raid().equals(raid)) continue;
            send(server, entry.getKey().playerId(), entry.getValue().summary(incomplete));
            iterator.remove();
        }
    }

    public static void expire(MinecraftServer server, long currentTick) {
        Iterator<Map.Entry<SessionKey, RaidReportSession>> iterator = SESSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<SessionKey, RaidReportSession> entry = iterator.next();
            if (entry.getValue().expiresAtTick() >= currentTick) continue;
            send(server, entry.getKey().playerId(), entry.getValue().summary(true));
            iterator.remove();
        }
    }

    public static void clear() {
        SESSIONS.clear();
    }

    private static java.util.Optional<RaidReportSession> session(UUID owner, RaidOpportunityKey raid) {
        return java.util.Optional.ofNullable(SESSIONS.get(new SessionKey(owner, raid)));
    }

    private static java.util.List<RaidReportSession> matching(UUID owner, ServerLevel level, GuardEntity guard) {
        String dimension = level.dimension().identifier().toString();
        double maxDistanceSquared = RaidOpportunityPolicy.MAX_DISTANCE * RaidOpportunityPolicy.MAX_DISTANCE;
        java.util.List<RaidReportSession> matches = new ArrayList<>();
        SESSIONS.forEach((key, report) -> {
            if (key.playerId().equals(owner)
                    && key.raid().dimensionId().equals(dimension)
                    && guard.distanceToSqr(key.raid().center().getCenter()) <= maxDistanceSquared) {
                matches.add(report);
            }
        });
        return matches;
    }

    private static void send(MinecraftServer server, UUID playerId, RaidReportSummary summary) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player != null && ServerPlayNetworking.canSend(player, RaidReportS2CPayload.ID)) {
            ServerPlayNetworking.send(player, new RaidReportS2CPayload(summary));
        }
    }

    private static long saturatedAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    private record SessionKey(UUID playerId, RaidOpportunityKey raid) {
    }
}
