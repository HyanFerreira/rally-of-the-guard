package net.hfstack.rallyguard.api.presentation;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

public record GuardPresentationContext(
        ServerPlayer viewer,
        GuardEntity guard,
        ServerLevel world
) {
    public GuardPresentationContext {
        Objects.requireNonNull(viewer, "viewer");
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(world, "world");
    }
}
