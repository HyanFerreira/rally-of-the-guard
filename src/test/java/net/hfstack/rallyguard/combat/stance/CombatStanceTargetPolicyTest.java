package net.hfstack.rallyguard.combat.stance;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatStanceTargetPolicyTest {
    @Test
    void acquisitionRejectsDeadAndProtectedTargets() {
        assertTrue(CombatStanceTargetPolicy.canAcquire(true, false));
        assertFalse(CombatStanceTargetPolicy.canAcquire(false, false));
        assertFalse(CombatStanceTargetPolicy.canAcquire(true, true));
    }

    @Test
    void nearestTargetWinsAndUuidBreaksEqualDistanceTies() {
        UUID low = new UUID(0, 1);
        UUID high = new UUID(0, 2);

        assertTrue(CombatStanceTargetPolicy.preferCandidate(4.0, high, 9.0, low));
        assertTrue(CombatStanceTargetPolicy.preferCandidate(4.0, low, 4.0, high));
        assertFalse(CombatStanceTargetPolicy.preferCandidate(4.0, high, 4.0, low));
    }
}
