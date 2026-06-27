package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hfstack.rallyguard.order.GuardRouteState;
import net.hfstack.rallyguard.order.GuardRoutes;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public final class GuardRouteTicker {
    private GuardRouteTicker() {
    }

    private static final Identifier GUARD_ID = Identifier.of("guardvillagers", "guard");
    private static final int TICK_INTERVAL = 10;
    private static final double ARRIVAL_DISTANCE_SQUARED = 2.5 * 2.5;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getTime() % TICK_INTERVAL != 0) return;
            tickWorld(world);
        });
    }

    private static void tickWorld(ServerWorld world) {
        List<? extends Entity> guards = world.getEntitiesByType(
                Registries.ENTITY_TYPE.get(GUARD_ID),
                entity -> entity instanceof GuardEntity && GuardRoutes.get(entity).active()
        );

        for (Entity entity : guards) {
            if (entity instanceof GuardEntity guard) {
                tickGuard(guard);
            }
        }
    }

    private static void tickGuard(GuardEntity guard) {
        GuardRouteState route = GuardRoutes.get(guard);
        if (!route.active() || !route.canRun()) return;

        BlockPos target = route.currentPoint();
        if (target == null) return;

        guard.setFollowing(false);
        guard.setPatrolling(true);
        if (!target.equals(guard.getPatrolPos())) {
            guard.setPatrolPos(target);
        }

        if (guard.getTarget() != null) return;

        double distance = guard.squaredDistanceTo(
                target.getX() + 0.5,
                target.getY(),
                target.getZ() + 0.5
        );
        if (distance > ARRIVAL_DISTANCE_SQUARED) return;

        int dwellTicks = route.dwellTicks() + TICK_INTERVAL;
        if (dwellTicks < route.waitTicks()) {
            GuardRoutes.set(guard, route.withProgress(route.currentIndex(), dwellTicks));
            return;
        }

        int nextIndex = (route.currentIndex() + 1) % route.points().size();
        GuardRouteState next = route.withProgress(nextIndex, 0);
        guard.setPatrolPos(next.currentPoint());
        guard.getNavigation().stop();
        GuardRoutes.set(guard, next);
    }
}
