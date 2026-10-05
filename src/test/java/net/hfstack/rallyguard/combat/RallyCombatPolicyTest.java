package net.hfstack.rallyguard.combat;

import org.junit.jupiter.api.Test;

import static net.hfstack.rallyguard.combat.RallyCombatPolicy.Decision.CONTINUE_COMBAT;
import static net.hfstack.rallyguard.combat.RallyCombatPolicy.Decision.DISENGAGE_AND_FORM_UP;
import static net.hfstack.rallyguard.combat.RallyCombatPolicy.Decision.FORM_UP;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RallyCombatPolicyTest {
    @Test
    void formsUpWithoutALiveTarget() {
        assertEquals(FORM_UP, RallyCombatPolicy.decide(false, false, true, 900.0, 24.0));
    }

    @Test
    void protectedTargetAlwaysForcesDisengagement() {
        assertEquals(DISENGAGE_AND_FORM_UP,
                RallyCombatPolicy.decide(true, true, false, 1.0, 24.0));
    }

    @Test
    void retreatDisabledPreservesValidCombat() {
        assertEquals(CONTINUE_COMBAT,
                RallyCombatPolicy.decide(true, false, false, 10_000.0, 24.0));
    }

    @Test
    void retreatBoundaryIsExclusive() {
        assertEquals(CONTINUE_COMBAT,
                RallyCombatPolicy.decide(true, false, true, 24.0 * 24.0, 24.0));
    }

    @Test
    void distanceAboveBoundaryForcesDisengagement() {
        assertEquals(DISENGAGE_AND_FORM_UP,
                RallyCombatPolicy.decide(true, false, true, 24.01 * 24.01, 24.0));
    }

    @Test
    void configuredMaximumStillDisengagesAboveBoundary() {
        assertEquals(DISENGAGE_AND_FORM_UP,
                RallyCombatPolicy.decide(true, false, true, 512.01 * 512.01, 512.0));
    }
}
