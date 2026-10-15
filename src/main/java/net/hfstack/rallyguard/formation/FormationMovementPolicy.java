package net.hfstack.rallyguard.formation;

public final class FormationMovementPolicy {
    private static final double HOLD_DISTANCE_SQUARED = 0.75 * 0.75;
    private static final double CATCH_UP_DISTANCE_SQUARED = 4.0 * 4.0;
    private static final double WALKING_SPEED_FLOOR = 1.0;
    private static final double SPRINTING_SPEED_FLOOR = 1.2;
    private static final double CATCH_UP_SPEED_FLOOR = 1.35;
    private static final double WALKING_LEAD_MULTIPLIER = 1.0;
    private static final double SPRINTING_LEAD_MULTIPLIER = 1.5;

    private FormationMovementPolicy() {
    }

    public static FormationMovementPlan plan(
            boolean commanderMoving,
            boolean commanderSprinting,
            double distanceSquared,
            double configuredSpeed
    ) {
        if (!Double.isFinite(distanceSquared) || distanceSquared < 0.0) {
            throw new IllegalArgumentException("distanceSquared must be non-negative and finite");
        }
        if (!Double.isFinite(configuredSpeed) || configuredSpeed <= 0.0) {
            throw new IllegalArgumentException("configuredSpeed must be positive and finite");
        }
        if (!commanderMoving) {
            return new FormationMovementPlan(
                    distanceSquared > HOLD_DISTANCE_SQUARED,
                    configuredSpeed,
                    0.0
            );
        }

        double speedFloor = commanderSprinting
                ? SPRINTING_SPEED_FLOOR
                : WALKING_SPEED_FLOOR;
        if (distanceSquared > CATCH_UP_DISTANCE_SQUARED) {
            speedFloor = CATCH_UP_SPEED_FLOOR;
        }
        return new FormationMovementPlan(
                true,
                Math.max(configuredSpeed, speedFloor),
                commanderSprinting ? SPRINTING_LEAD_MULTIPLIER : WALKING_LEAD_MULTIPLIER
        );
    }
}
