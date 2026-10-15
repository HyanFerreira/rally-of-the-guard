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

    @Test
    void facingTurnsGraduallyTowardActualMovement() {
        FormationVelocity velocity = new FormationVelocity(0.1, 0.0);

        assertEquals(-18.0F, FormationVelocityPolicy.facingYaw(0.0F, velocity), 1.0e-6F);
        assertEquals(-90.0F, FormationVelocityPolicy.facingYaw(-80.0F, velocity), 1.0e-6F);
    }

    @Test
    void negligibleMovementKeepsCurrentFacing() {
        FormationVelocity velocity = new FormationVelocity(0.001, 0.001);

        assertEquals(35.0F, FormationVelocityPolicy.facingYaw(35.0F, velocity), 1.0e-6F);
    }
}
