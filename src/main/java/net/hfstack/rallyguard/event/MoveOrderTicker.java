package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.move.MoveOrderMovementPolicy;
import net.hfstack.rallyguard.order.move.MoveOrderState;
import net.hfstack.rallyguard.order.move.MoveOrders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.List;

public final class MoveOrderTicker {
    private static final Identifier GUARD_ID = Identifier.fromNamespaceAndPath("guardvillagers", "guard");
    private static final int TICK_INTERVAL = 10;

    private MoveOrderTicker() {
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(world -> {
            if (world.getGameTime() % TICK_INTERVAL == 0) tickWorld(world);
        });
    }

    private static void tickWorld(ServerLevel world) {
        List<? extends Entity> guards = world.getEntities(
                BuiltInRegistries.ENTITY_TYPE.getValue(GUARD_ID),
                entity -> entity instanceof GuardEntity && MoveOrders.active(entity).isPresent()
        );
        for (Entity entity : guards) {
            if (entity instanceof GuardEntity guard) tickGuard(guard);
        }
    }

    private static void tickGuard(GuardEntity guard) {
        MoveOrderState order = MoveOrders.active(guard).orElse(null);
        if (order == null) return;

        BlockPos target = order.target();
        double distanceSquared = guard.distanceToSqr(
                target.getX() + 0.5,
                target.getY(),
                target.getZ() + 0.5
        );
        MoveOrderMovementPolicy.execute(
                MoveOrderMovementPolicy.decide(guard.getTarget() != null, distanceSquared),
                () -> guard.getNavigation().moveTo(
                    target.getX() + 0.5,
                    target.getY(),
                    target.getZ() + 0.5,
                    RallyConfig.routeMoveSpeed()
                ),
                () -> {
                    guard.getNavigation().stop();
                    MoveOrders.clear(guard);
                    GuardOrders.setWaiting(guard, true);
                }
        );
    }
}
