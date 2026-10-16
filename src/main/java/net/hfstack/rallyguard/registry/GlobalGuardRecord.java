package net.hfstack.rallyguard.registry;

import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.UUID;

public record GlobalGuardRecord(
        UUID guardId,
        String name,
        String dimensionId,
        BlockPos lastPosition,
        long lastSeenGameTime,
        int level,
        String rankId,
        boolean captain,
        Optional<String> squadName,
        int orderStatus
) {
    public GlobalGuardRecord {
        if (guardId == null || lastPosition == null) throw new IllegalArgumentException("Guard identity and position are required");
        name = name == null ? "" : name;
        dimensionId = dimensionId == null ? "" : dimensionId;
        lastPosition = lastPosition.immutable();
        level = Math.max(0, level);
        rankId = rankId == null ? "" : rankId;
        squadName = squadName == null ? Optional.empty() : squadName;
        orderStatus = Math.max(0, orderStatus);
    }
}
