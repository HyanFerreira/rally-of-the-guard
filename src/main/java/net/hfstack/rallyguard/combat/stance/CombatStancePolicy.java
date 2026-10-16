package net.hfstack.rallyguard.combat.stance;

import net.minecraft.core.BlockPos;

public final class CombatStancePolicy {
    public static final int RETURN_MARGIN = 2;

    private CombatStancePolicy() {
    }

    public static Decision decide(
            GuardCombatStance stance,
            boolean hasLiveTarget,
            boolean targetWithinRadius,
            boolean guardBeyondReturnBoundary
    ) {
        if (stance == GuardCombatStance.PASSIVE) {
            return hasLiveTarget ? Decision.DISENGAGE : Decision.NONE;
        }
        if (stance != GuardCombatStance.HOLD_POSITION) return Decision.NONE;
        if (hasLiveTarget && !targetWithinRadius) {
            return guardBeyondReturnBoundary ? Decision.DISENGAGE_AND_RETURN : Decision.DISENGAGE;
        }
        if (!hasLiveTarget && guardBeyondReturnBoundary) return Decision.RETURN_TO_ANCHOR;
        return Decision.NONE;
    }

    public static boolean withinRadius(BlockPos anchor, double x, double y, double z, int radius) {
        return distanceSquared(anchor, x, y, z) <= (double) radius * radius;
    }

    public static boolean beyondReturnBoundary(BlockPos anchor, double x, double y, double z, int radius) {
        double boundary = radius + RETURN_MARGIN;
        return distanceSquared(anchor, x, y, z) > boundary * boundary;
    }

    private static double distanceSquared(BlockPos anchor, double x, double y, double z) {
        double dx = x - (anchor.getX() + 0.5);
        double dy = y - (anchor.getY() + 0.5);
        double dz = z - (anchor.getZ() + 0.5);
        return dx * dx + dy * dy + dz * dz;
    }

    public enum Decision {
        NONE,
        DISENGAGE,
        DISENGAGE_AND_RETURN,
        RETURN_TO_ANCHOR
    }
}
