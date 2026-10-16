package net.hfstack.rallyguard.client.quick;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickCommandActionTest {
    @Test
    void stableIdsRoundTripAndRejectUnknownValues() {
        assertEquals(0, QuickCommandAction.FOLLOW.id());
        assertEquals(1, QuickCommandAction.ATTACK.id());
        assertEquals(2, QuickCommandAction.MOVE.id());
        assertEquals(3, QuickCommandAction.WAIT.id());
        assertEquals(4, QuickCommandAction.DEFEND.id());
        assertEquals(5, QuickCommandAction.PATROL.id());
        assertEquals(QuickCommandAction.DEFEND, QuickCommandAction.fromId(4).orElseThrow());
        assertTrue(QuickCommandAction.fromId(6).isEmpty());
    }
}
