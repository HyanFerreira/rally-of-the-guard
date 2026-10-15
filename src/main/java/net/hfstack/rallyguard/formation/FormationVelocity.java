package net.hfstack.rallyguard.formation;

public record FormationVelocity(double x, double z) {
    public FormationVelocity {
        if (!Double.isFinite(x) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("Formation velocity must be finite");
        }
    }
}
