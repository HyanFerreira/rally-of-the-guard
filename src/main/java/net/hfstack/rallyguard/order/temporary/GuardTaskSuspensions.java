package net.hfstack.rallyguard.order.temporary;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.GuardRoutes;
import net.hfstack.rallyguard.order.defend.DefendAreas;
import net.hfstack.rallyguard.order.move.MoveOrders;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public final class GuardTaskSuspensions {
    private GuardTaskSuspensions() {
    }

    public static boolean suspendIfAbsent(GuardEntity guard) {
        CompoundTag stored = guard.getAttached(GuardTaskAttachments.SUSPENDED_TASK);
        if (stored != null) return false;
        GuardTaskSnapshot snapshot = new GuardTaskSnapshot(
                GuardTaskSnapshot.SCHEMA_VERSION,
                guard.isFollowing(),
                guard.isPatrolling(),
                Optional.ofNullable(guard.getPatrolPos()),
                GuardOrders.isWaiting(guard),
                GuardOrders.isRallied(guard),
                GuardRoutes.get(guard),
                MoveOrders.active(guard),
                DefendAreas.active(guard)
        );
        guard.setAttached(GuardTaskAttachments.SUSPENDED_TASK, GuardTaskCodec.encode(snapshot));
        return true;
    }

    public static boolean hasSuspendedTask(GuardEntity guard) {
        return read(guard).isPresent();
    }

    public static Optional<GuardTaskSnapshot> read(GuardEntity guard) {
        return GuardTaskCodec.decode(guard.getAttached(GuardTaskAttachments.SUSPENDED_TASK));
    }

    public static void discard(GuardEntity guard) {
        guard.removeAttached(GuardTaskAttachments.SUSPENDED_TASK);
    }

    public static boolean restore(GuardEntity guard) {
        GuardTaskSnapshot snapshot = read(guard).orElse(null);
        if (snapshot == null) return false;
        discard(guard);

        guard.setTarget(null);
        guard.setAggressive(false);
        guard.getNavigation().stop();
        MoveOrders.clear(guard);
        DefendAreas.clear(guard);
        GuardRoutes.set(guard, snapshot.route());
        snapshot.moveOrder().ifPresent(order -> MoveOrders.set(guard, order.target()));
        snapshot.defendOrder().ifPresent(order -> DefendAreas.restore(guard, order));
        GuardOrders.setWaiting(guard, snapshot.waiting());
        GuardOrders.setRallied(guard, snapshot.rallied());
        guard.setFollowing(snapshot.following());
        guard.setPatrolPos(snapshot.patrolPosition().orElse(null));
        guard.setPatrolling(snapshot.patrolling());
        return true;
    }
}
