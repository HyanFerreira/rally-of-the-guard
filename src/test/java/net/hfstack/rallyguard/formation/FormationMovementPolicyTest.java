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
        assertEquals(0.0, plan.leadMultiplier());
    }

    @Test
    void stationaryGuardReturnsToASeparatedSlotAtConfiguredSpeed() {
        FormationMovementPlan plan = FormationMovementPolicy.plan(false, false, 1.0, 0.8);

        assertTrue(plan.navigate());
        assertEquals(0.8, plan.speed());
        assertEquals(0.0, plan.leadMultiplier());
    }

    @Test
    void walkingCommanderKeepsNearbyGuardNavigatingTowardALeadSlot() {
        FormationMovementPlan plan = FormationMovementPolicy.plan(true, false, 0.1, 0.8);

        assertTrue(plan.navigate());
        assertEquals(1.0, plan.speed());
        assertEquals(1.0, plan.leadMultiplier());
    }

    @Test
    void sprintingCommanderRaisesSpeedAndLookAheadWithoutBecomingExtreme() {
        FormationMovementPlan plan = FormationMovementPolicy.plan(true, true, 1.0, 1.0);

        assertTrue(plan.navigate());
        assertEquals(1.2, plan.speed());
        assertEquals(1.5, plan.leadMultiplier());
    }

    @Test
    void laggingGuardUsesBoundedCatchUpSpeed() {
        FormationMovementPlan plan = FormationMovementPolicy.plan(true, true, 25.0, 1.0);

        assertEquals(1.35, plan.speed());
    }

    @Test
    void explicitHigherConfiguredSpeedIsNeverReduced() {
        FormationMovementPlan plan = FormationMovementPolicy.plan(true, true, 25.0, 2.0);

        assertEquals(2.0, plan.speed());
    }
}
