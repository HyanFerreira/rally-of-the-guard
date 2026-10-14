package net.hfstack.rallyguard.api.recruitment;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.profile.GuardProfileView;

import java.util.Objects;
import java.util.Optional;

public record HiringValueCalculatedContext(
        GuardEntity guard,
        Optional<GuardProfileView> profile,
        boolean dynamic,
        RecruitmentOffer offer
) {
    public HiringValueCalculatedContext {
        Objects.requireNonNull(guard, "guard");
        profile = profile == null ? Optional.empty() : profile;
        Objects.requireNonNull(offer, "offer");
    }
}
