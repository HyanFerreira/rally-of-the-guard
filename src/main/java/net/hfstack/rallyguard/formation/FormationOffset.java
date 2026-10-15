package net.hfstack.rallyguard.formation;

public record FormationOffset(double right, double forward) {
    public FormationOffset {
        if (!Double.isFinite(right) || !Double.isFinite(forward)) {
            throw new IllegalArgumentException("Formation offsets must be finite");
        }
    }
}
