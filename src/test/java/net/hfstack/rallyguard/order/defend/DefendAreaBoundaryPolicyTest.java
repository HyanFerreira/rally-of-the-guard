package net.hfstack.rallyguard.order.defend;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefendAreaBoundaryPolicyTest {
    @Test
    void radiusUsesThreeDimensionalDistanceFromBlockCenter() {
        BlockPos center = new BlockPos(10, 64, 10);

        assertTrue(DefendAreaBoundaryPolicy.withinRadius(center, 26.5, 64.5, 10.5, 16));
        assertFalse(DefendAreaBoundaryPolicy.withinRadius(center, 26.51, 64.5, 10.5, 16));
    }

    @Test
    void liveTargetInsideAreaRemainsUnderNativeCombatControl() {
        assertEquals(
                DefendAreaBoundaryPolicy.Decision.KEEP_COMBAT,
                DefendAreaBoundaryPolicy.decide(true, true, false)
        );
    }

    @Test
    void targetOutsideAreaIsDroppedAndGuardReturnsOnlyBeyondMargin() {
        assertEquals(
                DefendAreaBoundaryPolicy.Decision.DISENGAGE,
                DefendAreaBoundaryPolicy.decide(true, false, false)
        );
        assertEquals(
                DefendAreaBoundaryPolicy.Decision.DISENGAGE_AND_RETURN,
                DefendAreaBoundaryPolicy.decide(true, false, true)
        );
    }

    @Test
    void idleGuardReturnsAfterTwoBlockBoundaryMargin() {
        BlockPos center = BlockPos.ZERO;

        assertFalse(DefendAreaBoundaryPolicy.beyondReturnBoundary(center, 18.5, 0.5, 0.5, 16));
        assertTrue(DefendAreaBoundaryPolicy.beyondReturnBoundary(center, 18.51, 0.5, 0.5, 16));
        assertEquals(
                DefendAreaBoundaryPolicy.Decision.RETURN_TO_POST,
                DefendAreaBoundaryPolicy.decide(false, false, true)
        );
    }
}
