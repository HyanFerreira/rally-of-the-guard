package net.hfstack.rallyguard.formation;

public record FormationMovementPlan(boolean navigate, double speed, double leadDistance) {
    public FormationMovementPlan {
        if (!Double.isFinite(speed) || speed <= 0.0) {
            throw new IllegalArgumentException("speed must be positive and finite");
        }
        if (!Double.isFinite(leadDistance) || leadDistance < 0.0) {
            throw new IllegalArgumentException("leadDistance must be non-negative and finite");
        }
    }
}
