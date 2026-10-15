package net.hfstack.rallyguard.order.defend;

import net.minecraft.core.BlockPos;

import java.util.Objects;

public final class DefendAreaBoundaryPolicy {
    public static final double RETURN_MARGIN = 2.0;

    public enum Decision {
        PATROL,
        KEEP_COMBAT,
        DISENGAGE,
        RETURN_TO_POST,
        DISENGAGE_AND_RETURN
    }

    private DefendAreaBoundaryPolicy() {
    }

    public static Decision decide(boolean hasTarget, boolean targetAllowed, boolean beyondReturnBoundary) {
        if (hasTarget && targetAllowed) return Decision.KEEP_COMBAT;
        if (hasTarget) {
            return beyondReturnBoundary ? Decision.DISENGAGE_AND_RETURN : Decision.DISENGAGE;
        }
        return beyondReturnBoundary ? Decision.RETURN_TO_POST : Decision.PATROL;
    }

    public static boolean withinRadius(BlockPos center, double x, double y, double z, int radius) {
        return distanceSquared(Objects.requireNonNull(center, "center"), x, y, z)
                <= (double) radius * radius;
    }

    public static boolean beyondReturnBoundary(BlockPos center, double x, double y, double z, int radius) {
        double boundary = radius + RETURN_MARGIN;
        return distanceSquared(Objects.requireNonNull(center, "center"), x, y, z) > boundary * boundary;
    }

    private static double distanceSquared(BlockPos center, double x, double y, double z) {
        double dx = x - (center.getX() + 0.5);
        double dy = y - (center.getY() + 0.5);
        double dz = z - (center.getZ() + 0.5);
        return dx * dx + dy * dy + dz * dz;
    }
}
