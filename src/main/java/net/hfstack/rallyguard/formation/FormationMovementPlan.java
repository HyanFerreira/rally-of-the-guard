package net.hfstack.rallyguard.formation;

public record FormationMovementPlan(boolean navigate, double speed) {
    public FormationMovementPlan {
        if (!Double.isFinite(speed) || speed <= 0.0) {
            throw new IllegalArgumentException("speed must be positive and finite");
        }
    }
}
