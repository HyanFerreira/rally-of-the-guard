package net.hfstack.rallyguard.api.presentation;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Objects;

public record GuardPresentationContext(
        ServerPlayerEntity viewer,
        GuardEntity guard,
        ServerWorld world
) {
    public GuardPresentationContext {
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(world, "world");
    }
}
