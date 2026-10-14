package net.hfstack.rallyguard.profile;

public final class NaturalCaptainPolicy {
    private NaturalCaptainPolicy() {
    }

    public static double adjustedChance(
            boolean enabled,
            double baseChance,
            int nearbyCaptains,
            int regionalLimit
    ) {
        if (!enabled || !Double.isFinite(baseChance)) return 0.0;
        int safeLimit = Math.max(0, regionalLimit);
        int safeNearby = Math.max(0, nearbyCaptains);
        if (safeLimit == 0 || safeNearby >= safeLimit) return 0.0;
        double safeChance = Math.max(0.0, Math.min(1.0, baseChance));
        return safeChance / (safeNearby + 1.0);
    }

    public static boolean isSelected(double roll, double chance) {
        return Double.isFinite(roll)
                && roll >= 0.0
                && roll < Math.max(0.0, Math.min(1.0, chance));
    }

    public static int minimumLevel(boolean captain, int rolledLevel, int configuredMinimum) {
        int safeRolledLevel = Math.max(1, Math.min(GuardLevelModel.MAX_LEVEL, rolledLevel));
        if (!captain) return safeRolledLevel;
        int safeMinimum = Math.max(1, Math.min(GuardLevelModel.MAX_LEVEL, configuredMinimum));
        return Math.max(safeRolledLevel, safeMinimum);
    }
}
