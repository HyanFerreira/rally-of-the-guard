package net.hfstack.rallyguard.order;

public final class PatrolMovementPolicy {
    public static final int LONG_ROUTE_WAIT_TICKS = 10 * 20;
    public static final double MAX_WALKING_SPEED = 0.6;
    public static final double IDLE_STEP_SPEED = 0.35;

    private PatrolMovementPolicy() {
    }

    public static double walkingSpeed(double configuredSpeed) {
        return Math.max(0.1, Math.min(MAX_WALKING_SPEED, configuredSpeed));
    }

    public static boolean allowsIdleStep(boolean route, int waitTicks) {
        return !route || waitTicks > LONG_ROUTE_WAIT_TICKS;
    }
}
