package net.hfstack.rallyguard.order;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class GuardRoutes {
    private GuardRoutes() {
    }

    private static final String ROUTE_PREFIX = "rallyguard:route:v1:";

    public static GuardRouteState get(Entity guard) {
        if (guard == null) return empty();

        for (String tag : guard.getCommandTags()) {
            if (tag.startsWith(ROUTE_PREFIX)) {
                return parse(tag);
            }
        }
        return empty();
    }

    public static void set(Entity guard, GuardRouteState route) {
        if (guard == null) return;
        clearTags(guard);
        if (route.points().isEmpty()) return;
        guard.addCommandTag(serialize(route));
    }

    public static void clear(Entity guard) {
        if (guard == null) return;
        clearTags(guard);
    }

    public static void deactivate(Entity guard) {
        GuardRouteState route = get(guard);
        if (route.points().isEmpty()) return;
        set(guard, route.withActive(false));
    }

    public static void applyToGuard(GuardEntity guard, GuardRouteState route) {
        set(guard, route);
        if (route.active() && route.canRun()) {
            GuardOrders.setWaiting(guard, false);
            guard.setFollowing(false);
            guard.setPatrolling(true);
            guard.setPatrolPos(route.currentPoint());
            guard.getNavigation().stop();
        }
    }

    private static GuardRouteState empty() {
        return new GuardRouteState(false, 0, GuardRouteState.DEFAULT_WAIT_TICKS, 0, List.of());
    }

    private static void clearTags(Entity guard) {
        Set<String> tags = guard.getCommandTags();
        for (String tag : List.copyOf(tags)) {
            if (tag.startsWith(ROUTE_PREFIX)) {
                guard.removeCommandTag(tag);
            }
        }
    }

    private static String serialize(GuardRouteState route) {
        StringBuilder points = new StringBuilder();
        for (int i = 0; i < route.points().size(); i++) {
            if (i > 0) points.append(';');
            BlockPos p = route.points().get(i);
            points.append(p.getX()).append(',').append(p.getY()).append(',').append(p.getZ());
        }

        return ROUTE_PREFIX
                + (route.active() ? 1 : 0) + ":"
                + route.currentIndex() + ":"
                + route.waitTicks() + ":"
                + route.dwellTicks() + ":"
                + points;
    }

    private static GuardRouteState parse(String tag) {
        try {
            String data = tag.substring(ROUTE_PREFIX.length());
            String[] parts = data.split(":", 5);
            if (parts.length < 5) return empty();

            boolean active = "1".equals(parts[0]);
            int currentIndex = Integer.parseInt(parts[1]);
            int waitTicks = Integer.parseInt(parts[2]);
            int dwellTicks = Integer.parseInt(parts[3]);
            List<BlockPos> points = parsePoints(parts[4]);
            return new GuardRouteState(active, currentIndex, waitTicks, dwellTicks, points);
        } catch (RuntimeException ignored) {
            return empty();
        }
    }

    private static List<BlockPos> parsePoints(String raw) {
        List<BlockPos> points = new ArrayList<>();
        if (raw.isBlank()) return points;

        for (String point : raw.split(";")) {
            String[] coords = point.split(",", 3);
            if (coords.length != 3) continue;

            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);
            points.add(new BlockPos(x, y, z));
            if (points.size() >= GuardRouteState.MAX_POINTS) break;
        }
        return points;
    }
}
