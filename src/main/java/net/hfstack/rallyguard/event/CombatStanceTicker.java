package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hfstack.rallyguard.combat.GuardCombatState;
import net.hfstack.rallyguard.combat.ProtectedAllyPolicy;
import net.hfstack.rallyguard.combat.stance.CombatStancePolicy;
import net.hfstack.rallyguard.combat.stance.CombatStanceRead;
import net.hfstack.rallyguard.combat.stance.CombatStanceState;
import net.hfstack.rallyguard.combat.stance.CombatStanceTargetPolicy;
import net.hfstack.rallyguard.combat.stance.CombatStances;
import net.hfstack.rallyguard.combat.stance.GuardCombatStance;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;

import java.util.List;
import java.util.UUID;

public final class CombatStanceTicker {
    private static final Identifier GUARD_ID = Identifier.fromNamespaceAndPath("guardvillagers", "guard");
    private static final int TICK_INTERVAL = 5;

    private CombatStanceTicker() {
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
                        && CombatStances.current(guard) != GuardCombatStance.DEFENSIVE
        );
        for (Entity entity : guards) {
            if (entity instanceof GuardEntity guard) tickGuard(world, guard);
        }
    }

    private static void tickGuard(ServerLevel world, GuardEntity guard) {
        CombatStanceRead read = CombatStances.read(guard);
        if (!(read instanceof CombatStanceRead.Available available)) return;
        CombatStanceState state = available.state();

        switch (state.stance()) {
            case DEFENSIVE -> {
            }
            case PASSIVE -> tickPassive(guard);
            case HOLD_POSITION -> state.anchor().ifPresent(anchor -> tickHoldPosition(guard, anchor));
            case AGGRESSIVE -> tickAggressive(world, guard);
            case PROTECT_COMMANDER -> tickProtectCommander(world, guard);
        }
    }

    private static void tickPassive(GuardEntity guard) {
        if (guard.getTarget() != null || guard.isAggressive() || guard.isUsingItem()) {
            GuardCombatState.disengage(guard);
        }
    }

    private static void tickHoldPosition(GuardEntity guard, BlockPos anchor) {
        int radius = RallyConfig.combatStanceHoldRadius();
        LivingEntity target = guard.getTarget();
        boolean liveTarget = target != null && target.isAlive();
        boolean targetWithinRadius = liveTarget && CombatStancePolicy.withinRadius(
                anchor, target.getX(), target.getY(), target.getZ(), radius
        );
        boolean beyondReturn = CombatStancePolicy.beyondReturnBoundary(
                anchor, guard.getX(), guard.getY(), guard.getZ(), radius
        );

        switch (CombatStancePolicy.decide(
                GuardCombatStance.HOLD_POSITION,
                liveTarget,
                targetWithinRadius,
                beyondReturn
        )) {
            case NONE -> {
            }
            case DISENGAGE -> GuardCombatState.disengage(guard);
            case RETURN_TO_ANCHOR -> returnToAnchor(guard, anchor);
            case DISENGAGE_AND_RETURN -> {
                GuardCombatState.disengage(guard);
                returnToAnchor(guard, anchor);
            }
        }
    }

    private static void tickAggressive(ServerLevel world, GuardEntity guard) {
        if (hasLiveTarget(guard)) return;
        double radius = RallyConfig.combatStanceAggressiveRadius();
        Monster target = selectTarget(
                guard,
                world.getEntitiesOfClass(Monster.class, guard.getBoundingBox().inflate(radius), candidate -> true)
        );
        if (target != null) guard.setTarget(target);
    }

    private static void tickProtectCommander(ServerLevel world, GuardEntity guard) {
        UUID ownerId = GuardOwnership.getOwner(guard);
        Entity ownerEntity = ownerId == null ? null : world.getEntity(ownerId);
        if (!(ownerEntity instanceof LivingEntity owner) || !owner.isAlive()) return;

        double radius = RallyConfig.combatStanceProtectRadius();
        Mob target = selectTarget(
                owner,
                world.getEntitiesOfClass(Mob.class, owner.getBoundingBox().inflate(radius), candidate ->
                        candidate != guard && candidate.getTarget() == owner
                ),
                ownerId
        );
        if (target != null && guard.getTarget() != target) guard.setTarget(target);
    }

    private static <T extends LivingEntity> T selectTarget(LivingEntity origin, List<T> candidates) {
        UUID ownerId = GuardOwnership.getOwner(origin);
        return selectTarget(origin, candidates, ownerId);
    }

    private static <T extends LivingEntity> T selectTarget(
            LivingEntity origin,
            List<T> candidates,
            UUID ownerId
    ) {
        T selected = null;
        double selectedDistance = Double.POSITIVE_INFINITY;
        for (T candidate : candidates) {
            boolean protectedAlly = ownerId != null && ProtectedAllyPolicy.isProtectedAlly(ownerId, candidate);
            if (!CombatStanceTargetPolicy.canAcquire(candidate.isAlive(), protectedAlly)) continue;
            double distance = origin.distanceToSqr(candidate);
            if (selected == null || CombatStanceTargetPolicy.preferCandidate(
                    distance, candidate.getUUID(), selectedDistance, selected.getUUID()
            )) {
                selected = candidate;
                selectedDistance = distance;
            }
        }
        return selected;
    }

    private static boolean hasLiveTarget(GuardEntity guard) {
        return guard.getTarget() != null && guard.getTarget().isAlive();
    }

    private static void returnToAnchor(GuardEntity guard, BlockPos anchor) {
        guard.getNavigation().moveTo(
                anchor.getX() + 0.5,
                anchor.getY(),
                anchor.getZ() + 0.5,
                RallyConfig.routeMoveSpeed()
        );
    }
}
