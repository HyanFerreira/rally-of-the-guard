package net.hfstack.rallyguard.profile;

import net.hfstack.rallyguard.api.profile.GuardRank;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GuardLevelModelTest {
    @Test
    void derivesEveryLevelAtItsExactThreshold() {
        long[] thresholds = {0, 20, 50, 95, 155, 235, 340, 475, 645, 855};
        for (int index = 0; index < thresholds.length; index++) {
            int expectedLevel = index + 1;
            long thresholdMilli = thresholds[index] * 1_000;
            assertEquals(expectedLevel, GuardLevelModel.level(thresholdMilli));
            assertEquals(thresholdMilli, GuardLevelModel.cumulativeXpMilli(expectedLevel));
            if (expectedLevel > 1) {
                assertEquals(expectedLevel - 1, GuardLevelModel.level(thresholdMilli - 1));
            }
        }
        assertEquals(10, GuardLevelModel.level(Long.MAX_VALUE));
    }

    @Test
    void mapsRankBoundaries() {
        assertEquals(GuardRank.RECRUIT, GuardLevelModel.rank(1));
        assertEquals(GuardRank.RECRUIT, GuardLevelModel.rank(2));
        assertEquals(GuardRank.GUARDSMAN, GuardLevelModel.rank(3));
        assertEquals(GuardRank.SOLDIER, GuardLevelModel.rank(5));
        assertEquals(GuardRank.VETERAN, GuardLevelModel.rank(7));
        assertEquals(GuardRank.ELITE, GuardLevelModel.rank(9));
        assertEquals(GuardRank.ELITE, GuardLevelModel.rank(10));
    }

    @Test
    void mapsAllNaturalLevelWeightBoundaries() {
        int[] firstRolls = {0, 450, 700, 840, 920, 960, 980, 990, 996, 999};
        for (int index = 0; index < firstRolls.length; index++) {
            assertEquals(index + 1, GuardLevelModel.naturalLevel(firstRolls[index]));
        }
        assertThrows(IllegalArgumentException.class, () -> GuardLevelModel.naturalLevel(-1));
        assertThrows(IllegalArgumentException.class, () -> GuardLevelModel.naturalLevel(1_000));
    }

    @Test
    void exposesExactCumulativeAttributeBonuses() {
        double[] health = {0, 1, 1, 2, 2, 3, 4, 5, 6, 8};
        double[] attack = {0, 0, 0.25, 0.25, 0.50, 0.75, 1.00, 1.25, 1.50, 2.00};
        double[] armor = {0, 0, 0, 1, 1, 1, 2, 2, 3, 3};
        for (int index = 0; index < 10; index++) {
            int level = index + 1;
            assertEquals(health[index], GuardLevelModel.healthBonus(level));
            assertEquals(attack[index], GuardLevelModel.attackBonus(level));
            assertEquals(armor[index], GuardLevelModel.armorBonus(level));
        }
    }
}
