package net.hfstack.rallyguard.api.recruitment;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;

import java.util.Objects;

/**
 * Immutable server-side facts for one recruitment attempt.
 */
public record RecruitmentContext(
        ServerPlayer player,
        GuardEntity guard,
        Identifier defaultPaymentItemId,
        int defaultCost,
        ServerLevel world,
        BlockPos position
) {
    public RecruitmentContext {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(defaultPaymentItemId, "defaultPaymentItemId");
        Objects.requireNonNull(world, "world");
        position = Objects.requireNonNull(position, "position");
        if (defaultCost < 0) {
            throw new IllegalArgumentException("Default recruitment cost cannot be negative");
        }
    }

    public RecruitmentOffer defaultOffer() {
        return new RecruitmentOffer(defaultPaymentItemId, defaultCost);
    }
}
