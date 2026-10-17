package net.hfstack.rallyguard.raid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaidOpportunityPolicyTest {
    @Test
    void eligiblePlayerNeedsActiveNearbyRaidAndLoadedGuards() {
        double inside = RaidOpportunityPolicy.MAX_DISTANCE * RaidOpportunityPolicy.MAX_DISTANCE;
        assertTrue(RaidOpportunityPolicy.eligible(true, false, inside, 1, false));
        assertFalse(RaidOpportunityPolicy.eligible(false, false, inside, 1, false));
        assertFalse(RaidOpportunityPolicy.eligible(true, true, inside, 1, false));
        assertFalse(RaidOpportunityPolicy.eligible(true, false, inside + 1.0, 1, false));
        assertFalse(RaidOpportunityPolicy.eligible(true, false, inside, 0, false));
        assertFalse(RaidOpportunityPolicy.eligible(true, false, inside, 1, true));
    }
}
