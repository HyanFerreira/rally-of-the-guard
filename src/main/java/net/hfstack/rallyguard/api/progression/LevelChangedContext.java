package net.hfstack.rallyguard.api.progression;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.profile.GuardProfileView;

import java.util.Objects;

public record LevelChangedContext(
        GuardEntity guard,
        GuardProfileView profile,
        int previousLevel,
        int currentLevel
) {
    public LevelChangedContext {
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(profile, "profile");
    }
}
