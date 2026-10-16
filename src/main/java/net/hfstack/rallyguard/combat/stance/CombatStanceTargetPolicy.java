package net.hfstack.rallyguard.combat.stance;

import java.util.Objects;
import java.util.UUID;

public final class CombatStanceTargetPolicy {
    private CombatStanceTargetPolicy() {
    }

    public static boolean canAcquire(boolean alive, boolean protectedAlly) {
        return alive && !protectedAlly;
    }

    public static boolean preferCandidate(
            double candidateDistanceSquared,
            UUID candidateId,
            double currentDistanceSquared,
            UUID currentId
    ) {
        Objects.requireNonNull(candidateId, "candidateId");
        Objects.requireNonNull(currentId, "currentId");
        int distanceOrder = Double.compare(candidateDistanceSquared, currentDistanceSquared);
        return distanceOrder < 0 || distanceOrder == 0 && candidateId.compareTo(currentId) < 0;
    }
}
