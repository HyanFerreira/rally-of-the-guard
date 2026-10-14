package net.hfstack.rallyguard.profile;

import net.hfstack.rallyguard.api.profile.GuardProfileView;

import java.util.Objects;

public record GuardProfile(
        GuardProfileOrigin originType,
        long totalXpMilli,
        int naturalQuality,
        int healthRoll,
        int attackRoll,
        int armorRoll,
        int speedRoll,
        long kills,
        long assists
) implements GuardProfileView {
    public static final int CURRENT_SCHEMA_VERSION = 1;
    public static final int CURRENT_GENERATION_VERSION = 1;
    public static final int QUALITY_MIN = 0;
    public static final int QUALITY_MAX = 10_000;
    public static final int ROLL_MIN = -10_000;
    public static final int ROLL_MAX = 10_000;

    public GuardProfile {
        originType = Objects.requireNonNull(originType, "originType");
        totalXpMilli = nonNegative(totalXpMilli);
        naturalQuality = clamp(naturalQuality, QUALITY_MIN, QUALITY_MAX);
        healthRoll = clamp(healthRoll, ROLL_MIN, ROLL_MAX);
        attackRoll = clamp(attackRoll, ROLL_MIN, ROLL_MAX);
        armorRoll = clamp(armorRoll, ROLL_MIN, ROLL_MAX);
        speedRoll = clamp(speedRoll, ROLL_MIN, ROLL_MAX);
        kills = nonNegative(kills);
        assists = nonNegative(assists);
    }

    public static GuardProfile createV1(
            GuardProfileOrigin originType,
            long totalXpMilli,
            int naturalQuality,
            int healthRoll,
            int attackRoll,
            int armorRoll,
            int speedRoll,
            long kills,
            long assists
    ) {
        return new GuardProfile(
                originType,
                totalXpMilli,
                naturalQuality,
                healthRoll,
                attackRoll,
                armorRoll,
                speedRoll,
                kills,
                assists
        );
    }

    public static GuardProfile legacyNeutral() {
        return createV1(GuardProfileOrigin.LEGACY, 0, 5_000, 0, 0, 0, 0, 0, 0);
    }

    @Override
    public int schemaVersion() {
        return CURRENT_SCHEMA_VERSION;
    }

    @Override
    public int generationVersion() {
        return CURRENT_GENERATION_VERSION;
    }

    @Override
    public String origin() {
        return originType.serializedName();
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static long nonNegative(long value) {
        return Math.max(0, value);
    }
}
