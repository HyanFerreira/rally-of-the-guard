package net.hfstack.rallyguard.formation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormationMovementPolicyTest {
    @Test
    void stationaryGuardHoldsWhenAlreadyAtItsSlot() {
        FormationMovementPlan plan = FormationMovementPolicy.plan(false, false, 0.5, 1.0);

        assertFalse(plan.navigate());
        assertEquals(0.0, plan.leadDistance());
    }

    @Test
    void stationaryGuardReturnsToASeparatedSlotAtConfiguredSpeed() {
        FormationMovementPlan plan = FormationMovementPolicy.plan(false, false, 1.0, 0.8);

        assertTrue(plan.navigate());
        assertEquals(0.8, plan.speed());
        assertEquals(0.0, plan.leadDistance());
    }

    @Test
    void walkingCommanderKeepsNearbyGuardNavigatingTowardALeadSlot() {
        FormationMovementPlan plan = FormationMovementPolicy.plan(true, false, 0.1, 0.8);

        assertTrue(plan.navigate());
        assertEquals(1.0, plan.speed());
        assertEquals(1.5, plan.leadDistance());
    }

    @Test
    void sprintingCommanderRaisesSpeedAndLookAheadWithoutBecomingExtreme() {
        FormationMovementPlan plan = FormationMovementPolicy.plan(true, true, 1.0, 1.0);

        assertTrue(plan.navigate());
        assertEquals(1.23, plan.speed(), 1.0e-9);
        assertEquals(2.0, plan.leadDistance());
    }

    @Test
    void laggingGuardUsesBoundedCatchUpSpeed() {
        FormationMovementPlan plan = FormationMovementPolicy.plan(true, true, 25.0, 1.0);

        assertEquals(1.35, plan.speed(), 1.0e-9);
    }

    @Test
    void movingLagRaisesSpeedProportionallyInsteadOfInSteps() {
        FormationMovementPlan plan = FormationMovementPolicy.plan(true, false, 4.0, 1.0);

        assertEquals(1.15, plan.speed(), 1.0e-9);
    }

    @Test
    void nearbyLagValuesProduceNearbySpeeds() {
        FormationMovementPlan justBefore = FormationMovementPolicy.plan(true, false, 2.24, 1.0);
        FormationMovementPlan justAfter = FormationMovementPolicy.plan(true, false, 2.26, 1.0);

        assertTrue(justAfter.speed() > justBefore.speed());
        assertTrue(justAfter.speed() - justBefore.speed() < 0.01);
    }

    @Test
    void explicitHigherConfiguredSpeedIsNeverReduced() {
        FormationMovementPlan plan = FormationMovementPolicy.plan(true, true, 25.0, 2.0);

        assertEquals(2.0, plan.speed());
    }
}
