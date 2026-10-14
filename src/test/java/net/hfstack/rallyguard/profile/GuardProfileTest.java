package net.hfstack.rallyguard.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuardProfileTest {
    @Test
    void createV1ClampsQualityToZeroThroughTenThousand() {
        assertEquals(0, profileWithQuality(-1).naturalQuality());
        assertEquals(10_000, profileWithQuality(10_001).naturalQuality());
    }

    @Test
    void createV1ClampsEveryRollToSignedTenThousand() {
        GuardProfile profile = GuardProfile.createV1(
                GuardProfileOrigin.NATURAL,
                0,
                5_000,
                -10_001,
                10_001,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                0,
                0
        );

        assertEquals(-10_000, profile.healthRoll());
        assertEquals(10_000, profile.attackRoll());
        assertEquals(-10_000, profile.armorRoll());
        assertEquals(10_000, profile.speedRoll());
    }

    @Test
    void createV1ClampsNegativeXpKillsAndAssistsToZero() {
        GuardProfile profile = GuardProfile.createV1(
                GuardProfileOrigin.COMMAND,
                -1,
                5_000,
                0,
                0,
                0,
                0,
                -2,
                -3
        );

        assertEquals(0, profile.totalXpMilli());
        assertEquals(0, profile.kills());
        assertEquals(0, profile.assists());
    }

    @Test
    void createV1HandlesLongMinValueWithoutOverflow() {
        GuardProfile profile = GuardProfile.createV1(
                GuardProfileOrigin.OTHER,
                Long.MIN_VALUE,
                5_000,
                0,
                0,
                0,
                0,
                Long.MIN_VALUE,
                Long.MIN_VALUE
        );

        assertEquals(0, profile.totalXpMilli());
        assertEquals(0, profile.kills());
        assertEquals(0, profile.assists());
    }

    @Test
    void legacyNeutralUsesV1LegacyLevelOneDefaults() {
        GuardProfile profile = GuardProfile.legacyNeutral();

        assertEquals(1, profile.schemaVersion());
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
    }

    @Test
    void unknownOriginNameMapsToOther() {
        assertEquals(GuardProfileOrigin.OTHER, GuardProfileOrigin.fromSerializedName("unknown"));
        assertEquals(GuardProfileOrigin.OTHER, GuardProfileOrigin.fromSerializedName(null));
    }

    private static GuardProfile profileWithQuality(int quality) {
        return GuardProfile.createV1(
                GuardProfileOrigin.NATURAL,
                0,
                quality,
                0,
                0,
                0,
                0,
                0,
                0
        );
    }
}
