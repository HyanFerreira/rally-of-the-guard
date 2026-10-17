package net.hfstack.rallyguard.client.quick;

import net.hfstack.rallyguard.network.NetworkConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickCommandActionTest {
    @Test
    void stableIdsRoundTripAndRejectUnknownValues() {
        assertEquals(0, QuickCommandAction.FOLLOW.id());
        assertEquals(1, QuickCommandAction.ATTACK.id());
        assertEquals(2, QuickCommandAction.MOVE.id());
        assertEquals(3, QuickCommandAction.WAIT.id());
        assertEquals(4, QuickCommandAction.DEFEND.id());
        assertEquals(5, QuickCommandAction.DISMISS.id());
        assertEquals(QuickCommandAction.DEFEND, QuickCommandAction.fromId(4).orElseThrow());
        assertTrue(QuickCommandAction.fromId(6).isEmpty());
    }

    @Test
    void actionsMapToExistingAuthoritativeFlows() {
        assertEquals(NetworkConstants.ACTION_FOLLOW, QuickCommandAction.FOLLOW.serverAction().orElseThrow());
        assertEquals(NetworkConstants.ACTION_WAIT, QuickCommandAction.WAIT.serverAction().orElseThrow());
        assertEquals(NetworkConstants.ACTION_DISMISS, QuickCommandAction.DISMISS.serverAction().orElseThrow());

        assertEquals(QuickCommandAction.Flow.COMBAT, QuickCommandAction.ATTACK.flow());
        assertEquals(QuickCommandAction.Flow.MOVE, QuickCommandAction.MOVE.flow());
        assertEquals(QuickCommandAction.Flow.DEFEND, QuickCommandAction.DEFEND.flow());
        assertFalse(QuickCommandAction.ATTACK.serverAction().isPresent());
        assertFalse(QuickCommandAction.MOVE.serverAction().isPresent());
        assertFalse(QuickCommandAction.DEFEND.serverAction().isPresent());
    }
}
