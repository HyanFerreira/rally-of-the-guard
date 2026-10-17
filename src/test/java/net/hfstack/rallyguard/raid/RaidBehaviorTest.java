package net.hfstack.rallyguard.raid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaidBehaviorTest {
    @Test
    void wireIdsAndAutomaticActionsAreStable() {
        assertEquals(RaidBehavior.NORMAL, RaidBehavior.fromWireId(0).orElseThrow());
        assertTrue(RaidBehavior.NORMAL.automaticAction().isEmpty());
        assertEquals(RaidResponseAction.DEFEND_VILLAGE,
                RaidBehavior.RALLY_TO_VILLAGE.automaticAction().orElseThrow());
        assertEquals(RaidResponseAction.RALLY_GUARDS,
                RaidBehavior.RALLY_TO_COMMANDER.automaticAction().orElseThrow());
        assertTrue(RaidBehavior.fromWireId(3).isEmpty());
    }
}
