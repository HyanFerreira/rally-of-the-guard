package net.hfstack.rallyguard.combat.stance;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatStancePolicyTest {
    @Test
    void passiveAlwaysDisengagesFromCombat() {
        assertEquals(
                CombatStancePolicy.Decision.DISENGAGE,
                CombatStancePolicy.decide(GuardCombatStance.PASSIVE, true, false, false)
        );
        assertEquals(
                CombatStancePolicy.Decision.NONE,
                CombatStancePolicy.decide(GuardCombatStance.PASSIVE, false, false, false)
        );
    }

    @Test
    void holdPositionDropsDistantTargetsAndReturnsPastMargin() {
        assertEquals(
                CombatStancePolicy.Decision.DISENGAGE,
                CombatStancePolicy.decide(GuardCombatStance.HOLD_POSITION, true, false, false)
        );
        assertEquals(
                CombatStancePolicy.Decision.DISENGAGE_AND_RETURN,
                CombatStancePolicy.decide(GuardCombatStance.HOLD_POSITION, true, false, true)
        );
        assertEquals(
                CombatStancePolicy.Decision.RETURN_TO_ANCHOR,
                CombatStancePolicy.decide(GuardCombatStance.HOLD_POSITION, false, false, true)
        );
    }

    @Test
    void holdBoundaryUsesBlockCenterAndTwoBlockReturnMargin() {
        BlockPos anchor = BlockPos.ZERO;

        assertTrue(CombatStancePolicy.withinRadius(anchor, 8.5, 0.5, 0.5, 8));
        assertFalse(CombatStancePolicy.withinRadius(anchor, 8.51, 0.5, 0.5, 8));
        assertFalse(CombatStancePolicy.beyondReturnBoundary(anchor, 10.5, 0.5, 0.5, 8));
        assertTrue(CombatStancePolicy.beyondReturnBoundary(anchor, 10.51, 0.5, 0.5, 8));
    }

    @Test
    void otherStancesLeaveExistingCombatToTheirSpecificHandlers() {
        assertEquals(
                CombatStancePolicy.Decision.NONE,
                CombatStancePolicy.decide(GuardCombatStance.DEFENSIVE, true, false, true)
        );
        assertEquals(
                CombatStancePolicy.Decision.NONE,
                CombatStancePolicy.decide(GuardCombatStance.AGGRESSIVE, false, false, false)
        );
    }
}
