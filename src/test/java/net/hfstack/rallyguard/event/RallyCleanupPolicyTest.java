package net.hfstack.rallyguard.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RallyCleanupPolicyTest {
    @Test
    void releasesOnlyOwnedRallyParticipant() {
        assertTrue(RallyCleanupPolicy.shouldRelease(true, true));
        assertFalse(RallyCleanupPolicy.shouldRelease(false, true));
        assertFalse(RallyCleanupPolicy.shouldRelease(true, false));
        assertFalse(RallyCleanupPolicy.shouldRelease(false, false));
    }

    @Test
    void repeatedCleanupIsIdempotentAfterRallyTagIsCleared() {
        assertTrue(RallyCleanupPolicy.shouldRelease(true, true));
        assertFalse(RallyCleanupPolicy.shouldRelease(true, false));
    }
}
