package net.hfstack.rallyguard.api.progression;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.profile.GuardProfileView;

import java.util.Objects;

public record XpAwardedContext(GuardEntity guard, GuardProfileView profile, long awardedXpMilli) {
    public XpAwardedContext {
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(profile, "profile");
        awardedXpMilli = Math.max(0, awardedXpMilli);
    }
}
