package net.hfstack.rallyguard.recruitment;

import net.hfstack.rallyguard.profile.GuardLevelModel;
import net.hfstack.rallyguard.profile.GuardProfile;
import net.hfstack.rallyguard.profile.GuardProfileOrigin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardHiringValueModelTest {
    @Test
    void reachesEveryDocumentedRankBandBoundary() {
        assertEquals(1, cost(1, 0));
        assertEquals(3, cost(2, 10_000));
        assertEquals(2, cost(3, 0));
        assertEquals(4, cost(4, 10_000));
        assertEquals(4, cost(5, 0));
        assertEquals(6, cost(6, 10_000));
        assertEquals(6, cost(7, 0));
        assertEquals(9, cost(8, 10_000));
        assertEquals(9, cost(9, 0));
        assertEquals(13, cost(10, 10_000));
    }

    @Test
    void usesHalfUpRoundingAndAppliesMultiplierBeforeClamp() {
        assertEquals(2, cost(1, 5_000));
        assertEquals(3, GuardHiringValueModel.cost(profile(1, 5_000), 2.0, 4, 1, 999));
        assertEquals(3, GuardHiringValueModel.cost(profile(1, 0), 0.1, 4, 3, 8));
        assertEquals(8, GuardHiringValueModel.cost(profile(10, 10_000), 10.0, 4, 3, 8));
    }

    @Test
    void captainBonusAppliesAfterMultiplierAndBeforeFinalClamp() {
        assertEquals(4, GuardHiringValueModel.cost(profile(5, 0, false), 1.0, 4, 1, 999));
        assertEquals(8, GuardHiringValueModel.cost(profile(5, 0, true), 1.0, 4, 1, 999));
        assertEquals(12, GuardHiringValueModel.cost(profile(5, 0, true), 2.0, 4, 1, 999));
        assertEquals(10, GuardHiringValueModel.cost(profile(5, 0, true), 2.0, 4, 1, 10));
    }

    @Test
    void captainBonusCannotOverflowOrLowerThePrice() {
        assertEquals(999, GuardHiringValueModel.cost(
                profile(10, 10_000, true),
                100.0,
                Integer.MAX_VALUE,
                1,
                999
        ));
        assertEquals(13, GuardHiringValueModel.cost(
                profile(10, 10_000, true),
                1.0,
                -20,
                1,
                999
        ));
    }

    @Test
    void priceNeverFallsWhenLevelOrQualityRisesIndependently() {
        for (int quality = 0; quality <= 10_000; quality += 500) {
            int previous = 0;
            for (int level = 1; level <= GuardLevelModel.MAX_LEVEL; level++) {
                int current = cost(level, quality);
                assertTrue(current >= previous, "level=" + level + ", quality=" + quality);
                previous = current;
            }
        }
        for (int level = 1; level <= GuardLevelModel.MAX_LEVEL; level++) {
            int previous = 0;
            for (int quality = 0; quality <= 10_000; quality += 500) {
                int current = cost(level, quality);
                assertTrue(current >= previous, "level=" + level + ", quality=" + quality);
                previous = current;
            }
        }
    }

    private static int cost(int level, int quality) {
        return GuardHiringValueModel.cost(profile(level, quality), 1.0, 4, 1, 999);
    }

    private static GuardProfile profile(int level, int quality) {
        return profile(level, quality, false);
    }

    private static GuardProfile profile(int level, int quality, boolean captain) {
        return GuardProfile.createV2(
                GuardProfileOrigin.NATURAL,
                GuardLevelModel.cumulativeXpMilli(level),
                quality,
                0,
                0,
                0,
                0,
                0,
                0,
                captain
        );
    }
}
