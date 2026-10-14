package net.hfstack.rallyguard.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RallyGuardCommandsTest {
    @Test
    void convertsVisibleXpPointsToInternalMilliXp() {
        assertEquals(20_000, RallyGuardCommands.toXpMilli(20));
        assertEquals(4_700, RallyGuardCommands.toXpMilli(4.7));
        assertEquals(1, RallyGuardCommands.toXpMilli(0.001));
    }

    @Test
    void formatsInternalMilliXpForCommandFeedback() {
        assertEquals("20", RallyGuardCommands.formatXp(20_000));
        assertEquals("4.7", RallyGuardCommands.formatXp(4_700));
        assertEquals("0.001", RallyGuardCommands.formatXp(1));
        assertEquals("0", RallyGuardCommands.formatXp(-1));
    }
}
