package net.hfstack.rallyguard.formation;

public record FormationMovementPlan(boolean navigate, double speed, double leadMultiplier) {
    public FormationMovementPlan {
        if (!Double.isFinite(speed) || speed <= 0.0) {
            throw new IllegalArgumentException("speed must be positive and finite");
        }
        if (!Double.isFinite(leadMultiplier) || leadMultiplier < 0.0) {
            throw new IllegalArgumentException("leadMultiplier must be non-negative and finite");
        }
    }
}
