package net.hfstack.rallyguard.profile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GuardProfileInitializationPolicyTest {

    @Test
    void absentGuardProfileRequiresInitialization() {
        assertTrue(GuardProfileInitializationPolicy.shouldInitialize(
                true,
                new GuardProfileReadResult.Absent()));
    }

    @Test
    void availableGuardProfileDoesNotRequireInitialization() {
        assertFalse(GuardProfileInitializationPolicy.shouldInitialize(
                true,
                new GuardProfileReadResult.Available(GuardProfile.legacyNeutral(), false)));
    }

    @Test
    void futureGuardProfileDoesNotRequireInitialization() {
        assertFalse(GuardProfileInitializationPolicy.shouldInitialize(
                true,
                new GuardProfileReadResult.FutureSchema(GuardProfile.CURRENT_SCHEMA_VERSION + 1)));
    }

    @Test
    void nonGuardEntityNeverRequiresInitialization() {
        assertFalse(GuardProfileInitializationPolicy.shouldInitialize(
                false,
                new GuardProfileReadResult.Absent()));
    }

    @Test
    void repeatedDecisionForAvailableProfileIsIdempotent() {
        GuardProfileReadResult current =
                new GuardProfileReadResult.Available(GuardProfile.legacyNeutral(), false);

        assertFalse(GuardProfileInitializationPolicy.shouldInitialize(true, current));
        assertFalse(GuardProfileInitializationPolicy.shouldInitialize(true, current));
    }
}
