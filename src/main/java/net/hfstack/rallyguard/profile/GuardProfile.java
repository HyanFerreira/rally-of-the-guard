package net.hfstack.rallyguard.profile;

import net.hfstack.rallyguard.api.profile.GuardProfileView;

import java.util.Objects;

public record GuardProfile(
        int generationVersion,
        GuardProfileOrigin originType,
        long totalXpMilli,
        int naturalQuality,
        int healthRoll,
        int attackRoll,
        int armorRoll,
        int speedRoll,
        long kills,
        long assists,
        boolean captain
) implements GuardProfileView {
    public static final int CURRENT_SCHEMA_VERSION = 2;
    public static final int PROFILE_CORE_PLACEHOLDER_GENERATION_VERSION = 1;
    public static final int CURRENT_GENERATION_VERSION = 3;
    public static final int QUALITY_MIN = 0;
    public static final int QUALITY_MAX = 10_000;
    public static final int ROLL_MIN = -10_000;
    public static final int ROLL_MAX = 10_000;

    public GuardProfile {
        generationVersion = Math.max(PROFILE_CORE_PLACEHOLDER_GENERATION_VERSION, generationVersion);
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

    public static GuardProfile createV2(
            GuardProfileOrigin originType,
            long totalXpMilli,
            int naturalQuality,
            int healthRoll,
            int attackRoll,
            int armorRoll,
            int speedRoll,
            long kills,
            long assists,
            boolean captain
    ) {
        return new GuardProfile(
                CURRENT_GENERATION_VERSION,
                originType,
                totalXpMilli,
                naturalQuality,
                healthRoll,
                attackRoll,
                armorRoll,
                speedRoll,
                kills,
                assists,
                captain
        );
    }

    static GuardProfile restoreV2(
            int generationVersion,
            GuardProfileOrigin originType,
            long totalXpMilli,
            int naturalQuality,
            int healthRoll,
            int attackRoll,
            int armorRoll,
            int speedRoll,
            long kills,
            long assists,
            boolean captain
    ) {
        return new GuardProfile(
                generationVersion,
                originType,
                totalXpMilli,
                naturalQuality,
                healthRoll,
                attackRoll,
                armorRoll,
                speedRoll,
                kills,
                assists,
                captain
        );
    }

    public static GuardProfile legacyNeutral() {
        return restoreV2(
                PROFILE_CORE_PLACEHOLDER_GENERATION_VERSION,
                GuardProfileOrigin.LEGACY,
                0,
                5_000,
                0,
                0,
                0,
                0,
                0,
                0,
                false
        );
    }

    public GuardProfile withProgress(long xpAwardMilli, boolean recordKill, boolean recordAssist) {
        return restoreV2(
                generationVersion,
                originType,
                saturatedAdd(totalXpMilli, Math.max(0, xpAwardMilli)),
                naturalQuality,
                healthRoll,
                attackRoll,
                armorRoll,
                speedRoll,
                recordKill ? saturatedAdd(kills, 1) : kills,
                recordAssist ? saturatedAdd(assists, 1) : assists,
                captain
        );
    }

    public GuardProfile withCaptain(boolean captain) {
        return restoreV2(
                generationVersion,
                originType,
                totalXpMilli,
                naturalQuality,
                healthRoll,
                attackRoll,
                armorRoll,
                speedRoll,
                kills,
                assists,
                captain
        );
    }

    @Override
    public int schemaVersion() {
        return CURRENT_SCHEMA_VERSION;
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

    private static long saturatedAdd(long left, long right) {
        if (right > 0 && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }
}
