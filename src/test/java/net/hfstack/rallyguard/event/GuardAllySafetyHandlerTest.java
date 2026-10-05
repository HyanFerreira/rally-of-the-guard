package net.hfstack.rallyguard.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardAllySafetyHandlerTest {
    @Test
    void onlyOwnedGuardDisengagesFromProtectedTarget() {
        assertTrue(GuardAllySafetyHandler.shouldDisengage(true, true));
        assertFalse(GuardAllySafetyHandler.shouldDisengage(false, true));
        assertFalse(GuardAllySafetyHandler.shouldDisengage(true, false));
    }

    @Test
    void onlyOwnedGuardDamageToProtectedVictimIsCancelled() {
        assertTrue(GuardAllySafetyHandler.shouldCancelDamage(true, true));
        assertFalse(GuardAllySafetyHandler.shouldCancelDamage(false, true));
        assertFalse(GuardAllySafetyHandler.shouldCancelDamage(true, false));
    }
}
