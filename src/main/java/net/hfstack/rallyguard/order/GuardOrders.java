package net.hfstack.rallyguard.order;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.entity.Entity;

public final class GuardOrders {
    private GuardOrders() {
    }

    private static final String WAITING_TAG = "rallyguard:waiting";

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

    public static int statusOf(GuardEntity guard) {
        if (guard.isPatrolling()) return GuardOrderStatus.PATROLLING;
        if (isWaiting(guard)) return GuardOrderStatus.WAITING;
        if (guard.isFollowing()) return GuardOrderStatus.FOLLOWING;
        return GuardOrderStatus.IDLE;
    }
}
