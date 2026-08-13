package net.hfstack.rallyguard.service;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.command.GuardCommandResult;
import net.hfstack.rallyguard.api.command.GuardCommandService;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.GuardRoutes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public final class DefaultGuardCommandService implements GuardCommandService {
    @Override
    public GuardCommandResult summon(ServerPlayerEntity commander, GuardEntity guard) {
        GuardCommandResult rejection = validate(requireCommander(commander), guard);
        if (rejection != null) {
            return rejection;
        }

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
        return GuardCommandResult.success("gui.rallyguard.command.summoned");
    }

    @Override
    public GuardCommandResult follow(ServerPlayerEntity commander, GuardEntity guard) {
        GuardCommandResult rejection = validate(requireCommander(commander), guard);
        if (rejection != null) {
            return rejection;
        }

        guard.setPatrolling(false);
        guard.setPatrolPos(null);
        GuardOrders.setRallied(guard, false);
        GuardOrders.setWaiting(guard, false);
        GuardRoutes.deactivate(guard);
        stopCurrentActions(guard);
        guard.setFollowing(true);
        return GuardCommandResult.success("gui.rallyguard.command.follow_on");
    }

    @Override
    public GuardCommandResult wait(ServerPlayerEntity commander, GuardEntity guard) {
        GuardCommandResult rejection = validate(requireCommander(commander), guard);
        if (rejection != null) {
            return rejection;
        }

        guard.setFollowing(false);
        guard.setPatrolling(false);
        guard.setPatrolPos(null);
        GuardOrders.setRallied(guard, false);
        GuardOrders.setWaiting(guard, true);
        GuardRoutes.deactivate(guard);
        stopCurrentActions(guard);
        return GuardCommandResult.success("gui.rallyguard.command.wait_on");
    }

    @Override
    public GuardCommandResult patrol(ServerPlayerEntity commander, GuardEntity guard, BlockPos position) {
        GuardCommandResult rejection = validate(requireCommander(commander), guard);
        if (rejection != null) {
            return rejection;
        }

        BlockPos patrolPosition = Objects.requireNonNull(position, "position").toImmutable();

        guard.setFollowing(false);
        GuardOrders.setRallied(guard, false);
        GuardOrders.setWaiting(guard, false);
        GuardRoutes.deactivate(guard);
        guard.setPatrolPos(patrolPosition);
        guard.setPatrolling(true);
        stopCurrentActions(guard);
        return GuardCommandResult.success("gui.rallyguard.command.patrol_on");
    }

    @Override
    public GuardCommandResult stopPatrol(ServerPlayerEntity commander, GuardEntity guard) {
        GuardCommandResult rejection = validate(requireCommander(commander), guard);
        if (rejection != null) {
            return rejection;
        }

        guard.setPatrolling(false);
        guard.setFollowing(false);
        guard.setPatrolPos(null);
        GuardOrders.setRallied(guard, false);
        GuardOrders.setWaiting(guard, true);
        GuardRoutes.deactivate(guard);
        stopCurrentActions(guard);
        return GuardCommandResult.success("gui.rallyguard.command.patrol_off");
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
