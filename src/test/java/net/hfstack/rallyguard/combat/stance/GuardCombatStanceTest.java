package net.hfstack.rallyguard.combat.stance;

import net.hfstack.rallyguard.api.command.GuardCombatStance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardCombatStanceTest {
    @Test
    void wireIdsAreStableAndUnknownIdsAreRejected() {
        assertEquals(0, GuardCombatStance.DEFENSIVE.wireId());
        assertEquals(1, GuardCombatStance.AGGRESSIVE.wireId());
        assertEquals(2, GuardCombatStance.PASSIVE.wireId());
        assertEquals(3, GuardCombatStance.HOLD_POSITION.wireId());
        assertEquals(4, GuardCombatStance.PROTECT_COMMANDER.wireId());
        assertEquals(GuardCombatStance.HOLD_POSITION, GuardCombatStance.fromWireId(3).orElseThrow());
        assertTrue(GuardCombatStance.fromWireId(-1).isEmpty());
        assertTrue(GuardCombatStance.fromWireId(5).isEmpty());
    }
}
