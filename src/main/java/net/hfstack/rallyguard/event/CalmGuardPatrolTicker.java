package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.order.GuardRoutes;
import net.hfstack.rallyguard.order.PatrolMovementPolicy;
import net.hfstack.rallyguard.order.defend.DefendAreas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.List;

public final class CalmGuardPatrolTicker {
    private static final Identifier GUARD_ID = Identifier.fromNamespaceAndPath("guardvillagers", "guard");
    private static final int TICK_INTERVAL = 10;
    private static final double ARRIVAL_DISTANCE_SQUARED = 1.25 * 1.25;

    private CalmGuardPatrolTicker() {
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(world -> {
            if (world.getGameTime() % TICK_INTERVAL == 0) tickWorld(world);
        });
    }

    private static void tickWorld(ServerLevel world) {
        List<? extends Entity> guards = world.getEntities(
                BuiltInRegistries.ENTITY_TYPE.getValue(GUARD_ID),
                entity -> entity instanceof GuardEntity guard
                        && GuardOwnership.hasOwner(guard)
                        && guard.isPatrolling()
                        && guard.getPatrolPos() != null
                        && !GuardRoutes.get(guard).active()
                        && DefendAreas.active(guard).isEmpty()
        );
        for (Entity entity : guards) {
            if (entity instanceof GuardEntity guard) tickGuard(guard);
        }
    }

    private static void tickGuard(GuardEntity guard) {
        BlockPos anchor = guard.getPatrolPos();
        if (anchor == null || guard.getTarget() != null) {
            CalmPatrolMovement.clear(guard);
            return;
        }

        double distance = guard.distanceToSqr(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5);
        if (distance > ARRIVAL_DISTANCE_SQUARED) {
            CalmPatrolMovement.clear(guard);
            guard.getNavigation().moveTo(
                    anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5,
                    PatrolMovementPolicy.walkingSpeed(RallyConfig.routeMoveSpeed())
            );
            return;
        }

        CalmPatrolMovement.holdAt(guard, anchor, true);
    }
}
