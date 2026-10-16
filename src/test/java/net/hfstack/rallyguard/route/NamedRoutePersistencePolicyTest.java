package net.hfstack.rallyguard.route;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamedRoutePersistencePolicyTest {
    @Test
    void futureSchemaCannotBeOverwritten() {
        assertFalse(NamedRoutePersistencePolicy.canWrite(new NamedRouteRegistryReadResult.FutureSchema(2)));
        assertTrue(NamedRoutePersistencePolicy.canWrite(new NamedRouteRegistryReadResult.Absent()));
        assertTrue(NamedRoutePersistencePolicy.canWrite(
                new NamedRouteRegistryReadResult.Available(NamedRouteRegistry.empty(), false)
        ));
    }

    @Test
    void onlyKnownRepairedDataShouldBeCanonicalized() {
        assertTrue(NamedRoutePersistencePolicy.shouldCanonicalize(
                new NamedRouteRegistryReadResult.Available(NamedRouteRegistry.empty(), true)
        ));
        assertFalse(NamedRoutePersistencePolicy.shouldCanonicalize(
                new NamedRouteRegistryReadResult.Available(NamedRouteRegistry.empty(), false)
        ));
        assertFalse(NamedRoutePersistencePolicy.shouldCanonicalize(new NamedRouteRegistryReadResult.FutureSchema(2)));
    }
}
