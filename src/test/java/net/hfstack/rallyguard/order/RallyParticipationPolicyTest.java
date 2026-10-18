package net.hfstack.rallyguard.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RallyParticipationPolicyTest {
    @Test
    void idleGuardMayJoin() {
        assertTrue(RallyParticipationPolicy.mayJoin(false, false, false, false));
    }

    @Test
    void waitingAndActiveRouteGuardsNeverJoin() {
        assertFalse(RallyParticipationPolicy.mayJoin(false, false, true, false));
        assertFalse(RallyParticipationPolicy.mayJoin(false, false, false, true));
        assertFalse(RallyParticipationPolicy.mayJoin(true, true, true, false));
        assertFalse(RallyParticipationPolicy.mayJoin(true, true, false, true));
    }

    @Test
    void ordinaryFixedPatrolDoesNotJoin() {
        assertFalse(RallyParticipationPolicy.mayJoin(true, false, false, false));
    }

    @Test
    void defendAreaPatrolMayBeTemporarilyReplaced() {
        assertTrue(RallyParticipationPolicy.mayJoin(true, true, false, false));
    }
}
