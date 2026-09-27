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
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.GuardRoutes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public final class DefaultGuardCommandService implements GuardCommandService {
    @Override
    public GuardCommandResult summon(ServerPlayerEntity commander, GuardEntity guard) {
        return execute(commander, guard, GuardCommandType.SUMMON, null, () -> {
            double offsetX = (commander.getRandom().nextDouble() - 0.5) * 2.5;
            double offsetZ = (commander.getRandom().nextDouble() - 0.5) * 2.5;
            guard.refreshPositionAndAngles(
                    commander.getX() + offsetX,
                    commander.getY(),
                    commander.getZ() + offsetZ,
                    guard.getYaw(),
                    guard.getPitch()
            );
            guard.setFollowing(false);
            GuardOrders.setRallied(guard, false);
            GuardOrders.setWaiting(guard, false);
            GuardRoutes.deactivate(guard);
            stopCurrentActions(guard);
        }, "gui.rallyguard.command.summoned");
    }

    @Override
    public GuardCommandResult follow(ServerPlayerEntity commander, GuardEntity guard) {
        return execute(commander, guard, GuardCommandType.FOLLOW, null, () -> {
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
    public GuardCommandResult wait(ServerPlayerEntity commander, GuardEntity guard) {
        return execute(commander, guard, GuardCommandType.WAIT, null, () -> {
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
    public GuardCommandResult patrol(ServerPlayerEntity commander, GuardEntity guard, BlockPos position) {
        BlockPos patrolPosition = Objects.requireNonNull(position, "position").toImmutable();
        return execute(commander, guard, GuardCommandType.PATROL, patrolPosition, () -> {
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
    public GuardCommandResult stopPatrol(ServerPlayerEntity commander, GuardEntity guard) {
        return execute(commander, guard, GuardCommandType.STOP_PATROL, null, () -> {
            guard.setPatrolling(false);
            guard.setFollowing(false);
            guard.setPatrolPos(null);
            GuardOrders.setRallied(guard, false);
            GuardOrders.setWaiting(guard, true);
            GuardRoutes.deactivate(guard);
            stopCurrentActions(guard);
        }, "gui.rallyguard.command.patrol_off");
    }

    private static GuardCommandResult execute(
            ServerPlayerEntity commander,
            GuardEntity guard,
            GuardCommandType command,
            BlockPos requestedPosition,
            Runnable action,
            String successTranslationKey
    ) {
        ServerPlayerEntity requiredCommander = requireCommander(commander);
        GuardCommandResult rejection = validate(requiredCommander, guard);
        if (rejection != null) {
            return rejection;
        }

        GuardCommandContext context = new GuardCommandContext(
                requiredCommander,
                guard,
                command,
                requiredCommander.getEntityWorld(),
                guard.getBlockPos(),
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
                    guard.getUuid(),
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
                    guard.getUuid(),
                    exception
            );
        }
        return result;
    }

    private static ServerPlayerEntity requireCommander(ServerPlayerEntity commander) {
        return Objects.requireNonNull(commander, "commander");
    }

    private static GuardCommandResult validate(ServerPlayerEntity commander, GuardEntity guard) {
        if (guard == null || !guard.isAlive() || guard.getEntityWorld() != commander.getEntityWorld()) {
            return GuardCommandResult.invalidGuard();
        }
        if (!GuardOwnership.isOwnedBy(guard, commander.getUuid())) {
            return GuardCommandResult.notOwner();
        }
        return null;
    }

    private static void stopCurrentActions(GuardEntity guard) {
        guard.setTarget(null);
        guard.setAttacking(false);
        guard.getNavigation().stop();
    }
}
