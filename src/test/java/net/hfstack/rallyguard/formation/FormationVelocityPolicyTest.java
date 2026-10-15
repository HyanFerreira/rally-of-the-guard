package net.hfstack.rallyguard.formation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormationVelocityPolicyTest {
    @Test
    void guardAtSlotInheritsCommanderMovementWithoutLunging() {
        FormationVelocity velocity = FormationVelocityPolicy.velocity(0.1, 0.0, 0.0, 0.0);

        assertEquals(0.1, velocity.x(), 1.0e-9);
        assertEquals(0.0, velocity.z(), 1.0e-9);
    }

    @Test
    void laggingGuardReceivesSmallContinuousCorrection() {
        FormationVelocity velocity = FormationVelocityPolicy.velocity(0.1, 0.0, 0.5, 0.0);

        assertEquals(0.14, velocity.x(), 1.0e-9);
    }

    @Test
    void guardAheadOfSlotSlowsInsteadOfRunningIntoCommander() {
        FormationVelocity velocity = FormationVelocityPolicy.velocity(0.1, 0.0, -0.5, 0.0);

        assertEquals(0.06, velocity.x(), 1.0e-9);
    }

    @Test
    void correctionAndTotalSpeedRemainBounded() {
        FormationVelocity velocity = FormationVelocityPolicy.velocity(10.0, 0.0, 100.0, 0.0);

        assertTrue(Math.hypot(velocity.x(), velocity.z()) <= 0.38 + 1.0e-9);
    }
}
