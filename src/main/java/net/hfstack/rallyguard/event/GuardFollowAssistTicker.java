package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hfstack.rallyguard.order.GuardFollowAssistPolicy;
import net.hfstack.rallyguard.order.GuardOrders;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.OptionalDouble;

public final class GuardFollowAssistTicker {
    private static final Identifier GUARD_ID = Identifier.fromNamespaceAndPath("guardvillagers", "guard");
    private static final int TICK_INTERVAL = 2;

    private GuardFollowAssistTicker() {
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(world -> {
            if (world.getGameTime() % TICK_INTERVAL != 0) return;
            assistFollowingGuards(world);
        });
    }

    private static void assistFollowingGuards(ServerLevel world) {
        List<? extends Entity> entities = world.getEntities(
                BuiltInRegistries.ENTITY_TYPE.getValue(GUARD_ID),
                entity -> entity instanceof GuardEntity guard && guard.isFollowing()
        );
        for (Entity entity : entities) {
            if (!(entity instanceof GuardEntity guard)) continue;
            if (!(guard.getOwner() instanceof ServerPlayer owner) || owner.level() != world) continue;

            boolean fighting = guard.getTarget() != null && guard.getTarget().isAlive();
            OptionalDouble speed = GuardFollowAssistPolicy.speed(
                    guard.isFollowing(),
                    GuardOrders.isRallied(guard),
                    fighting,
                    owner.isSprinting(),
                    owner.isPassenger(),
                    guard.distanceToSqr(owner)
            );
            if (speed.isPresent()) {
                guard.getNavigation().moveTo(owner.getX(), owner.getY(), owner.getZ(), speed.getAsDouble());
            }
        }
    }
}
