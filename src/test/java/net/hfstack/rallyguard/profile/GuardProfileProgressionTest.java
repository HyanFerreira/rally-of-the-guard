package net.hfstack.rallyguard.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuardProfileProgressionTest {
    @Test
    void progressAddsXpAndRequestedCombatStatistic() {
        GuardProfile profile = GuardProfile.legacyNeutral().withProgress(2_500, true, false);

        assertEquals(2_500, profile.totalXpMilli());
        assertEquals(1, profile.kills());
        assertEquals(0, profile.assists());
    }

    @Test
    void progressIgnoresNegativeXpAndSaturatesCounters() {
        GuardProfile maximum = GuardProfile.createV2(
                GuardProfileOrigin.LEGACY,
                Long.MAX_VALUE,
                5_000,
                0,
                0,
                0,
                0,
                Long.MAX_VALUE,
                Long.MAX_VALUE,
                true
        );

        GuardProfile updated = maximum.withProgress(Long.MAX_VALUE, true, true).withProgress(-5, false, false);
        assertEquals(Long.MAX_VALUE, updated.totalXpMilli());
        assertEquals(Long.MAX_VALUE, updated.kills());
        assertEquals(Long.MAX_VALUE, updated.assists());
        assertEquals(true, updated.captain());
    }
}
