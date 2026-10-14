package net.hfstack.rallyguard.recruitment;

import net.hfstack.rallyguard.api.profile.GuardProfileView;
import net.hfstack.rallyguard.api.profile.GuardRank;

import java.util.Objects;

public final class GuardHiringValueModel {
    private GuardHiringValueModel() {
    }

    public static int cost(
            GuardProfileView profile,
            double multiplier,
            int captainBonus,
            int configuredMinimum,
            int configuredMaximum
    ) {
        Objects.requireNonNull(profile, "profile");
        double safeMultiplier = Double.isFinite(multiplier) ? Math.max(0.0, multiplier) : 1.0;
        int minimum = Math.max(0, configuredMinimum);
        int maximum = Math.max(minimum, configuredMaximum);
        RankBand band = band(profile.rank());
        int levelPosition = Math.floorMod(Math.max(1, profile.level()) - 1, 2);
        double quality = Math.max(0, Math.min(10_000, profile.naturalQuality())) / 10_000.0;
        double progress = (levelPosition + quality) / 2.0;
        double raw = band.minimum() + (band.maximum() - band.minimum()) * progress;
        long rounded = raw * safeMultiplier >= Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : (long) Math.floor(raw * safeMultiplier + 0.5);
        long priced = profile.captain()
                ? Math.min(Integer.MAX_VALUE, rounded + Math.max(0L, captainBonus))
                : rounded;
        return (int) Math.max(minimum, Math.min(maximum, priced));
    }

    static RankBand band(GuardRank rank) {
        return switch (Objects.requireNonNull(rank, "rank")) {
            case RECRUIT -> new RankBand(1, 3);
            case GUARDSMAN -> new RankBand(2, 4);
            case SOLDIER -> new RankBand(4, 6);
            case VETERAN -> new RankBand(6, 9);
            case ELITE -> new RankBand(9, 13);
        };
    }

    record RankBand(int minimum, int maximum) {
    }
}
