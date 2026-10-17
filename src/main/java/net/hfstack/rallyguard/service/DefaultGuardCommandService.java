package net.hfstack.rallyguard.service;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.command.GuardCommandContext;
import net.hfstack.rallyguard.api.command.GuardCommandDecision;
import net.hfstack.rallyguard.api.command.GuardCommandEvents;
import net.hfstack.rallyguard.api.command.GuardCommandResult;
import net.hfstack.rallyguard.api.command.GuardCommandService;
import net.hfstack.rallyguard.api.command.GuardCommandType;
import net.hfstack.rallyguard.api.eligibility.GuardEligibility;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityContext;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityDecision;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.combat.GuardCombatState;
import net.hfstack.rallyguard.combat.stance.CombatStanceRead;
import net.hfstack.rallyguard.combat.stance.CombatStances;
import net.hfstack.rallyguard.api.command.GuardCombatStance;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.GuardRoutes;
import net.hfstack.rallyguard.order.move.MoveOrderRead;
import net.hfstack.rallyguard.order.move.MoveOrders;
import net.hfstack.rallyguard.order.defend.DefendAreaRead;
import net.hfstack.rallyguard.order.defend.DefendAreaState;
import net.hfstack.rallyguard.order.defend.DefendAreas;
import net.hfstack.rallyguard.order.temporary.GuardTaskSuspensions;
import net.hfstack.rallyguard.route.DefaultNamedRouteService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;

import java.util.Objects;

public final class DefaultGuardCommandService implements GuardCommandService {
    @Override
    public GuardCommandResult summon(ServerPlayer commander, GuardEntity guard) {
        return execute(commander, guard, GuardCommandType.SUMMON, null, () -> {
            GuardTaskSuspensions.suspendIfAbsent(guard);
            MoveOrders.clear(guard);
            DefendAreas.clear(guard);
            guard.setPatrolling(false);
            guard.setPatrolPos(null);
            double offsetX = (commander.getRandom().nextDouble() - 0.5) * 2.5;
            double offsetZ = (commander.getRandom().nextDouble() - 0.5) * 2.5;
            guard.snapTo(
                    commander.getX() + offsetX,
                    commander.getY(),
                    commander.getZ() + offsetZ,
                    guard.getYRot(),
                    guard.getXRot()
            );
            guard.setFollowing(false);
            GuardOrders.setRallied(guard, false);
            GuardOrders.setWaiting(guard, false);
            GuardRoutes.deactivate(guard);
            stopCurrentActions(guard);
        }, "gui.rallyguard.command.summoned");
    }

    @Override
    public GuardCommandResult follow(ServerPlayer commander, GuardEntity guard) {
        return execute(commander, guard, GuardCommandType.FOLLOW, null, () -> {
            GuardTaskSuspensions.suspendIfAbsent(guard);
            MoveOrders.clear(guard);
            DefendAreas.clear(guard);
            guard.setPatrolling(false);
            guard.setPatrolPos(null);
            GuardOrders.setRallied(guard, false);
            GuardOrders.setWaiting(guard, false);
            GuardRoutes.deactivate(guard);
            stopCurrentActions(guard);
            guard.setFollowing(true);
        }, "gui.rallyguard.command.follow_on");
    }

    @Override
    public GuardCommandResult wait(ServerPlayer commander, GuardEntity guard) {
        return execute(commander, guard, GuardCommandType.WAIT, null, () -> {
            GuardTaskSuspensions.discard(guard);
            MoveOrders.clear(guard);
            DefendAreas.clear(guard);
            guard.setFollowing(false);
            guard.setPatrolling(false);
            guard.setPatrolPos(null);
            GuardOrders.setRallied(guard, false);
            GuardOrders.setWaiting(guard, true);
            GuardRoutes.deactivate(guard);
            stopCurrentActions(guard);
        }, "gui.rallyguard.command.wait_on");
    }

    @Override
    public GuardCommandResult dismiss(ServerPlayer commander, GuardEntity guard) {
        ServerPlayer requiredCommander = requireCommander(commander);
        GuardCommandResult rejection = validate(requiredCommander, guard);
        if (rejection != null) return rejection;
        if (!GuardTaskSuspensions.hasSuspendedTask(guard)) return GuardCommandResult.nothingToDismiss();
        return executeValidated(requiredCommander, guard, GuardCommandType.DISMISS, null, () -> {
            if (!GuardTaskSuspensions.restore(guard)) {
                throw new IllegalStateException("Suspended guard task disappeared during dismissal");
            }
            DefaultNamedRouteService.instance().repairMissingAssignment(requiredCommander, guard);
        }, "gui.rallyguard.command.dismissed");
    }

    @Override
    public GuardCommandResult patrol(ServerPlayer commander, GuardEntity guard, BlockPos position) {
        BlockPos patrolPosition = Objects.requireNonNull(position, "position");
        return execute(commander, guard, GuardCommandType.PATROL, patrolPosition, () -> {
            GuardTaskSuspensions.discard(guard);
            MoveOrders.clear(guard);
            DefendAreas.clear(guard);
            guard.setFollowing(false);
            GuardOrders.setRallied(guard, false);
            GuardOrders.setWaiting(guard, false);
            GuardRoutes.deactivate(guard);
            guard.setPatrolPos(patrolPosition);
            guard.setPatrolling(true);
            stopCurrentActions(guard);
        }, "gui.rallyguard.command.patrol_on");
    }

    @Override
    public GuardCommandResult stopPatrol(ServerPlayer commander, GuardEntity guard) {
        return execute(commander, guard, GuardCommandType.STOP_PATROL, null, () -> {
            GuardTaskSuspensions.discard(guard);
            MoveOrders.clear(guard);
            DefendAreas.clear(guard);
            guard.setPatrolling(false);
            guard.setFollowing(false);
            guard.setPatrolPos(null);
            GuardOrders.setRallied(guard, false);
            GuardOrders.setWaiting(guard, true);
            GuardRoutes.deactivate(guard);
            stopCurrentActions(guard);
        }, "gui.rallyguard.command.patrol_off");
    }

    @Override
    public GuardCommandResult moveTo(ServerPlayer commander, GuardEntity guard, BlockPos position) {
        ServerPlayer requiredCommander = requireCommander(commander);
        GuardCommandResult rejection = validate(requiredCommander, guard);
        if (rejection != null) return rejection;
        if (MoveOrders.read(guard) instanceof MoveOrderRead.Unsupported) {
            return GuardCommandResult.unsupportedData();
        }

        BlockPos target = Objects.requireNonNull(position, "position").immutable();
        return executeValidated(requiredCommander, guard, GuardCommandType.MOVE_TO, target, () -> {
            GuardTaskSuspensions.suspendIfAbsent(guard);
            DefendAreas.clear(guard);
            guard.setFollowing(false);
            guard.setPatrolling(false);
            guard.setPatrolPos(null);
            GuardOrders.setRallied(guard, false);
            GuardOrders.setWaiting(guard, false);
            GuardRoutes.deactivate(guard);
            stopCurrentActions(guard);
            MoveOrders.set(guard, target);
        }, "gui.rallyguard.command.move_ordered");
    }

    @Override
    public GuardCommandResult defendArea(
            ServerPlayer commander,
            GuardEntity guard,
            BlockPos center,
            BlockPos post,
            int radius
    ) {
        ServerPlayer requiredCommander = requireCommander(commander);
        GuardCommandResult rejection = validate(requiredCommander, guard);
        if (rejection != null) return rejection;
        if (DefendAreas.read(guard) instanceof DefendAreaRead.Unsupported) {
            return GuardCommandResult.unsupportedData();
        }

        DefendAreaState order = DefendAreaState.active(center, post, radius);
        return executeValidated(requiredCommander, guard, GuardCommandType.DEFEND_AREA, order.center(), () -> {
            GuardTaskSuspensions.suspendIfAbsent(guard);
            MoveOrders.clear(guard);
            guard.setFollowing(false);
            GuardOrders.setRallied(guard, false);
            GuardOrders.setWaiting(guard, false);
            GuardRoutes.deactivate(guard);
            stopCurrentActions(guard);
            guard.setPatrolPos(order.post());
            guard.setPatrolling(true);
            DefendAreas.set(guard, order.center(), order.post(), order.radius());
        }, "gui.rallyguard.command.defend_ordered");
    }

    @Override
    public GuardCommandResult setCombatStance(
            ServerPlayer commander,
            GuardEntity guard,
            GuardCombatStance stance
    ) {
        ServerPlayer requiredCommander = requireCommander(commander);
        GuardCombatStance requiredStance = Objects.requireNonNull(stance, "stance");
        GuardCommandResult rejection = validate(requiredCommander, guard);
        if (rejection != null) return rejection;
        if (CombatStances.read(guard) instanceof CombatStanceRead.Unsupported) {
            return GuardCommandResult.unsupportedData();
        }

        BlockPos anchor = requiredStance == GuardCombatStance.HOLD_POSITION
                ? guard.blockPosition().immutable()
                : null;
        return executeValidated(
                requiredCommander,
                guard,
                GuardCommandType.SET_COMBAT_STANCE,
                anchor,
                () -> {
                    CombatStances.set(guard, requiredStance, anchor);
                    if (requiredStance == GuardCombatStance.PASSIVE) {
                        GuardCombatState.disengage(guard);
                    }
                },
                "gui.rallyguard.stance.changed"
        );
    }

    private static GuardCommandResult execute(
            ServerPlayer commander,
            GuardEntity guard,
            GuardCommandType command,
            BlockPos requestedPosition,
            Runnable action,
            String successTranslationKey
    ) {
        ServerPlayer requiredCommander = requireCommander(commander);
        GuardCommandResult rejection = validate(requiredCommander, guard);
        if (rejection != null) {
            return rejection;
        }

        return executeValidated(requiredCommander, guard, command, requestedPosition, action, successTranslationKey);
    }

    private static GuardCommandResult executeValidated(
            ServerPlayer requiredCommander,
            GuardEntity guard,
            GuardCommandType command,
            BlockPos requestedPosition,
            Runnable action,
            String successTranslationKey
    ) {

        GuardCommandContext context = new GuardCommandContext(
                requiredCommander,
                guard,
                command,
                requiredCommander.level(),
                guard.blockPosition(),
                requestedPosition
        );

        GuardEligibilityDecision eligibility = GuardEligibility.evaluate(
                GuardEligibilityContext.command(requiredCommander, guard, command, requestedPosition)
        );
        if (eligibility instanceof GuardEligibilityDecision.Deny denied) {
            return GuardCommandResult.denied(denied.reason());
        }

        GuardCommandDecision decision;
        try {
            decision = GuardCommandEvents.BEFORE.invoker().evaluate(context);
        } catch (RuntimeException exception) {
            RallyOfTheGuard.LOGGER.error(
                    "Guard command policy failed for command {} and guard {}",
                    command,
                    guard.getUUID(),
                    exception
            );
            return GuardCommandResult.policyError();
        }

        if (decision instanceof GuardCommandDecision.Deny denied) {
            return GuardCommandResult.denied(denied.reason());
        }

        action.run();
        GuardCommandResult result = GuardCommandResult.success(successTranslationKey);
        try {
            GuardCommandEvents.AFTER.invoker().onCommanded(context, result);
        } catch (RuntimeException exception) {
            RallyOfTheGuard.LOGGER.error(
                    "Guard command AFTER listener failed for command {} and guard {}",
                    command,
                    guard.getUUID(),
                    exception
            );
        }
        return result;
    }

    private static ServerPlayer requireCommander(ServerPlayer commander) {
        return Objects.requireNonNull(commander, "commander");
    }

    private static GuardCommandResult validate(ServerPlayer commander, GuardEntity guard) {
        if (guard == null || !guard.isAlive() || guard.level() != commander.level()) {
            return GuardCommandResult.invalidGuard();
        }
        if (!GuardOwnership.isOwnedBy(guard, commander.getUUID())) {
            return GuardCommandResult.notOwner();
        }
        return null;
    }

    private static void stopCurrentActions(GuardEntity guard) {
        guard.setTarget(null);
        guard.setAggressive(false);
        guard.getNavigation().stop();
    }
}
