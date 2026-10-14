package net.hfstack.rallyguard.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardProfilePersistencePolicyTest {
    @Test
    void absentDataCanBeInitialized() {
        GuardProfileReadResult result = new GuardProfileReadResult.Absent();

        assertTrue(GuardProfilePersistencePolicy.canWrite(result));
        assertFalse(GuardProfilePersistencePolicy.shouldCanonicalize(result));
    }

    @Test
    void availableDataCanBeSaved() {
        GuardProfileReadResult result = new GuardProfileReadResult.Available(
                GuardProfile.legacyNeutral(),
                false
        );

        assertTrue(GuardProfilePersistencePolicy.canWrite(result));
    }

    @Test
    void futureSchemaCannotBeOverwritten() {
        GuardProfileReadResult result = new GuardProfileReadResult.FutureSchema(2);

        assertFalse(GuardProfilePersistencePolicy.canWrite(result));
        assertFalse(GuardProfilePersistencePolicy.shouldCanonicalize(result));
    }

    @Test
    void onlyRepairedAvailableDataRequiresCanonicalRewrite() {
        GuardProfileReadResult clean = new GuardProfileReadResult.Available(
                GuardProfile.legacyNeutral(),
                false
        );
        GuardProfileReadResult repaired = new GuardProfileReadResult.Available(
                GuardProfile.legacyNeutral(),
                true
        );

        assertFalse(GuardProfilePersistencePolicy.shouldCanonicalize(clean));
        assertTrue(GuardProfilePersistencePolicy.shouldCanonicalize(repaired));
    }
}
