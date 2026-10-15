package net.hfstack.rallyguard.order.move;

public final class MoveOrderMovementPolicy {
    private static final double ARRIVAL_DISTANCE_SQUARED = 2.5 * 2.5;

    public enum Decision {
        PAUSE_FOR_COMBAT,
        NAVIGATE,
        ARRIVE
    }

    private MoveOrderMovementPolicy() {
    }

    public static Decision decide(boolean hasCombatTarget, double distanceSquared) {
        if (hasCombatTarget) return Decision.PAUSE_FOR_COMBAT;
        return distanceSquared <= ARRIVAL_DISTANCE_SQUARED ? Decision.ARRIVE : Decision.NAVIGATE;
    }
}
