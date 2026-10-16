package net.hfstack.rallyguard.route;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record NamedRouteDefinition(UUID id, String name, String dimensionId, int waitSeconds, List<BlockPos> points) {
    public NamedRouteDefinition {
        id = Objects.requireNonNull(id, "id");
        name = Objects.requireNonNull(name, "name");
        dimensionId = Objects.requireNonNull(dimensionId, "dimensionId");
        points = List.copyOf(points);
    }
}
