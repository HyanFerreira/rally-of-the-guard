package net.hfstack.rallyguard.formation;

public final class FormationMovementPolicy {
    private static final double HOLD_DISTANCE_SQUARED = 0.75 * 0.75;
    private static final double WALKING_SPEED_FLOOR = 1.0;
    private static final double SPRINTING_SPEED_FLOOR = 1.2;
    private static final double MAX_MOVING_SPEED_FLOOR = 1.35;
    private static final double CATCH_UP_GAIN = 0.12;
    private static final double WALKING_LEAD_DISTANCE = 1.5;
    private static final double SPRINTING_LEAD_DISTANCE = 2.0;

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
        double distance = Math.sqrt(distanceSquared);
        double catchUp = Math.max(0.0, distance - Math.sqrt(HOLD_DISTANCE_SQUARED)) * CATCH_UP_GAIN;
        speedFloor = Math.min(MAX_MOVING_SPEED_FLOOR, speedFloor + catchUp);
        return new FormationMovementPlan(
                true,
                Math.max(configuredSpeed, speedFloor),
                commanderSprinting ? SPRINTING_LEAD_DISTANCE : WALKING_LEAD_DISTANCE
        );
    }
}
