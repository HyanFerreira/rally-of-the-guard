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

    public static void execute(Decision decision, Runnable navigate, Runnable arrive) {
        switch (decision) {
            case PAUSE_FOR_COMBAT -> {
                // Combat AI retains control of navigation while the move order is paused.
            }
            case NAVIGATE -> navigate.run();
            case ARRIVE -> arrive.run();
        }
    }
}
