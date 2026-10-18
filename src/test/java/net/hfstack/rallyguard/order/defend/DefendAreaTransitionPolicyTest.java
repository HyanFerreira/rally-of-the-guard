package net.hfstack.rallyguard.order.defend;

import net.hfstack.rallyguard.order.RallyParticipationPolicy;
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

    @Test
    void explicitTasksStillProtectDefendAreaGuardsFromRally() {
        assertFalse(RallyParticipationPolicy.mayJoin(true, true, true, false));
        assertFalse(RallyParticipationPolicy.mayJoin(true, true, false, true));
    }
}
