package net.hfstack.rallyguard.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatrolMovementPolicyTest {
    @Test
    void configuredRouteSpeedCannotMakePatrollingGuardsRun() {
        assertEquals(0.1, PatrolMovementPolicy.walkingSpeed(0.01));
        assertEquals(0.5, PatrolMovementPolicy.walkingSpeed(0.5));
        assertEquals(0.6, PatrolMovementPolicy.walkingSpeed(4.0));
    }

    @Test
    void routeIdleStepsRequireMoreThanTenSecondsOfWaiting() {
        assertFalse(PatrolMovementPolicy.allowsIdleStep(true, 199));
        assertFalse(PatrolMovementPolicy.allowsIdleStep(true, 200));
        assertTrue(PatrolMovementPolicy.allowsIdleStep(true, 201));
        assertTrue(PatrolMovementPolicy.allowsIdleStep(false, 0));
    }
}
