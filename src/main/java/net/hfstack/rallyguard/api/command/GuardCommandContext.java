package net.hfstack.rallyguard.api.command;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable server-side facts for one validated tactical order attempt.
 */
public record GuardCommandContext(
        ServerPlayerEntity commander,
        GuardEntity guard,
        GuardCommandType command,
        ServerWorld world,
        BlockPos guardPosition,
        BlockPos requestedPosition
) {
    public GuardCommandContext {
        Objects.requireNonNull(commander, "commander");
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(world, "world");
        guardPosition = Objects.requireNonNull(guardPosition, "guardPosition").toImmutable();
        requestedPosition = requestedPosition == null ? null : requestedPosition.toImmutable();
    }

    /**
     * Target block for positional orders such as patrol, or empty for non-positional orders.
     */
    public Optional<BlockPos> targetPosition() {
        return Optional.ofNullable(requestedPosition);
    }
}
