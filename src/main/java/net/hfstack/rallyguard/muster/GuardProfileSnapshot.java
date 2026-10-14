package net.hfstack.rallyguard.muster;

import net.hfstack.rallyguard.api.profile.GuardRank;
import net.hfstack.rallyguard.api.recruitment.RecruitmentOffer;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record GuardProfileSnapshot(
        int entityId,
        UUID entityUuid,
        String displayName,
        int level,
        GuardRank rank,
        Optional<String> rankOverride,
        boolean captain,
        int stars,
        long xpIntoLevelMilli,
        long xpForNextLevelMilli,
        double maxHealth,
        double attackDamage,
        double armor,
        double movementSpeed,
        long kills,
        long assists,
        GuardServiceStatus serviceStatus,
        Optional<RecruitmentOffer> hiringOffer
) {
    public static final int MAX_DISPLAY_NAME_LENGTH = 128;
    public static final int MAX_RANK_OVERRIDE_LENGTH = 64;

    public GuardProfileSnapshot {
        Objects.requireNonNull(entityUuid, "entityUuid");
        displayName = bounded(displayName, MAX_DISPLAY_NAME_LENGTH);
        level = Math.max(1, Math.min(10, level));
        Objects.requireNonNull(rank, "rank");
        rankOverride = rankOverride == null ? Optional.empty() : rankOverride
                .map(value -> bounded(value, MAX_RANK_OVERRIDE_LENGTH))
                .filter(value -> !value.isBlank());
        stars = Math.max(1, Math.min(5, stars));
        xpIntoLevelMilli = Math.max(0, xpIntoLevelMilli);
        xpForNextLevelMilli = Math.max(0, xpForNextLevelMilli);
        maxHealth = finiteNonNegative(maxHealth);
        attackDamage = finiteNonNegative(attackDamage);
        armor = finiteNonNegative(armor);
        movementSpeed = finiteNonNegative(movementSpeed);
        kills = Math.max(0, kills);
        assists = Math.max(0, assists);
        Objects.requireNonNull(serviceStatus, "serviceStatus");
        hiringOffer = hiringOffer == null ? Optional.empty() : hiringOffer;
    }

    private static String bounded(String value, int maximumLength) {
        String safe = Objects.requireNonNullElse(value, "");
        return safe.length() <= maximumLength ? safe : safe.substring(0, maximumLength);
    }

    private static double finiteNonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }
}
