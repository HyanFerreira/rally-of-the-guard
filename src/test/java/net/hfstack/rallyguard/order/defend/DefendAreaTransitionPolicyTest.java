package net.hfstack.rallyguard.order.defend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefendAreaTransitionPolicyTest {
    @Test
    void rallyStillExcludesOrdinaryPatrolButMayReplaceDefendArea() {
        assertFalse(DefendAreaTransitionPolicy.mayJoinRally(true, false));
        assertTrue(DefendAreaTransitionPolicy.mayJoinRally(true, true));
        assertTrue(DefendAreaTransitionPolicy.mayJoinRally(false, false));
    }
}
