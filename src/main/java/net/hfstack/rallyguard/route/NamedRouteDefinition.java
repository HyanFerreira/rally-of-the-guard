package net.hfstack.rallyguard.route;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record NamedRouteDefinition(UUID id, String name, String dimensionId, int waitSeconds, List<BlockPos> points,
                                   List<String> pointLabels) {
    public static final int MAX_POINT_LABEL_CODE_POINTS = 32;

    public NamedRouteDefinition {
        id = Objects.requireNonNull(id, "id");
        name = Objects.requireNonNull(name, "name");
        dimensionId = Objects.requireNonNull(dimensionId, "dimensionId");
        points = List.copyOf(points);
        pointLabels = pointLabels == null ? emptyLabels(points.size()) : List.copyOf(pointLabels);
        if (pointLabels.size() != points.size()) {
            throw new IllegalArgumentException("Point labels must match route points");
        }
    }

    public NamedRouteDefinition(UUID id, String name, String dimensionId, int waitSeconds, List<BlockPos> points) {
        this(id, name, dimensionId, waitSeconds, points, emptyLabels(points.size()));
    }

    private static List<String> emptyLabels(int size) {
        return java.util.stream.IntStream.range(0, size).mapToObj(index -> "").toList();
    }
}
