package net.hfstack.rallyguard.order;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.entity.Entity;

public final class GuardOrders {
    private GuardOrders() {
    }

    private static final String WAITING_TAG = "rallyguard:waiting";
    private static final String RALLIED_TAG = "rallyguard:rallied";

    public static boolean isWaiting(Entity guard) {
        return guard != null && guard.getCommandTags().contains(WAITING_TAG);
    }

    public static void setWaiting(Entity guard, boolean waiting) {
        if (guard == null) return;

        if (waiting) {
            guard.addCommandTag(WAITING_TAG);
        } else {
            guard.removeCommandTag(WAITING_TAG);
        }
    }

    public static boolean isRallied(Entity guard) {
        return guard != null && guard.getCommandTags().contains(RALLIED_TAG);
    }

    public static void setRallied(Entity guard, boolean rallied) {
        if (guard == null) return;

        if (rallied) {
            guard.addCommandTag(RALLIED_TAG);
        } else {
            guard.removeCommandTag(RALLIED_TAG);
        }
    }

    public static int statusOf(GuardEntity guard) {
        if (GuardRoutes.get(guard).active()) return GuardOrderStatus.ROUTING;
        if (guard.isPatrolling()) return GuardOrderStatus.PATROLLING;
        if (isWaiting(guard)) return GuardOrderStatus.WAITING;
        if (isRallied(guard)) return GuardOrderStatus.FOLLOWING;
        if (guard.isFollowing()) return GuardOrderStatus.FOLLOWING;
        return GuardOrderStatus.IDLE;
    }
}
