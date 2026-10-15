package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.combat.GuardCombatState;
import net.hfstack.rallyguard.combat.ProtectedAllyPolicy;
import net.hfstack.rallyguard.combat.RallyCombatPolicy;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.effect.ModEffects;
import net.hfstack.rallyguard.formation.RallyFormation;
import net.hfstack.rallyguard.formation.FormationHeadingPolicy;
import net.hfstack.rallyguard.formation.FormationHeadingState;
import net.hfstack.rallyguard.formation.FormationMovementPlan;
import net.hfstack.rallyguard.formation.FormationMovementPolicy;
import net.hfstack.rallyguard.formation.FormationVelocity;
import net.hfstack.rallyguard.formation.FormationVelocityPolicy;
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
    private static final double COMMANDER_MOVEMENT_SQUARED = 0.02 * 0.02;
    private static final double DIRECT_CONTROL_DISTANCE_SQUARED = 3.0 * 3.0;
    private static final Map<UUID, FormationAnchor> ANCHORS = new HashMap<>();

    private record FormationAnchor(
            Vec3 position,
            FormationHeadingState heading,
            boolean inFront,
            RallyFormation formation,
            Vec3 movement,
            boolean moving
    ) {
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(world -> {
            tickWorld(world);
        });
    }

    public static void startRally(ServerPlayer player) {
        startRally(player, RallyFormation.ESCORT);
    }

    public static void startRally(ServerPlayer player, RallyFormation formation) {
        ANCHORS.put(
                player.getUUID(),
                new FormationAnchor(
                        player.position(),
                        FormationHeadingState.initial(player.getYRot()),
                        true,
                        formation,
                        new Vec3(0.0, 0.0, 0.0),
                        false
                )
        );
    }

    public static void updateFormation(ServerPlayer player, RallyFormation formation) {
        ANCHORS.computeIfPresent(player.getUUID(), (ignored, anchor) -> new FormationAnchor(
                anchor.position(),
                anchor.heading(),
                anchor.inFront(),
                formation,
                anchor.movement(),
                anchor.moving()
        ));
    }

    public static void stopRally(ServerPlayer player) {
        ANCHORS.remove(player.getUUID());
    }

    private static void tickWorld(ServerLevel world) {
        for (ServerPlayer player : world.players()) {
            if (!player.hasEffect(ModEffects.RALLY_COMMANDER)) continue;
            refreshRallyCommanderEffect(player);
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
            LivingEntity target = guard.getTarget();
            boolean hasLiveTarget = target != null && target.isAlive();
            boolean protectedTarget = hasLiveTarget
                    && ProtectedAllyPolicy.isProtectedAlly(player.getUUID(), target);
            RallyCombatPolicy.Decision combatDecision = RallyCombatPolicy.decide(
                    hasLiveTarget,
                    protectedTarget,
                    RallyConfig.rallyRetreatEnabled(),
                    guard.distanceToSqr(player),
                    RallyConfig.rallyRetreatDistance()
            );
            if (combatDecision == RallyCombatPolicy.Decision.CONTINUE_COMBAT) continue;

            GuardCombatState.disengage(guard);
            if (!RallyConfig.rallyFormationEnabled()) {
                guard.setPatrolling(false);
                guard.setFollowing(true);
                continue;
            }

            Vec3 baseSlot = RallyFormationSlots.safeFormationSlot(
                    world,
                    player,
                    anchor.formation(),
                    i,
                    guards.size(),
                    anchor.heading().yawDegrees(),
                    anchor.inFront()
            );
            double baseDistance = guard.distanceToSqr(baseSlot.x, baseSlot.y, baseSlot.z);
            FormationMovementPlan plan = FormationMovementPolicy.plan(
                    anchor.moving(),
                    player.isSprinting(),
                    baseDistance,
                    RallyConfig.formationReturnSpeed()
            );
            guard.setFollowing(false);
            guard.setPatrolling(false);

            double teleportDistance = RallyConfig.formationTeleportDistance();
            if (RallyConfig.rallyTeleportEnabled() && baseDistance > teleportDistance * teleportDistance) {
                guard.snapTo(baseSlot.x, baseSlot.y, baseSlot.z, guard.getYRot(), guard.getXRot());
                guard.setDeltaMovement(0.0, 0.0, 0.0);
                guard.getNavigation().stop();
                continue;
            }

            if (anchor.moving() && baseDistance <= DIRECT_CONTROL_DISTANCE_SQUARED) {
                moveWithFormation(guard, anchor.movement(), baseSlot);
            } else if (plan.navigate()) {
                guard.getNavigation().moveTo(baseSlot.x, baseSlot.y, baseSlot.z, plan.speed());
            } else {
                holdSlot(guard, player);
            }
        }
    }

    private static void holdSlot(GuardEntity guard, ServerPlayer player) {
        guard.getNavigation().stop();
        guard.setDeltaMovement(0.0, guard.getDeltaMovement().y, 0.0);
        guard.lookAt(player, 30.0F, 30.0F);
    }

    private static FormationAnchor anchorFor(ServerPlayer player) {
        Vec3 current = player.position();
        FormationAnchor anchor = ANCHORS.get(player.getUUID());
        if (anchor == null) {
            anchor = new FormationAnchor(
                    current,
                    FormationHeadingState.initial(player.getYRot()),
                    true,
                    RallyFormation.ESCORT,
                    new Vec3(0.0, 0.0, 0.0),
                    false
            );
            ANCHORS.put(player.getUUID(), anchor);
            return anchor;
        }

        Vec3 movement = new Vec3(
                current.x - anchor.position().x,
                0.0,
                current.z - anchor.position().z
        );
        boolean moving = movement.horizontalDistanceSqr() > COMMANDER_MOVEMENT_SQUARED;
        FormationHeadingState heading = FormationHeadingPolicy.update(
                anchor.heading(),
                movement.x,
                movement.z,
                RallyConfig.formationHeadingMargin()
        );
        FormationAnchor updated = new FormationAnchor(
                current,
                heading,
                moving ? false : anchor.inFront(),
                anchor.formation(),
                movement,
                moving
        );
        ANCHORS.put(player.getUUID(), updated);
        return updated;
    }

    private static void moveWithFormation(GuardEntity guard, Vec3 commanderMovement, Vec3 slot) {
        FormationVelocity velocity = FormationVelocityPolicy.velocity(
                commanderMovement.x,
                commanderMovement.z,
                slot.x - guard.getX(),
                slot.z - guard.getZ()
        );
        guard.getNavigation().stop();
        guard.setDeltaMovement(velocity.x(), guard.getDeltaMovement().y, velocity.z());
        float yaw = FormationVelocityPolicy.facingYaw(guard.getYRot(), velocity);
        guard.setYRot(yaw);
        guard.setYBodyRot(yaw);
        guard.setYHeadRot(yaw);
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
