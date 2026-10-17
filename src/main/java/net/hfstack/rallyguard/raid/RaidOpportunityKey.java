package net.hfstack.rallyguard.raid;

import net.minecraft.core.BlockPos;

public record RaidOpportunityKey(int raidId, String dimensionId, BlockPos center) {
    public RaidOpportunityKey {
        if (raidId < 0) throw new IllegalArgumentException("raidId must be non-negative");
        if (dimensionId == null || dimensionId.isBlank()) throw new IllegalArgumentException("dimensionId is required");
        if (center == null) throw new IllegalArgumentException("center is required");
        center = center.immutable();
    }
}
