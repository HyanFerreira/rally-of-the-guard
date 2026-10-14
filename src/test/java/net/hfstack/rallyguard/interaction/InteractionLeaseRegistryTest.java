package net.hfstack.rallyguard.interaction;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractionLeaseRegistryTest {
    @Test
    void onlyFirstViewerAcquiresAndLastViewerReleasesGuard() {
        InteractionLeaseRegistry<String, String> leases = new InteractionLeaseRegistry<>();

        assertTrue(leases.acquire("guard", "alice"));
        assertFalse(leases.acquire("guard", "bob"));
        assertFalse(leases.release("guard", "alice"));
        assertTrue(leases.release("guard", "bob"));
    }

    @Test
    void duplicateAcquisitionBySameViewerDoesNotRequireDuplicateRelease() {
        InteractionLeaseRegistry<String, String> leases = new InteractionLeaseRegistry<>();

        assertTrue(leases.acquire("guard", "alice"));
        assertFalse(leases.acquire("guard", "alice"));
        assertTrue(leases.release("guard", "alice"));
        assertFalse(leases.release("guard", "alice"));
    }

    @Test
    void releasingViewerFromEveryGuardReturnsOnlyNewlyUnheldGuards() {
        InteractionLeaseRegistry<String, String> leases = new InteractionLeaseRegistry<>();
        leases.acquire("guard-a", "alice");
        leases.acquire("guard-a", "bob");
        leases.acquire("guard-b", "alice");

        assertEquals(Set.of("guard-b"), leases.releaseAll("alice"));
        assertTrue(leases.release("guard-a", "bob"));
    }

    @Test
    void clearReturnsAllHeldGuards() {
        InteractionLeaseRegistry<String, String> leases = new InteractionLeaseRegistry<>();
        leases.acquire("guard-a", "alice");
        leases.acquire("guard-b", "bob");

        assertEquals(Set.of("guard-a", "guard-b"), leases.clear());
        assertFalse(leases.release("guard-a", "alice"));
    }
}
