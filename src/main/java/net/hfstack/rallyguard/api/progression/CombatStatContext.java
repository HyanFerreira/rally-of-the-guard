package net.hfstack.rallyguard.api.progression;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.profile.GuardProfileView;

import java.util.Objects;

public record CombatStatContext(GuardEntity guard, GuardProfileView profile, CombatStat stat) {
    public CombatStatContext {
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(stat, "stat");
    }
}
