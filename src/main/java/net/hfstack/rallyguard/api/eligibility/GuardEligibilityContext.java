package net.hfstack.rallyguard.api.eligibility;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.command.GuardCommandType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable server-side facts used by guard eligibility policies.
 */
public record GuardEligibilityContext(
        ServerPlayer player,
        GuardEntity guard,
        GuardEligibilityOperation operation,
        ServerLevel world,
        GuardCommandType command,
        BlockPos requestedPosition
) {
    public GuardEligibilityContext {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(world, "world");
        if (operation == GuardEligibilityOperation.ISSUE_COMMAND && command == null) {
            throw new IllegalArgumentException("Command eligibility requires a command type");
        }
        if (operation != GuardEligibilityOperation.ISSUE_COMMAND && command != null) {
            throw new IllegalArgumentException("Only command eligibility accepts a command type");
        }
        requestedPosition = requestedPosition == null ? null : requestedPosition;
    }

    public static GuardEligibilityContext of(
            ServerPlayer player,
            GuardEntity guard,
            GuardEligibilityOperation operation
    ) {
        return new GuardEligibilityContext(player, guard, operation, player.level(), null, null);
    }

    public static GuardEligibilityContext command(
            ServerPlayer player,
            GuardEntity guard,
            GuardCommandType command,
            BlockPos requestedPosition
    ) {
        return new GuardEligibilityContext(
                player,
                guard,
                GuardEligibilityOperation.ISSUE_COMMAND,
                player.level(),
                command,
                requestedPosition
        );
    }

    public Optional<GuardCommandType> commandType() {
        return Optional.ofNullable(command);
    }

    public Optional<BlockPos> targetPosition() {
        return Optional.ofNullable(requestedPosition);
    }
}
