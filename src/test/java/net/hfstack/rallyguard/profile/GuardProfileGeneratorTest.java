package net.hfstack.rallyguard.profile;

import org.junit.jupiter.api.Test;
import net.hfstack.rallyguard.config.RallyConfig;

import java.util.UUID;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardProfileGeneratorTest {
    @Test
    void generationIsDeterministicForGuardIdentity() {
        UUID guardId = UUID.fromString("2187ba6f-51dd-410d-b821-33d6bd78377a");
        GuardProfile first = GuardProfileGenerator.generate(guardId, GuardProfileOrigin.LEGACY, false);
        GuardProfile second = GuardProfileGenerator.generate(guardId, GuardProfileOrigin.LEGACY, false);

        assertEquals(first, second);
        assertEquals(GuardProfile.CURRENT_GENERATION_VERSION, first.generationVersion());
        assertEquals(1, GuardLevelModel.level(first.totalXpMilli()));
    }

    @Test
    void differentGuardIdentitiesProduceDifferentProfiles() {
        GuardProfile first = GuardProfileGenerator.generate(new UUID(1, 2), GuardProfileOrigin.NATURAL, true);
        GuardProfile second = GuardProfileGenerator.generate(new UUID(3, 4), GuardProfileOrigin.NATURAL, true);

        assertFalse(first.equals(second));
    }

    @Test
    void onlyExactProfileCorePlaceholderMigrates() {
        assertTrue(NaturalIdentityMigration.isProfileCorePlaceholder(GuardProfile.legacyNeutral()));
        GuardProfile generated = GuardProfileGenerator.generate(new UUID(1, 2), GuardProfileOrigin.LEGACY, false);
        assertFalse(NaturalIdentityMigration.isProfileCorePlaceholder(generated));
        GuardProfile placeholderWithService = GuardProfile.restoreV2(
                GuardProfile.PROFILE_CORE_PLACEHOLDER_GENERATION_VERSION,
                GuardProfileOrigin.LEGACY,
                0,
                5_000,
                0,
                0,
                0,
                0,
                1,
                0,
                false
        );
        assertFalse(NaturalIdentityMigration.isProfileCorePlaceholder(placeholderWithService));
    }

    @Test
    void qualityDistributionRemainsTriangular() {
        Random random = new Random(42);
        int[] stars = new int[5];
        int samples = 100_000;
        for (int index = 0; index < samples; index++) {
            int quality = NaturalAttributeModel.quality(random.nextInt(10_001), random.nextInt(10_001));
            stars[Math.min(4, quality / 2_000)]++;
        }

        double[] expected = {0.08, 0.24, 0.36, 0.24, 0.08};
        for (int index = 0; index < stars.length; index++) {
            double actual = stars[index] / (double) samples;
            assertTrue(Math.abs(actual - expected[index]) < 0.01,
                    () -> "Unexpected frequency for star band: " + actual);
        }
    }

    @Test
    void naturalCaptainSelectionPromotesOnlyLevelAndPreservesNaturalRolls() {
        boolean originalAdvanced = RallyConfig.advancedNaturalGuardsEnabled;
        boolean originalEnabled = RallyConfig.captainSpawnEnabled;
        double originalChance = RallyConfig.captainSpawnChance;
        int originalMinimum = RallyConfig.captainMinimumLevel;
        int originalLimit = RallyConfig.captainRegionalLimit;
        try {
            RallyConfig.advancedNaturalGuardsEnabled = true;
            RallyConfig.captainSpawnEnabled = true;
            RallyConfig.captainSpawnChance = 1.0;
            RallyConfig.captainMinimumLevel = 5;
            RallyConfig.captainRegionalLimit = 2;
            UUID id = UUID.fromString("16e18838-37f7-4776-a87c-3c3d8f0d9efd");

            GuardProfile captain = GuardProfileGenerator.generate(id, GuardProfileOrigin.NATURAL, true, 0);
            GuardProfile blocked = GuardProfileGenerator.generate(id, GuardProfileOrigin.NATURAL, true, 2);
            GuardProfile command = GuardProfileGenerator.generate(id, GuardProfileOrigin.COMMAND, false, 0);

            assertTrue(captain.captain());
            assertTrue(captain.level() >= 5);
            assertFalse(blocked.captain());
            assertFalse(command.captain());
            assertEquals(blocked.naturalQuality(), captain.naturalQuality());
            assertEquals(blocked.healthRoll(), captain.healthRoll());
            assertEquals(blocked.attackRoll(), captain.attackRoll());
            assertEquals(blocked.armorRoll(), captain.armorRoll());
            assertEquals(blocked.speedRoll(), captain.speedRoll());
        } finally {
            RallyConfig.advancedNaturalGuardsEnabled = originalAdvanced;
            RallyConfig.captainSpawnEnabled = originalEnabled;
            RallyConfig.captainSpawnChance = originalChance;
            RallyConfig.captainMinimumLevel = originalMinimum;
            RallyConfig.captainRegionalLimit = originalLimit;
        }
    }
}
