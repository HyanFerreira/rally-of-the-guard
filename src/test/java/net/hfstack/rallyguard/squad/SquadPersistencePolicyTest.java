package net.hfstack.rallyguard.squad;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SquadPersistencePolicyTest {
    @Test
    void onlyFutureSchemasAreProtectedFromWrites() {
        assertTrue(SquadPersistencePolicy.canWrite(new SquadRegistryReadResult.Absent()));
        assertTrue(SquadPersistencePolicy.canWrite(new SquadRegistryReadResult.Available(SquadRegistry.empty(), false)));
        assertFalse(SquadPersistencePolicy.canWrite(new SquadRegistryReadResult.FutureSchema(2)));

        assertTrue(SquadPersistencePolicy.canWrite(new GuardSquadAssignmentReadResult.Absent()));
        assertTrue(SquadPersistencePolicy.canWrite(new GuardSquadAssignmentReadResult.Available(
                GuardSquadAssignment.unassigned(), false
        )));
        assertFalse(SquadPersistencePolicy.canWrite(new GuardSquadAssignmentReadResult.FutureSchema(2)));
    }

    @Test
    void repairedSupportedDataNeedsCanonicalization() {
        assertTrue(SquadPersistencePolicy.shouldCanonicalize(
                new SquadRegistryReadResult.Available(SquadRegistry.empty(), true)
        ));
        assertFalse(SquadPersistencePolicy.shouldCanonicalize(
                new SquadRegistryReadResult.Available(SquadRegistry.empty(), false)
        ));
        assertTrue(SquadPersistencePolicy.shouldCanonicalize(
                new GuardSquadAssignmentReadResult.Available(GuardSquadAssignment.unassigned(), true)
        ));
    }
}
