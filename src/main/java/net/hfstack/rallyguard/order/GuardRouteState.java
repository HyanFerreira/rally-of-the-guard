package net.hfstack.rallyguard.order;

import net.hfstack.rallyguard.config.RallyConfig;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record GuardRouteState(boolean active, int currentIndex, int waitTicks, int dwellTicks, List<BlockPos> points,
                              Optional<UUID> namedRouteId, List<String> pointLabels) {
    public GuardRouteState {
        currentIndex = Math.max(0, currentIndex);
        waitTicks = Math.max(0, waitTicks);
        dwellTicks = Math.max(0, dwellTicks);
        points = List.copyOf(points.subList(0, Math.min(points.size(), RallyConfig.routeMaxPoints())));
        namedRouteId = namedRouteId == null ? Optional.empty() : namedRouteId;
        pointLabels = RoutePointLabels.normalize(pointLabels, points.size());
        if (!points.isEmpty() && currentIndex >= points.size()) {
            currentIndex = 0;
        }
    }

    public GuardRouteState(boolean active, int currentIndex, int waitTicks, int dwellTicks, List<BlockPos> points) {
        this(active, currentIndex, waitTicks, dwellTicks, points, Optional.empty(), List.of());
    }

    public GuardRouteState(boolean active, int currentIndex, int waitTicks, int dwellTicks, List<BlockPos> points,
                           Optional<UUID> namedRouteId) {
        this(active, currentIndex, waitTicks, dwellTicks, points, namedRouteId, List.of());
    }

    public static int defaultWaitTicks() {
        return RallyConfig.routeDefaultWaitSeconds() * 20;
    }

    public boolean canRun() {
        return points.size() >= 2;
    }

    public BlockPos currentPoint() {
        if (points.isEmpty()) return null;
        return points.get(Math.min(currentIndex, points.size() - 1));
    }

    public GuardRouteState withActive(boolean active) {
        return new GuardRouteState(active, currentIndex, waitTicks, 0, points, namedRouteId, pointLabels);
    }

    public GuardRouteState withProgress(int currentIndex, int dwellTicks) {
        return new GuardRouteState(active, currentIndex, waitTicks, dwellTicks, points, namedRouteId, pointLabels);
    }

    public GuardRouteState withNamedRouteId(Optional<UUID> routeId) {
        return new GuardRouteState(active, currentIndex, waitTicks, dwellTicks, points, routeId, pointLabels);
    }
}
