package net.hfstack.rallyguard.api.command;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable server-side facts for one validated tactical order attempt.
 */
public record GuardCommandContext(
        ServerPlayer commander,
        GuardEntity guard,
        GuardCommandType command,
        ServerLevel world,
        BlockPos guardPosition,
        BlockPos requestedPosition
) {
    public GuardCommandContext {
        Objects.requireNonNull(commander, "commander");
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(world, "world");
        guardPosition = Objects.requireNonNull(guardPosition, "guardPosition");
        requestedPosition = requestedPosition == null ? null : requestedPosition;
    }

    /**
     * Target block for positional orders such as patrol, or empty for non-positional orders.
     */
    public Optional<BlockPos> targetPosition() {
        return Optional.ofNullable(requestedPosition);
    }
}
