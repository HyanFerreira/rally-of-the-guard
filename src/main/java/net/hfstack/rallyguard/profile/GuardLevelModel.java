package net.hfstack.rallyguard.profile;

import net.hfstack.rallyguard.api.profile.GuardRank;

public final class GuardLevelModel {
    public static final int MAX_LEVEL = 10;
    private static final long[] CUMULATIVE_XP_MILLI = {
            0,
            0,
            20_000,
            50_000,
            95_000,
            155_000,
            235_000,
            340_000,
            475_000,
            645_000,
            855_000
    };
    private static final int[] NATURAL_LEVEL_WEIGHTS = {450, 250, 140, 80, 40, 20, 10, 6, 3, 1};
    private static final double[] HEALTH_BONUSES = {0, 0, 1, 1, 2, 2, 3, 4, 5, 6, 8};
    private static final double[] ATTACK_BONUSES = {0, 0, 0, 0.25, 0.25, 0.50, 0.75, 1.00, 1.25, 1.50, 2.00};
    private static final double[] ARMOR_BONUSES = {0, 0, 0, 0, 1, 1, 1, 2, 2, 3, 3};

    private GuardLevelModel() {
    }

    public static int level(long totalXpMilli) {
        long xp = Math.max(0, totalXpMilli);
        for (int level = MAX_LEVEL; level >= 2; level--) {
            if (xp >= CUMULATIVE_XP_MILLI[level]) {
                return level;
            }
        }
        return 1;
    }

    public static GuardRank rank(int level) {
        int bounded = Math.max(1, Math.min(MAX_LEVEL, level));
        return switch ((bounded - 1) / 2) {
            case 0 -> GuardRank.RECRUIT;
            case 1 -> GuardRank.GUARDSMAN;
            case 2 -> GuardRank.SOLDIER;
            case 3 -> GuardRank.VETERAN;
            default -> GuardRank.ELITE;
        };
    }

    public static long cumulativeXpMilli(int level) {
        return CUMULATIVE_XP_MILLI[Math.max(1, Math.min(MAX_LEVEL, level))];
    }

    public static long xpIntoLevelMilli(long totalXpMilli) {
        int level = level(totalXpMilli);
        return Math.max(0, totalXpMilli) - cumulativeXpMilli(level);
    }

    public static long xpForNextLevelMilli(int level) {
        int bounded = Math.max(1, Math.min(MAX_LEVEL, level));
        if (bounded == MAX_LEVEL) {
            return 0;
        }
        return cumulativeXpMilli(bounded + 1) - cumulativeXpMilli(bounded);
    }

    public static int naturalLevel(int roll) {
        if (roll < 0 || roll >= 1_000) {
            throw new IllegalArgumentException("Natural level roll must be between 0 and 999");
        }
        int cumulative = 0;
        for (int index = 0; index < NATURAL_LEVEL_WEIGHTS.length; index++) {
            cumulative += NATURAL_LEVEL_WEIGHTS[index];
            if (roll < cumulative) {
                return index + 1;
            }
        }
        throw new IllegalStateException("Natural level weights must total 1000");
    }

    public static double healthBonus(int level) {
        return HEALTH_BONUSES[boundedLevel(level)];
    }

    public static double attackBonus(int level) {
        return ATTACK_BONUSES[boundedLevel(level)];
    }

    public static double armorBonus(int level) {
        return ARMOR_BONUSES[boundedLevel(level)];
    }

    private static int boundedLevel(int level) {
        return Math.max(1, Math.min(MAX_LEVEL, level));
    }
}
