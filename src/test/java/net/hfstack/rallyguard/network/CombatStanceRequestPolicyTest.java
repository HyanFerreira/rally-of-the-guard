package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.combat.stance.GuardCombatStance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatStanceRequestPolicyTest {
    @Test
    void acceptsOnlyKnownStancesInAValidScope() {
        assertEquals(
                GuardCombatStance.PROTECT_COMMANDER,
                CombatStanceRequestPolicy.resolve(4, true).orElseThrow()
        );
        assertTrue(CombatStanceRequestPolicy.resolve(99, true).isEmpty());
        assertTrue(CombatStanceRequestPolicy.resolve(0, false).isEmpty());
    }
}
