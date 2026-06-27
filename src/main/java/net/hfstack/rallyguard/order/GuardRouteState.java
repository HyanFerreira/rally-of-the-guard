package net.hfstack.rallyguard.order;

import net.hfstack.rallyguard.config.RallyConfig;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record GuardRouteState(boolean active, int currentIndex, int waitTicks, int dwellTicks, List<BlockPos> points) {
    public GuardRouteState {
        currentIndex = Math.max(0, currentIndex);
        waitTicks = Math.max(0, waitTicks);
        dwellTicks = Math.max(0, dwellTicks);
        points = List.copyOf(points.subList(0, Math.min(points.size(), RallyConfig.routeMaxPoints())));
        if (!points.isEmpty() && currentIndex >= points.size()) {
            currentIndex = 0;
        }
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
        return new GuardRouteState(active, currentIndex, waitTicks, 0, points);
    }

    public GuardRouteState withProgress(int currentIndex, int dwellTicks) {
        return new GuardRouteState(active, currentIndex, waitTicks, dwellTicks, points);
    }
}
