package net.hfstack.rallyguard.formation;

public final class FormationHeadingPolicy {
    private static final double MOVEMENT_SQUARED = 0.02 * 0.02;
    private static final float MAX_TURN_DEGREES = 10.0F;
    private static final float DECISIVE_TURN_DEGREES = 60.0F;

    private FormationHeadingPolicy() {
    }

    public static FormationHeadingState update(
            FormationHeadingState current,
            double movementX,
            double movementZ,
            double lateralMargin
    ) {
        if (!Double.isFinite(movementX)
                || !Double.isFinite(movementZ)
                || !Double.isFinite(lateralMargin)
                || lateralMargin < 0.0) {
            throw new IllegalArgumentException("Heading inputs must be finite and margin non-negative");
        }
        if (movementX * movementX + movementZ * movementZ <= MOVEMENT_SQUARED) {
            return new FormationHeadingState(current.yawDegrees(), 0.0, false);
        }

        double yawRadians = Math.toRadians(current.yawDegrees());
        double rightX = Math.cos(yawRadians);
        double rightZ = Math.sin(yawRadians);
        double drift = current.lateralDrift() + movementX * rightX + movementZ * rightZ;
        float desiredYaw = (float) Math.toDegrees(Math.atan2(-movementX, movementZ));
        boolean decisiveTurn = Math.abs(wrappedDifference(current.yawDegrees(), desiredYaw))
                >= DECISIVE_TURN_DEGREES;
        boolean adapting = current.adapting() || decisiveTurn || Math.abs(drift) > lateralMargin;
        if (!adapting) {
            return new FormationHeadingState(current.yawDegrees(), drift, false);
        }

        float updatedYaw = approachDegrees(current.yawDegrees(), desiredYaw, MAX_TURN_DEGREES);
        boolean aligned = Math.abs(wrappedDifference(updatedYaw, desiredYaw)) < 0.01F;
        double boundedDrift = aligned
                ? 0.0
                : Math.max(-lateralMargin * 2.0, Math.min(lateralMargin * 2.0, drift));
        return new FormationHeadingState(updatedYaw, boundedDrift, !aligned);
    }

    private static float approachDegrees(float current, float target, float maximumChange) {
        float difference = wrappedDifference(current, target);
        if (Math.abs(difference) <= maximumChange) return normalize(target);
        return normalize(current + Math.copySign(maximumChange, difference));
    }

    private static float wrappedDifference(float current, float target) {
        float difference = normalize(target) - normalize(current);
        if (difference > 180.0F) difference -= 360.0F;
        if (difference < -180.0F) difference += 360.0F;
        return difference;
    }

    private static float normalize(float degrees) {
        float normalized = degrees % 360.0F;
        if (normalized >= 180.0F) normalized -= 360.0F;
        if (normalized < -180.0F) normalized += 360.0F;
        return normalized;
    }
}
