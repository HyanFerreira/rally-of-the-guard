package net.hfstack.rallyguard.api.recruitment;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

/**
 * Immutable server-side facts for one recruitment attempt.
 */
public record RecruitmentContext(
        ServerPlayerEntity player,
        GuardEntity guard,
        Identifier defaultPaymentItemId,
        int defaultCost,
        ServerWorld world,
        BlockPos position
) {
    public RecruitmentContext {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(defaultPaymentItemId, "defaultPaymentItemId");
        Objects.requireNonNull(world, "world");
        position = Objects.requireNonNull(position, "position").toImmutable();
        if (defaultCost < 0) {
            throw new IllegalArgumentException("Default recruitment cost cannot be negative");
        }
    }

    public RecruitmentOffer defaultOffer() {
        return new RecruitmentOffer(defaultPaymentItemId, defaultCost);
    }
}
