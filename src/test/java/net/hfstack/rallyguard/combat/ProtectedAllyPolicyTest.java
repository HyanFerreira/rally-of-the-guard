package net.hfstack.rallyguard.combat;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtectedAllyPolicyTest {
    private static final UUID COMMANDER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID TARGET = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID OTHER_PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test
    void protectsCommanderAndMatchingPersistentOwner() {
        assertTrue(ProtectedAllyPolicy.matchesRelationship(
                COMMANDER, COMMANDER, null, false, false
        ));
        assertTrue(ProtectedAllyPolicy.matchesRelationship(
                COMMANDER, TARGET, COMMANDER, false, false
        ));
    }

    @Test
    void protectsCurrentRiderAndDirectLeashHolder() {
        assertTrue(ProtectedAllyPolicy.matchesRelationship(
                COMMANDER, TARGET, null, true, false
        ));
        assertTrue(ProtectedAllyPolicy.matchesRelationship(
                COMMANDER, TARGET, null, false, true
        ));
    }

    @Test
    void doesNotProtectMissingOwnerDifferentOwnerOrUnassociatedTarget() {
        assertFalse(ProtectedAllyPolicy.matchesRelationship(
                COMMANDER, TARGET, null, false, false
        ));
        assertFalse(ProtectedAllyPolicy.matchesRelationship(
                COMMANDER, TARGET, OTHER_PLAYER, false, false
        ));
    }
}
