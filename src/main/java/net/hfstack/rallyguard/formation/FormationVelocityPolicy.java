package net.hfstack.rallyguard.formation;

public final class FormationVelocityPolicy {
    private static final double CORRECTION_GAIN = 0.08;
    private static final double MAX_CORRECTION = 0.06;
    private static final double MAX_COMMANDER_SPEED = 0.35;
    private static final double MAX_GUARD_SPEED = 0.38;
    private static final double FACING_MOVEMENT_SQUARED = 0.01 * 0.01;
    private static final float MAX_FACING_CHANGE = 18.0F;

    private FormationVelocityPolicy() {
    }

    public static FormationVelocity velocity(
            double commanderX,
            double commanderZ,
            double slotErrorX,
            double slotErrorZ
    ) {
        validate(commanderX, commanderZ, slotErrorX, slotErrorZ);
        double[] commander = clamp(commanderX, commanderZ, MAX_COMMANDER_SPEED);
        double[] correction = clamp(
                slotErrorX * CORRECTION_GAIN,
                slotErrorZ * CORRECTION_GAIN,
                MAX_CORRECTION
        );
        double[] result = clamp(
                commander[0] + correction[0],
                commander[1] + correction[1],
                MAX_GUARD_SPEED
        );
        return new FormationVelocity(result[0], result[1]);
    }

    public static float facingYaw(float currentYaw, FormationVelocity velocity) {
        if (velocity.x() * velocity.x() + velocity.z() * velocity.z() <= FACING_MOVEMENT_SQUARED) {
            return normalize(currentYaw);
        }
        float targetYaw = (float) Math.toDegrees(Math.atan2(-velocity.x(), velocity.z()));
        float difference = wrappedDifference(currentYaw, targetYaw);
        if (Math.abs(difference) <= MAX_FACING_CHANGE) return normalize(targetYaw);
        return normalize(currentYaw + Math.copySign(MAX_FACING_CHANGE, difference));
    }

    private static double[] clamp(double x, double z, double maximum) {
        double lengthSquared = x * x + z * z;
        if (lengthSquared <= maximum * maximum) return new double[]{x, z};
        double scale = maximum / Math.sqrt(lengthSquared);
        return new double[]{x * scale, z * scale};
    }

    private static void validate(double... values) {
        for (double value : values) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Velocity inputs must be finite");
            }
        }
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
