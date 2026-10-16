package net.hfstack.rallyguard.order;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.config.RallyConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class GuardRoutes {
    private GuardRoutes() {
    }

    private static final String ROUTE_PREFIX = "rallyguard:route:";
    private static final String ROUTE_PREFIX_V1 = ROUTE_PREFIX + "v1:";
    private static final String ROUTE_PREFIX_V2 = ROUTE_PREFIX + "v2:";

    public static GuardRouteState get(Entity guard) {
        if (guard == null) return empty();

        for (String tag : guard.entityTags()) {
            if (tag.startsWith(ROUTE_PREFIX_V2) || tag.startsWith(ROUTE_PREFIX_V1)) {
                return parse(tag);
            }
        }
        return empty();
    }

    public static void set(Entity guard, GuardRouteState route) {
        if (guard == null) return;
        clearTags(guard);
        if (route.points().isEmpty()) return;
        guard.addTag(serialize(route));
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
        return new GuardRouteState(false, 0, GuardRouteState.defaultWaitTicks(), 0, List.of(), Optional.empty());
    }

    private static void clearTags(Entity guard) {
        Set<String> tags = guard.entityTags();
        for (String tag : List.copyOf(tags)) {
            if (tag.startsWith(ROUTE_PREFIX)) {
                guard.removeTag(tag);
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

        return ROUTE_PREFIX_V2
                + (route.active() ? 1 : 0) + ":"
                + route.currentIndex() + ":"
                + route.waitTicks() + ":"
                + route.dwellTicks() + ":"
                + route.namedRouteId().map(UUID::toString).orElse("-") + ":"
                + points;
    }

    static GuardRouteState parse(String tag) {
        if (tag.startsWith(ROUTE_PREFIX_V2)) return parseV2(tag);
        if (tag.startsWith(ROUTE_PREFIX_V1)) return parseV1(tag);
        return empty();
    }

    private static GuardRouteState parseV1(String tag) {
        try {
            String data = tag.substring(ROUTE_PREFIX_V1.length());
            String[] parts = data.split(":", 5);
            if (parts.length < 5) return empty();

            boolean active = "1".equals(parts[0]);
            int currentIndex = Integer.parseInt(parts[1]);
            int waitTicks = Integer.parseInt(parts[2]);
            int dwellTicks = Integer.parseInt(parts[3]);
            List<BlockPos> points = parsePoints(parts[4]);
            return new GuardRouteState(active, currentIndex, waitTicks, dwellTicks, points, Optional.empty());
        } catch (RuntimeException ignored) {
            return empty();
        }
    }

    private static GuardRouteState parseV2(String tag) {
        try {
            String data = tag.substring(ROUTE_PREFIX_V2.length());
            String[] parts = data.split(":", 6);
            if (parts.length < 6) return empty();

            boolean active = "1".equals(parts[0]);
            int currentIndex = Integer.parseInt(parts[1]);
            int waitTicks = Integer.parseInt(parts[2]);
            int dwellTicks = Integer.parseInt(parts[3]);
            Optional<UUID> namedRouteId = "-".equals(parts[4])
                    ? Optional.empty() : Optional.of(UUID.fromString(parts[4]));
            List<BlockPos> points = parsePoints(parts[5]);
            return new GuardRouteState(active, currentIndex, waitTicks, dwellTicks, points, namedRouteId);
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
            if (points.size() >= RallyConfig.routeMaxPoints()) break;
        }
        return points;
    }
}
