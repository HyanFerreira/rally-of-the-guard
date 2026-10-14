package net.hfstack.rallyguard.api.progression;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.profile.GuardProfileView;

import java.util.Objects;

public record XpAwardContext(GuardEntity guard, GuardProfileView profile) {
    public XpAwardContext {
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(profile, "profile");
    }
}
