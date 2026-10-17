package net.hfstack.rallyguard.order;

import java.util.OptionalDouble;

public final class GuardFollowAssistPolicy {
    private static final double NATIVE_FOLLOW_DISTANCE_SQUARED = 3.0 * 3.0;
    private static final double CATCH_UP_DISTANCE_SQUARED = 5.0 * 5.0;
    private static final double SPRINT_ASSIST_SPEED = 0.95;
    public static final double CATCH_UP_SPEED = 1.15;

    private GuardFollowAssistPolicy() {
    }

    public static OptionalDouble speed(
            boolean following,
            boolean rallied,
            boolean fighting,
            boolean commanderSprinting,
            boolean commanderMounted,
            double distanceSquared
    ) {
        if (!Double.isFinite(distanceSquared) || distanceSquared < 0.0) {
            throw new IllegalArgumentException("distanceSquared must be non-negative and finite");
        }
        if (!following
                || rallied
                || fighting
                || !commanderSprinting
                || commanderMounted
                || distanceSquared <= NATIVE_FOLLOW_DISTANCE_SQUARED) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(distanceSquared > CATCH_UP_DISTANCE_SQUARED
                ? CATCH_UP_SPEED
                : SPRINT_ASSIST_SPEED);
    }
}
