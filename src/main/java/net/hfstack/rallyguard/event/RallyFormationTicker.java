package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.effect.ModEffects;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.GuardRoutes;
import net.hfstack.rallyguard.order.RallyFormationSlots;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RallyFormationTicker {
    private RallyFormationTicker() {
    }

    private static final Identifier GUARD_ID = Identifier.fromNamespaceAndPath("guardvillagers", "guard");
    private static final int TICK_INTERVAL = 2;
    private static final double HOLD_DISTANCE_SQUARED = 0.75 * 0.75;
    private static final double FORMATION_DISTANCE_SQUARED = 1.2 * 1.2;
    private static final Map<UUID, FormationAnchor> ANCHORS = new HashMap<>();

    private record FormationAnchor(BlockPos block, float yaw, boolean inFront) {
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(world -> {
            if (world.getGameTime() % TICK_INTERVAL != 0) return;
            tickWorld(world);
        });
    }

    public static void startRally(ServerPlayer player) {
        ANCHORS.put(player.getUUID(), new FormationAnchor(player.blockPosition(), player.getYRot(), true));
    }

    public static void stopRally(ServerPlayer player) {
        ANCHORS.remove(player.getUUID());
    }

    private static void tickWorld(ServerLevel world) {
        for (ServerPlayer player : world.players()) {
            if (!player.hasEffect(ModEffects.RALLY_COMMANDER)) continue;
            refreshRallyCommanderEffect(player);
            if (!RallyConfig.rallyFormationEnabled()) continue;
            tickPlayerFormation(world, player);
        }
    }

    private static void refreshRallyCommanderEffect(ServerPlayer player) {
        MobEffectInstance effect = player.getEffect(ModEffects.RALLY_COMMANDER);
        int durationTicks = RallyConfig.rallyEffectTimerSeconds() * 20;
        int refreshThresholdTicks = Math.max(1, Math.min(durationTicks - 1, durationTicks / 3));
        if (effect == null || effect.getDuration() > refreshThresholdTicks) return;

        player.addEffect(new MobEffectInstance(
                ModEffects.RALLY_COMMANDER,
                durationTicks,
                effect.getAmplifier(),
                false,
                false,
                true
        ));
    }

    private static void tickPlayerFormation(ServerLevel world, ServerPlayer player) {
        List<GuardEntity> guards = ralliedGuards(world, player);
        FormationAnchor anchor = anchorFor(player);

        for (int i = 0; i < guards.size(); i++) {
            GuardEntity guard = guards.get(i);
            if (isBusyFighting(guard)) continue;

            Vec3 slot = RallyFormationSlots.safeEscortSlot(world, player, i, anchor.yaw(), anchor.inFront());
            double distance = guard.distanceToSqr(slot.x, slot.y, slot.z);
            guard.setFollowing(false);
            guard.setPatrolling(false);

            double teleportDistance = RallyConfig.formationTeleportDistance();
            if (RallyConfig.rallyTeleportEnabled() && distance > teleportDistance * teleportDistance) {
                guard.snapTo(slot.x, slot.y, slot.z, guard.getYRot(), guard.getXRot());
                guard.setDeltaMovement(0.0, 0.0, 0.0);
                guard.getNavigation().stop();
                continue;
            }

            if (distance <= HOLD_DISTANCE_SQUARED) {
                holdSlot(guard, player);
                continue;
            }

            if (distance > FORMATION_DISTANCE_SQUARED) {
                guard.getNavigation().moveTo(slot.x, slot.y, slot.z, RallyConfig.formationReturnSpeed());
            } else {
                guard.getNavigation().stop();
                guard.setDeltaMovement(0.0, guard.getDeltaMovement().y, 0.0);
            }
        }
    }

    private static boolean isBusyFighting(GuardEntity guard) {
        LivingEntity target = guard.getTarget();
        if (target != null && target.isAlive()) {
            return true;
        }

        guard.setTarget(null);
        guard.setAggressive(false);
        guard.stopUsingItem();
        return false;
    }

    private static void holdSlot(GuardEntity guard, ServerPlayer player) {
        guard.getNavigation().stop();
        guard.setDeltaMovement(0.0, guard.getDeltaMovement().y, 0.0);
        guard.lookAt(player, 30.0F, 30.0F);
    }

    private static FormationAnchor anchorFor(ServerPlayer player) {
        BlockPos current = player.blockPosition();
        FormationAnchor anchor = ANCHORS.get(player.getUUID());
        if (anchor == null) {
            anchor = new FormationAnchor(current, player.getYRot(), true);
            ANCHORS.put(player.getUUID(), anchor);
            return anchor;
        }

        if (current.equals(anchor.block())) {
            return anchor;
        }

        int dx = current.getX() - anchor.block().getX();
        int dz = current.getZ() - anchor.block().getZ();
        float yaw = movementYaw(dx, dz, anchor.yaw());
        FormationAnchor moved = new FormationAnchor(current, yaw, false);
        ANCHORS.put(player.getUUID(), moved);
        return moved;
    }

    private static float movementYaw(int dx, int dz, float fallback) {
        if (dx == 0 && dz == 0) return fallback;
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    private static List<GuardEntity> ralliedGuards(ServerLevel world, ServerPlayer player) {
        List<? extends Entity> entities = world.getEntities(
                BuiltInRegistries.ENTITY_TYPE.getValue(GUARD_ID),
                entity -> entity instanceof GuardEntity guard
                        && GuardOwnership.isOwnedBy(entity, player.getUUID())
                        && GuardOrders.isRallied(guard)
                        && !guard.isPatrolling()
                        && !GuardOrders.isWaiting(guard)
                        && !GuardRoutes.get(guard).active()
                        && entity.distanceToSqr(player) <= RallyConfig.combatGuardSearchRadius() * RallyConfig.combatGuardSearchRadius()
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
