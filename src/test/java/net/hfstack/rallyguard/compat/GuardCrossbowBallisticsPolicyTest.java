package net.hfstack.rallyguard.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuardCrossbowBallisticsPolicyTest {
    @Test
    void replacesGuardVillagersExcessiveCrossbowSpeedWithVanillaMobSpeed() {
        assertEquals(1.6F, GuardCrossbowBallisticsPolicy.projectileSpeed(6.0F));
    }

    @Test
    void leavesUnexpectedProjectileSpeedsUntouched() {
        assertEquals(2.4F, GuardCrossbowBallisticsPolicy.projectileSpeed(2.4F));
    }
}
