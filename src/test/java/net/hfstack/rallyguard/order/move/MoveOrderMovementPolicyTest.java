package net.hfstack.rallyguard.order.move;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveOrderMovementPolicyTest {
    @Test
    void combatPausesWithoutConsideringArrival() {
        assertEquals(
                MoveOrderMovementPolicy.Decision.PAUSE_FOR_COMBAT,
                MoveOrderMovementPolicy.decide(true, 0.0)
        );
    }

    @Test
    void distanceBeyondTwoAndAHalfBlocksNavigates() {
        assertEquals(
                MoveOrderMovementPolicy.Decision.NAVIGATE,
                MoveOrderMovementPolicy.decide(false, 6.250_001)
        );
    }

    @Test
    void distanceAtArrivalBoundaryCompletesTheOrder() {
        assertEquals(
                MoveOrderMovementPolicy.Decision.ARRIVE,
                MoveOrderMovementPolicy.decide(false, 6.25)
        );
    }
}
