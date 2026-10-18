package net.hfstack.rallyguard.network;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.function.Predicate;

public final class GuardRouteUpdatePolicy {
    public static final double CAPTURE_RADIUS_SQUARED = 8.0 * 8.0;
    public static final String INVALID_REQUEST_KEY = "gui.rallyguard.route.invalid_request";
    public static final String INVALID_POINT_KEY = "gui.rallyguard.route.invalid_point";

    private static final Validation ACCEPTED = new Validation(true, "");
    private static final Validation INVALID_REQUEST = new Validation(false, INVALID_REQUEST_KEY);
    private static final Validation INVALID_POINT = new Validation(false, INVALID_POINT_KEY);

    private GuardRouteUpdatePolicy() {
    }

    public static Validation validate(int action, List<BlockPos> submittedPoints, List<BlockPos> storedPoints,
                                      BlockPos playerPosition, int minY, int maxYExclusive,
                                      Predicate<BlockPos> withinWorldBorder, double captureRadiusSquared,
                                      int maxPoints) {
        if (!isKnownAction(action) || submittedPoints == null || storedPoints == null
                || playerPosition == null || withinWorldBorder == null
                || captureRadiusSquared < 0.0 || maxPoints < 0
                || submittedPoints.size() > maxPoints) {
            return INVALID_REQUEST;
        }
        if (action == NetworkConstants.ROUTE_START && submittedPoints.size() < 2) {
            return INVALID_REQUEST;
        }
        if (action == NetworkConstants.ROUTE_CLEAR && !submittedPoints.isEmpty()) {
            return INVALID_REQUEST;
        }

        for (BlockPos point : submittedPoints) {
            if (point == null || point.getY() < minY || point.getY() >= maxYExclusive
                    || !withinWorldBorder.test(point)) {
                return INVALID_POINT;
            }
            if (!storedPoints.contains(point) && squaredDistance(playerPosition, point) > captureRadiusSquared) {
                return INVALID_POINT;
            }
        }
        return ACCEPTED;
    }

    private static boolean isKnownAction(int action) {
        return action == NetworkConstants.ROUTE_SAVE
                || action == NetworkConstants.ROUTE_START
                || action == NetworkConstants.ROUTE_PAUSE
                || action == NetworkConstants.ROUTE_CLEAR;
    }

    private static double squaredDistance(BlockPos first, BlockPos second) {
        double dx = (double) first.getX() - second.getX();
        double dy = (double) first.getY() - second.getY();
        double dz = (double) first.getZ() - second.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    public record Validation(boolean accepted, String translationKey) {
    }
}
