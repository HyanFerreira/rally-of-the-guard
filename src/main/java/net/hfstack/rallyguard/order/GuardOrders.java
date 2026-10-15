package net.hfstack.rallyguard.order;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.order.move.MoveOrders;
import net.minecraft.world.entity.Entity;

public final class GuardOrders {
    private GuardOrders() {
    }

    private static final String WAITING_TAG = "rallyguard:waiting";
    private static final String RALLIED_TAG = "rallyguard:rallied";

    public static boolean isWaiting(Entity guard) {
        return guard != null && guard.entityTags().contains(WAITING_TAG);
    }

    public static void setWaiting(Entity guard, boolean waiting) {
        if (guard == null) return;

        if (waiting) {
            guard.addTag(WAITING_TAG);
        } else {
            guard.removeTag(WAITING_TAG);
        }
    }

    public static boolean isRallied(Entity guard) {
        return guard != null && guard.entityTags().contains(RALLIED_TAG);
    }

    public static void setRallied(Entity guard, boolean rallied) {
        if (guard == null) return;

        if (rallied) {
            guard.addTag(RALLIED_TAG);
        } else {
            guard.removeTag(RALLIED_TAG);
        }
    }

    public static int statusOf(GuardEntity guard) {
        return GuardOrderStatus.resolve(
                MoveOrders.active(guard).isPresent(),
                GuardRoutes.get(guard).active(),
                guard.isPatrolling(),
                isWaiting(guard),
                isRallied(guard) || guard.isFollowing()
        );
    }
}
