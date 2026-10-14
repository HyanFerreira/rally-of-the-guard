package net.hfstack.rallyguard.combat;

import net.hfstack.rallyguard.api.command.GuardCommandScope;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScopedAttackPolicyTest {
    private static final UUID ALPHA = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Test
    void scopeCombinesWithExistingCombatReadiness() {
        assertTrue(ScopedAttackPolicy.mayReceiveOrder(
                GuardCommandScope.squad(ALPHA), Optional.of(ALPHA), true, true, true
        ));
        assertFalse(ScopedAttackPolicy.mayReceiveOrder(
                GuardCommandScope.squad(ALPHA), Optional.empty(), true, true, true
        ));
        assertFalse(ScopedAttackPolicy.mayReceiveOrder(
                GuardCommandScope.all(), Optional.empty(), false, true, true
        ));
        assertFalse(ScopedAttackPolicy.mayReceiveOrder(
                GuardCommandScope.all(), Optional.empty(), true, false, true
        ));
        assertFalse(ScopedAttackPolicy.mayReceiveOrder(
                GuardCommandScope.all(), Optional.empty(), true, true, false
        ));
    }
}
