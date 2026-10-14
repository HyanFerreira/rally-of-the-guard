package net.hfstack.rallyguard.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardMeleeBackstepPolicyTest {
    @Test
    void preventsContinuousBackstepWhenGuardHasNoShield() {
        assertFalse(GuardMeleeBackstepPolicy.shouldBackstep(false, false));
    }

    @Test
    void preservesGuardVillagersBackstepWhileShieldIsAvailableAndLowered() {
        assertTrue(GuardMeleeBackstepPolicy.shouldBackstep(true, false));
    }

    @Test
    void doesNotBackstepWhileShieldIsAlreadyRaised() {
        assertFalse(GuardMeleeBackstepPolicy.shouldBackstep(true, true));
    }
}
