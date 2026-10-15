package net.hfstack.rallyguard.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuardProfileTest {
    @Test
    void createV2ClampsQualityToZeroThroughTenThousand() {
        assertEquals(0, profileWithQuality(-1).naturalQuality());
        assertEquals(10_000, profileWithQuality(10_001).naturalQuality());
    }

    @Test
    void createV2ClampsEveryRollToSignedTenThousand() {
        GuardProfile profile = GuardProfile.createV2(
                GuardProfileOrigin.NATURAL,
                0,
                5_000,
                -10_001,
                10_001,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                0,
                0,
                false
        );

        assertEquals(-10_000, profile.healthRoll());
        assertEquals(10_000, profile.attackRoll());
        assertEquals(-10_000, profile.armorRoll());
        assertEquals(10_000, profile.speedRoll());
    }

    @Test
    void createV2ClampsNegativeXpKillsAndAssistsToZero() {
        GuardProfile profile = GuardProfile.createV2(
                GuardProfileOrigin.COMMAND,
                -1,
                5_000,
                0,
                0,
                0,
                0,
                -2,
                -3,
                false
        );

        assertEquals(0, profile.totalXpMilli());
        assertEquals(0, profile.kills());
        assertEquals(0, profile.assists());
    }

    @Test
    void createV2HandlesLongMinValueWithoutOverflow() {
        GuardProfile profile = GuardProfile.createV2(
                GuardProfileOrigin.OTHER,
                Long.MIN_VALUE,
                5_000,
                0,
                0,
                0,
                0,
                Long.MIN_VALUE,
                Long.MIN_VALUE,
                false
        );

        assertEquals(0, profile.totalXpMilli());
        assertEquals(0, profile.kills());
        assertEquals(0, profile.assists());
    }

    @Test
    void legacyNeutralUsesSchemaTwoLegacyLevelOneDefaults() {
        GuardProfile profile = GuardProfile.legacyNeutral();

        assertEquals(2, profile.schemaVersion());
        assertEquals(1, profile.generationVersion());
        assertEquals(GuardProfileOrigin.LEGACY, profile.originType());
        assertEquals("legacy", profile.origin());
        assertEquals(0, profile.totalXpMilli());
        assertEquals(5_000, profile.naturalQuality());
        assertEquals(0, profile.healthRoll());
        assertEquals(0, profile.attackRoll());
        assertEquals(0, profile.armorRoll());
        assertEquals(0, profile.speedRoll());
        assertEquals(0, profile.kills());
        assertEquals(0, profile.assists());
        assertEquals(false, profile.captain());
    }

    @Test
    void unknownOriginNameMapsToOther() {
        assertEquals(GuardProfileOrigin.OTHER, GuardProfileOrigin.fromSerializedName("unknown"));
        assertEquals(GuardProfileOrigin.OTHER, GuardProfileOrigin.fromSerializedName(null));
    }

    @Test
    void changingCaptainRolePreservesEveryOtherProfileField() {
        GuardProfile original = GuardProfile.restoreV2(
                2,
                GuardProfileOrigin.NATURAL,
                123_456,
                8_765,
                -321,
                654,
                2_345,
                -987,
                42,
                19,
                false
        );

        GuardProfile promoted = original.withCaptain(true);
        GuardProfile demoted = promoted.withCaptain(false);

        assertEquals(true, promoted.captain());
        assertEquals(false, demoted.captain());
        assertEquals(original, demoted);
        assertEquals(original.generationVersion(), promoted.generationVersion());
        assertEquals(original.originType(), promoted.originType());
        assertEquals(original.totalXpMilli(), promoted.totalXpMilli());
        assertEquals(original.naturalQuality(), promoted.naturalQuality());
        assertEquals(original.healthRoll(), promoted.healthRoll());
        assertEquals(original.attackRoll(), promoted.attackRoll());
        assertEquals(original.armorRoll(), promoted.armorRoll());
        assertEquals(original.speedRoll(), promoted.speedRoll());
        assertEquals(original.kills(), promoted.kills());
        assertEquals(original.assists(), promoted.assists());
    }

    private static GuardProfile profileWithQuality(int quality) {
        return GuardProfile.createV2(
                GuardProfileOrigin.NATURAL,
                0,
                quality,
                0,
                0,
                0,
                0,
                0,
                0,
                false
        );
    }
}
