package net.hfstack.rallyguard.raid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaidResponseActionTest {
    @Test
    void acceptsOnlyStableWireIds() {
        assertEquals(RaidResponseAction.RALLY_GUARDS, RaidResponseAction.fromWireId(0).orElseThrow());
        assertEquals(RaidResponseAction.DEFEND_VILLAGE, RaidResponseAction.fromWireId(1).orElseThrow());
        assertEquals(RaidResponseAction.IGNORE, RaidResponseAction.fromWireId(2).orElseThrow());
        assertTrue(RaidResponseAction.fromWireId(-1).isEmpty());
        assertTrue(RaidResponseAction.fromWireId(3).isEmpty());
    }
}
