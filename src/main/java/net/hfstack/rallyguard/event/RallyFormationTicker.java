package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.effect.ModEffects;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.GuardRoutes;
import net.hfstack.rallyguard.order.RallyFormationSlots;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RallyFormationTicker {
    private RallyFormationTicker() {
    }

    private static final Identifier GUARD_ID = Identifier.of("guardvillagers", "guard");
    private static final int TICK_INTERVAL = 2;
    private static final double HOLD_DISTANCE_SQUARED = 0.75 * 0.75;
    private static final double FORMATION_DISTANCE_SQUARED = 1.2 * 1.2;
    private static final double TELEPORT_DISTANCE_SQUARED = 30.0 * 30.0;
    private static final double MOVE_SPEED = 1.0;
    private static final double SEARCH_RADIUS = 100.0;
    private static final Map<UUID, FormationAnchor> ANCHORS = new HashMap<>();

    private record FormationAnchor(BlockPos block, float yaw, boolean inFront) {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getTime() % TICK_INTERVAL != 0) return;
            tickWorld(world);
        });
    }

    public static void startRally(ServerPlayerEntity player) {
        ANCHORS.put(player.getUuid(), new FormationAnchor(player.getBlockPos(), player.getYaw(), true));
    }

    public static void stopRally(ServerPlayerEntity player) {
        ANCHORS.remove(player.getUuid());
    }

    private static void tickWorld(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (!player.hasStatusEffect(ModEffects.RALLY_COMMANDER)) continue;
            refreshRallyCommanderEffect(player);
            tickPlayerFormation(world, player);
        }
    }

    private static void refreshRallyCommanderEffect(ServerPlayerEntity player) {
        StatusEffectInstance effect = player.getStatusEffect(ModEffects.RALLY_COMMANDER);
        if (effect == null || effect.getDuration() > ModEffects.RALLY_COMMANDER_REFRESH_THRESHOLD_TICKS) return;

        player.addStatusEffect(new StatusEffectInstance(
                ModEffects.RALLY_COMMANDER,
                ModEffects.RALLY_COMMANDER_DURATION_TICKS,
                effect.getAmplifier(),
                false,
                false,
                true
        ));
    }

    private static void tickPlayerFormation(ServerWorld world, ServerPlayerEntity player) {
        List<GuardEntity> guards = ralliedGuards(world, player);
        FormationAnchor anchor = anchorFor(player);

        for (int i = 0; i < guards.size(); i++) {
            GuardEntity guard = guards.get(i);
            if (isBusyFighting(guard)) continue;

            Vec3d slot = RallyFormationSlots.safeEscortSlot(world, player, i, anchor.yaw(), anchor.inFront());
            double distance = guard.squaredDistanceTo(slot.x, slot.y, slot.z);
            guard.setFollowing(false);
            guard.setPatrolling(false);

            if (distance > TELEPORT_DISTANCE_SQUARED) {
                guard.refreshPositionAndAngles(slot.x, slot.y, slot.z, guard.getYaw(), guard.getPitch());
                guard.setVelocity(0.0, 0.0, 0.0);
                guard.getNavigation().stop();
                continue;
            }

            if (distance <= HOLD_DISTANCE_SQUARED) {
                holdSlot(guard, player);
                continue;
            }

            if (distance > FORMATION_DISTANCE_SQUARED) {
                guard.getNavigation().startMovingTo(slot.x, slot.y, slot.z, MOVE_SPEED);
            } else {
                guard.getNavigation().stop();
                guard.setVelocity(0.0, guard.getVelocity().y, 0.0);
            }
        }
    }

    private static boolean isBusyFighting(GuardEntity guard) {
        LivingEntity target = guard.getTarget();
        if (target != null && target.isAlive()) {
            return true;
        }

        guard.setTarget(null);
        guard.setAttacking(false);
        guard.clearActiveItem();
        return false;
    }

    private static void holdSlot(GuardEntity guard, ServerPlayerEntity player) {
        guard.getNavigation().stop();
        guard.setVelocity(0.0, guard.getVelocity().y, 0.0);
        guard.lookAtEntity(player, 30.0F, 30.0F);
    }

    private static FormationAnchor anchorFor(ServerPlayerEntity player) {
        BlockPos current = player.getBlockPos();
        FormationAnchor anchor = ANCHORS.get(player.getUuid());
        if (anchor == null) {
            anchor = new FormationAnchor(current, player.getYaw(), true);
            ANCHORS.put(player.getUuid(), anchor);
            return anchor;
        }

        if (current.equals(anchor.block())) {
            return anchor;
        }

        int dx = current.getX() - anchor.block().getX();
        int dz = current.getZ() - anchor.block().getZ();
        float yaw = movementYaw(dx, dz, anchor.yaw());
        FormationAnchor moved = new FormationAnchor(current, yaw, false);
        ANCHORS.put(player.getUuid(), moved);
        return moved;
    }

    private static float movementYaw(int dx, int dz, float fallback) {
        if (dx == 0 && dz == 0) return fallback;
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    private static List<GuardEntity> ralliedGuards(ServerWorld world, ServerPlayerEntity player) {
        List<? extends Entity> entities = world.getEntitiesByType(
                Registries.ENTITY_TYPE.get(GUARD_ID),
                entity -> entity instanceof GuardEntity guard
                        && GuardOwnership.isOwnedBy(entity, player.getUuid())
                        && GuardOrders.isRallied(guard)
                        && !guard.isPatrolling()
                        && !GuardOrders.isWaiting(guard)
                        && !GuardRoutes.get(guard).active()
                        && entity.squaredDistanceTo(player) <= SEARCH_RADIUS * SEARCH_RADIUS
        );

        List<GuardEntity> guards = new ArrayList<>(entities.size());
        for (Entity entity : entities) {
            if (entity instanceof GuardEntity guard) {
                guards.add(guard);
            }
        }
        guards.sort(Comparator.comparingInt(Entity::getId));
        return guards;
    }
}
