package net.hfstack.rallyguard.formation;

public final class FormationVelocityPolicy {
    private static final double CORRECTION_GAIN = 0.08;
    private static final double MAX_CORRECTION = 0.06;
    private static final double MAX_COMMANDER_SPEED = 0.35;
    private static final double MAX_GUARD_SPEED = 0.38;

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
}
