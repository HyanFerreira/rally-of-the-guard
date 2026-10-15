package net.hfstack.rallyguard.formation;

public record FormationHeadingState(float yawDegrees, double lateralDrift, boolean adapting) {
    public FormationHeadingState {
        if (!Float.isFinite(yawDegrees) || !Double.isFinite(lateralDrift)) {
            throw new IllegalArgumentException("Formation heading state must be finite");
        }
    }

    public static FormationHeadingState initial(float yawDegrees) {
        return new FormationHeadingState(yawDegrees, 0.0, false);
    }
}
