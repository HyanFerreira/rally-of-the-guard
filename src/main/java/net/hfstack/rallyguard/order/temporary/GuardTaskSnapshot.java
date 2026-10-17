package net.hfstack.rallyguard.order.temporary;

import net.hfstack.rallyguard.order.GuardRouteState;
import net.hfstack.rallyguard.order.defend.DefendAreaState;
import net.hfstack.rallyguard.order.move.MoveOrderState;
import net.minecraft.core.BlockPos;

import java.util.Objects;
import java.util.Optional;

public record GuardTaskSnapshot(
        int schemaVersion,
        boolean following,
        boolean patrolling,
        Optional<BlockPos> patrolPosition,
        boolean waiting,
        boolean rallied,
        GuardRouteState route,
        Optional<MoveOrderState> moveOrder,
        Optional<DefendAreaState> defendOrder
) {
    public static final int SCHEMA_VERSION = 1;

    public GuardTaskSnapshot {
        patrolPosition = patrolPosition == null ? Optional.empty() : patrolPosition.map(BlockPos::immutable);
        route = Objects.requireNonNull(route, "route");
        moveOrder = moveOrder == null ? Optional.empty() : moveOrder;
        defendOrder = defendOrder == null ? Optional.empty() : defendOrder;
    }
}
