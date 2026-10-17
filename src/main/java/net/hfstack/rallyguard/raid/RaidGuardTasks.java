package net.hfstack.rallyguard.raid;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.GuardRoutes;
import net.hfstack.rallyguard.order.defend.DefendAreaState;
import net.hfstack.rallyguard.order.defend.DefendAreas;
import net.hfstack.rallyguard.order.move.MoveOrders;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

final class RaidGuardTasks {
    private RaidGuardTasks() {
    }

    static boolean suspend(GuardEntity guard, RaidOpportunityKey raid) {
        if (read(guard).isPresent()) return false;
        RaidGuardTaskSnapshot snapshot = new RaidGuardTaskSnapshot(
                RaidGuardTaskSnapshot.SCHEMA_VERSION,
                raid,
                guard.isFollowing(),
                guard.isPatrolling(),
                Optional.ofNullable(guard.getPatrolPos()),
                GuardOrders.isWaiting(guard),
                GuardOrders.isRallied(guard),
                GuardRoutes.get(guard),
                MoveOrders.active(guard),
                DefendAreas.active(guard)
        );
        guard.setAttached(RaidGuardTaskAttachments.SUSPENDED_TASK, RaidGuardTaskCodec.encode(snapshot));
        return true;
    }

    static boolean belongsTo(GuardEntity guard, RaidOpportunityKey raid) {
        return read(guard).map(snapshot -> snapshot.raid().equals(raid)).orElse(false);
    }

    static void discard(GuardEntity guard, RaidOpportunityKey raid) {
        if (belongsTo(guard, raid)) guard.removeAttached(RaidGuardTaskAttachments.SUSPENDED_TASK);
    }

    static boolean restore(GuardEntity guard, RaidOpportunityKey raid) {
        RaidGuardTaskSnapshot snapshot = read(guard).filter(value -> value.raid().equals(raid)).orElse(null);
        if (snapshot == null) return false;
        guard.removeAttached(RaidGuardTaskAttachments.SUSPENDED_TASK);

        if (!DefendAreas.clearIfFromRaid(guard, raid.raidId(), raid.center())) return false;

        guard.getNavigation().stop();
        MoveOrders.clear(guard);
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

    static Optional<RaidGuardTaskSnapshot> read(GuardEntity guard) {
        CompoundTag stored = guard.getAttached(RaidGuardTaskAttachments.SUSPENDED_TASK);
        return RaidGuardTaskCodec.decode(stored);
    }
}
