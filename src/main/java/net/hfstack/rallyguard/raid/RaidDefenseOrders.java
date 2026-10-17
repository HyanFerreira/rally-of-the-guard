package net.hfstack.rallyguard.raid;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.order.defend.DefendAreaState;
import net.hfstack.rallyguard.order.defend.DefendAreas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.raid.Raid;

import java.util.List;

public final class RaidDefenseOrders {
    private static final Identifier GUARD_ID = Identifier.fromNamespaceAndPath("guardvillagers", "guard");

    private RaidDefenseOrders() {
    }

    public static void release(MinecraftServer server, RaidOpportunityKey key) {
        ServerLevel level = RaidOpportunityDetector.findLevel(server, key.dimensionId());
        if (level == null) return;
        List<? extends Entity> guards = level.getEntities(
                BuiltInRegistries.ENTITY_TYPE.getValue(GUARD_ID),
                entity -> entity instanceof GuardEntity guard && belongsTo(guard, key)
        );
        guards.forEach(entity -> releaseGuard((GuardEntity) entity, key));
    }

    public static void releaseIfFinished(GuardEntity guard, ServerLevel level) {
        DefendAreaState order = DefendAreas.active(guard).orElse(null);
        if (order == null || order.raidId().isEmpty()) return;
        Raid raid = level.getRaids().get(order.raidId().getAsInt());
        boolean stillActive = raid != null
                && raid.isActive()
                && !raid.isOver()
                && !raid.isStopped()
                && raid.getCenter().equals(order.center());
        if (!stillActive) releaseGuard(guard, order.raidId().getAsInt(), order.center());
    }

    private static boolean belongsTo(GuardEntity guard, RaidOpportunityKey key) {
        DefendAreaState order = DefendAreas.active(guard).orElse(null);
        return order != null
                && order.raidId().isPresent()
                && order.raidId().getAsInt() == key.raidId()
                && order.center().equals(key.center());
    }

    private static void releaseGuard(GuardEntity guard, RaidOpportunityKey key) {
        releaseGuard(guard, key.raidId(), key.center());
    }

    private static void releaseGuard(GuardEntity guard, int raidId, BlockPos center) {
        if (!DefendAreas.clearIfFromRaid(guard, raidId, center)) return;
        guard.setPatrolling(false);
        guard.setPatrolPos(null);
        guard.getNavigation().stop();
    }
}
