package net.hfstack.rallyguard.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RallyConfigTest {
    @Test
    void squadSettingsDefaultAndClampToSupportedRanges() {
        boolean originalEnabled = RallyConfig.squadsEnabled;
        int originalSquads = RallyConfig.maxSquadsPerPlayer;
        int originalMembers = RallyConfig.maxMembersPerSquad;
        int originalName = RallyConfig.maxSquadNameLength;
        try {
            assertTrue(RallyConfig.squadsEnabled());
            assertEquals(16, RallyConfig.maxSquadsPerPlayer());
            assertEquals(32, RallyConfig.maxMembersPerSquad());
            assertEquals(24, RallyConfig.maxSquadNameLength());

            RallyConfig.maxSquadsPerPlayer = 100;
            RallyConfig.maxMembersPerSquad = 0;
            RallyConfig.maxSquadNameLength = 99;
            assertEquals(64, RallyConfig.maxSquadsPerPlayer());
            assertEquals(1, RallyConfig.maxMembersPerSquad());
            assertEquals(48, RallyConfig.maxSquadNameLength());
        } finally {
            RallyConfig.squadsEnabled = originalEnabled;
            RallyConfig.maxSquadsPerPlayer = originalSquads;
            RallyConfig.maxMembersPerSquad = originalMembers;
            RallyConfig.maxSquadNameLength = originalName;
        }
    }

    @Test
    void retreatDefaultsAndDistanceClampsToSupportedRange() {
        boolean originalEnabled = RallyConfig.rallyRetreatEnabled;
        double originalDistance = RallyConfig.rallyRetreatDistance;
        try {
            assertTrue(RallyConfig.rallyRetreatEnabled());
            assertEquals(24.0, RallyConfig.rallyRetreatDistance());

            RallyConfig.rallyRetreatDistance = 0.0;
            assertEquals(1.0, RallyConfig.rallyRetreatDistance());

            RallyConfig.rallyRetreatDistance = 999.0;
            assertEquals(512.0, RallyConfig.rallyRetreatDistance());
        } finally {
            RallyConfig.rallyRetreatEnabled = originalEnabled;
            RallyConfig.rallyRetreatDistance = originalDistance;
        }
    }

    @Test
    void progressionFeaturesDefaultToEnabled() {
        assertTrue(RallyConfig.progressionEnabled());
        assertTrue(RallyConfig.naturalVariationEnabled());
        assertTrue(RallyConfig.statGrowthEnabled());
        assertTrue(RallyConfig.advancedNaturalGuardsEnabled());
        assertTrue(RallyConfig.naturalCaptainsEnabled());
        assertEquals(0.05, RallyConfig.captainSpawnChance());
        assertEquals(5, RallyConfig.captainMinimumLevel());
        assertEquals(128.0, RallyConfig.captainRegionalRadius());
        assertEquals(2, RallyConfig.captainRegionalLimit());
        assertEquals(4, RallyConfig.captainHireCostBonus());
    }

    @Test
    void captainSettingsClampAndAdvancedNaturalGuardsCanDisableGeneration() {
        boolean originalAdvanced = RallyConfig.advancedNaturalGuardsEnabled;
        boolean originalCaptains = RallyConfig.captainSpawnEnabled;
        double originalChance = RallyConfig.captainSpawnChance;
        int originalLevel = RallyConfig.captainMinimumLevel;
        double originalRadius = RallyConfig.captainRegionalRadius;
        int originalLimit = RallyConfig.captainRegionalLimit;
        int originalBonus = RallyConfig.captainHireCostBonus;
        try {
            RallyConfig.advancedNaturalGuardsEnabled = false;
            RallyConfig.captainSpawnEnabled = true;
            RallyConfig.captainSpawnChance = Double.NaN;
            RallyConfig.captainMinimumLevel = 99;
            RallyConfig.captainRegionalRadius = 0.0;
            RallyConfig.captainRegionalLimit = -2;
            RallyConfig.captainHireCostBonus = 2_000;

            assertFalse(RallyConfig.naturalCaptainsEnabled());
            assertEquals(0.05, RallyConfig.captainSpawnChance());
            assertEquals(10, RallyConfig.captainMinimumLevel());
            assertEquals(1.0, RallyConfig.captainRegionalRadius());
            assertEquals(0, RallyConfig.captainRegionalLimit());
            assertEquals(999, RallyConfig.captainHireCostBonus());
        } finally {
            RallyConfig.advancedNaturalGuardsEnabled = originalAdvanced;
            RallyConfig.captainSpawnEnabled = originalCaptains;
            RallyConfig.captainSpawnChance = originalChance;
            RallyConfig.captainMinimumLevel = originalLevel;
            RallyConfig.captainRegionalRadius = originalRadius;
            RallyConfig.captainRegionalLimit = originalLimit;
            RallyConfig.captainHireCostBonus = originalBonus;
        }
    }

    @Test
    void captainPromotionSettingsDefaultAndClampToSupportedRanges() {
        boolean originalEnabled = RallyConfig.captainPromotionEnabled;
        int originalLevel = RallyConfig.captainPromotionMinimumLevel;
        int originalCost = RallyConfig.captainPromotionCost;
        try {
            assertTrue(RallyConfig.captainPromotionEnabled());
            assertEquals(7, RallyConfig.captainPromotionMinimumLevel());
            assertEquals(4, RallyConfig.captainPromotionCost());

            RallyConfig.captainPromotionEnabled = false;
            RallyConfig.captainPromotionMinimumLevel = -1;
            RallyConfig.captainPromotionCost = -20;
            assertFalse(RallyConfig.captainPromotionEnabled());
            assertEquals(1, RallyConfig.captainPromotionMinimumLevel());
            assertEquals(0, RallyConfig.captainPromotionCost());

            RallyConfig.captainPromotionMinimumLevel = 99;
            RallyConfig.captainPromotionCost = 2_000;
            assertEquals(10, RallyConfig.captainPromotionMinimumLevel());
            assertEquals(999, RallyConfig.captainPromotionCost());
        } finally {
            RallyConfig.captainPromotionEnabled = originalEnabled;
            RallyConfig.captainPromotionMinimumLevel = originalLevel;
            RallyConfig.captainPromotionCost = originalCost;
        }
    }

    @Test
    void combatProgressionSettingsClampAndRejectNonFiniteValues() {
        double originalMultiplier = RallyConfig.xpMultiplier;
        int originalTimeout = RallyConfig.combatTimeoutSeconds;
        double originalContribution = RallyConfig.damageContributionShare;
        double originalFinalBlow = RallyConfig.finalBlowShare;
        double originalAssistDamage = RallyConfig.assistMinimumDamage;
        double originalAssistShare = RallyConfig.assistMinimumShare;
        try {
            RallyConfig.xpMultiplier = Double.NaN;
            RallyConfig.combatTimeoutSeconds = 0;
            RallyConfig.damageContributionShare = 2.0;
            RallyConfig.finalBlowShare = -1.0;
            RallyConfig.assistMinimumDamage = Double.POSITIVE_INFINITY;
            RallyConfig.assistMinimumShare = 2.0;

            assertEquals(1.0, RallyConfig.xpMultiplier());
            assertEquals(20, RallyConfig.combatTimeoutTicks());
            assertEquals(1.0, RallyConfig.damageContributionShare());
            assertEquals(0.0, RallyConfig.finalBlowShare());
            assertEquals(1.0, RallyConfig.assistMinimumDamage());
            assertEquals(1.0, RallyConfig.assistMinimumShare());
        } finally {
            RallyConfig.xpMultiplier = originalMultiplier;
            RallyConfig.combatTimeoutSeconds = originalTimeout;
            RallyConfig.damageContributionShare = originalContribution;
            RallyConfig.finalBlowShare = originalFinalBlow;
            RallyConfig.assistMinimumDamage = originalAssistDamage;
            RallyConfig.assistMinimumShare = originalAssistShare;
        }
    }

    @Test
    void dynamicHiringSettingsClampAndKeepMaximumAboveMinimum() {
        double originalMultiplier = RallyConfig.dynamicHireCostMultiplier;
        int originalMinimum = RallyConfig.dynamicHireCostMinimum;
        int originalMaximum = RallyConfig.dynamicHireCostMaximum;
        try {
            RallyConfig.dynamicHireCostMultiplier = Double.NaN;
            RallyConfig.dynamicHireCostMinimum = 800;
            RallyConfig.dynamicHireCostMaximum = 4;

            assertEquals(1.0, RallyConfig.dynamicHireCostMultiplier());
            assertEquals(800, RallyConfig.dynamicHireCostMinimum());
            assertEquals(800, RallyConfig.dynamicHireCostMaximum());
        } finally {
            RallyConfig.dynamicHireCostMultiplier = originalMultiplier;
            RallyConfig.dynamicHireCostMinimum = originalMinimum;
            RallyConfig.dynamicHireCostMaximum = originalMaximum;
        }
    }
}
