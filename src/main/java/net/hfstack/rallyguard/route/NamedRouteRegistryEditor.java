package net.hfstack.rallyguard.route;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class NamedRouteRegistryEditor {
    private NamedRouteRegistryEditor() {
    }

    public static EditResult create(NamedRouteRegistry registry, UUID id, String rawName, String dimensionId,
                                    int waitSeconds, List<BlockPos> points, int maximumRoutes,
                                    int maximumNameCodePoints, int maximumPoints) {
        return create(registry, id, rawName, dimensionId, waitSeconds, points, emptyLabels(points), maximumRoutes,
                maximumNameCodePoints, maximumPoints);
    }

    public static EditResult create(NamedRouteRegistry registry, UUID id, String rawName, String dimensionId,
                                    int waitSeconds, List<BlockPos> points, List<String> pointLabels,
                                    int maximumRoutes, int maximumNameCodePoints, int maximumPoints) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(id, "id");
        if (registry.routes().size() >= maximumRoutes) return new Failure(FailureReason.LIMIT_REACHED);
        if (registry.routes().stream().anyMatch(route -> route.id().equals(id))) {
            return new Failure(FailureReason.DUPLICATE_ID);
        }
        Optional<String> name = NamedRouteNamePolicy.normalize(rawName, maximumNameCodePoints);
        if (name.isEmpty()) return new Failure(FailureReason.INVALID_NAME);
        if (NamedRouteNamePolicy.isDuplicate(registry, name.get(), Optional.empty())) {
            return new Failure(FailureReason.DUPLICATE_NAME);
        }
        Optional<NamedRouteDefinition> definition = definition(
                id, name.get(), dimensionId, waitSeconds, points, pointLabels, maximumPoints
        );
        if (definition.isEmpty()) return new Failure(FailureReason.INVALID_ROUTE);
        List<NamedRouteDefinition> routes = new ArrayList<>(registry.routes());
        routes.add(definition.get());
        return new Success(new NamedRouteRegistry(NamedRouteRegistry.CURRENT_SCHEMA_VERSION, routes), definition.get());
    }

    public static EditResult update(NamedRouteRegistry registry, UUID id, String rawName, String dimensionId,
                                    int waitSeconds, List<BlockPos> points, int maximumNameCodePoints,
                                    int maximumPoints) {
        return update(registry, id, rawName, dimensionId, waitSeconds, points, emptyLabels(points),
                maximumNameCodePoints, maximumPoints);
    }

    public static EditResult update(NamedRouteRegistry registry, UUID id, String rawName, String dimensionId,
                                    int waitSeconds, List<BlockPos> points, List<String> pointLabels,
                                    int maximumNameCodePoints, int maximumPoints) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(id, "id");
        int index = indexOf(registry, id);
        if (index < 0) return new Failure(FailureReason.NOT_FOUND);
        Optional<String> name = NamedRouteNamePolicy.normalize(rawName, maximumNameCodePoints);
        if (name.isEmpty()) return new Failure(FailureReason.INVALID_NAME);
        if (NamedRouteNamePolicy.isDuplicate(registry, name.get(), Optional.of(id))) {
            return new Failure(FailureReason.DUPLICATE_NAME);
        }
        Optional<NamedRouteDefinition> definition = definition(
                id, name.get(), dimensionId, waitSeconds, points, pointLabels, maximumPoints
        );
        if (definition.isEmpty()) return new Failure(FailureReason.INVALID_ROUTE);
        List<NamedRouteDefinition> routes = new ArrayList<>(registry.routes());
        routes.set(index, definition.get());
        return new Success(new NamedRouteRegistry(NamedRouteRegistry.CURRENT_SCHEMA_VERSION, routes), definition.get());
    }

    public static EditResult delete(NamedRouteRegistry registry, UUID id) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(id, "id");
        int index = indexOf(registry, id);
        if (index < 0) return new Failure(FailureReason.NOT_FOUND);
        List<NamedRouteDefinition> routes = new ArrayList<>(registry.routes());
        NamedRouteDefinition removed = routes.remove(index);
        return new Success(new NamedRouteRegistry(NamedRouteRegistry.CURRENT_SCHEMA_VERSION, routes), removed);
    }

    private static Optional<NamedRouteDefinition> definition(UUID id, String name, String dimensionId,
                                                              int waitSeconds, List<BlockPos> points,
                                                              List<String> pointLabels,
                                                              int maximumPoints) {
        Identifier dimension = dimensionId == null ? null : Identifier.tryParse(dimensionId);
        if (dimension == null || points == null || maximumPoints < 2 || points.size() < 2
                || points.size() > maximumPoints || pointLabels == null || pointLabels.size() != points.size()
                || waitSeconds < 0 || waitSeconds > 600) {
            return Optional.empty();
        }
        List<String> normalizedLabels = pointLabels.stream().map(NamedRouteRegistryEditor::normalizeLabel).toList();
        return Optional.of(new NamedRouteDefinition(
                id, name, dimension.toString(), waitSeconds, points, normalizedLabels
        ));
    }

    private static List<String> emptyLabels(List<BlockPos> points) {
        if (points == null) return List.of();
        return points.stream().map(point -> "").toList();
    }

    private static String normalizeLabel(String raw) {
        String value = raw == null ? "" : raw.trim();
        int maximum = NamedRouteDefinition.MAX_POINT_LABEL_CODE_POINTS;
        if (value.codePointCount(0, value.length()) <= maximum) return value;
        return value.substring(0, value.offsetByCodePoints(0, maximum));
    }

    private static int indexOf(NamedRouteRegistry registry, UUID id) {
        for (int i = 0; i < registry.routes().size(); i++) {
            if (registry.routes().get(i).id().equals(id)) return i;
        }
        return -1;
    }

    public sealed interface EditResult permits Success, Failure {
    }

    public record Success(NamedRouteRegistry registry, NamedRouteDefinition affected) implements EditResult {
    }

    public record Failure(FailureReason reason) implements EditResult {
    }

    public enum FailureReason {
        INVALID_NAME, DUPLICATE_NAME, DUPLICATE_ID, LIMIT_REACHED, NOT_FOUND, INVALID_ROUTE
    }
}
