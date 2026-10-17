package net.hfstack.rallyguard.order.temporary;

import net.hfstack.rallyguard.order.GuardRouteState;
import net.hfstack.rallyguard.order.defend.DefendAreaCodec;
import net.hfstack.rallyguard.order.defend.DefendAreaRead;
import net.hfstack.rallyguard.order.defend.DefendAreaState;
import net.hfstack.rallyguard.order.move.MoveOrderState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class GuardTaskCodec {
    private static final String SCHEMA_VERSION = "schema_version";
    private static final String FOLLOWING = "following";
    private static final String PATROLLING = "patrolling";
    private static final String PATROL_POSITION = "patrol_position";
    private static final String WAITING = "waiting";
    private static final String RALLIED = "rallied";
    private static final String ROUTE = "route";
    private static final String MOVE_TARGET = "move_target";
    private static final String DEFEND_ORDER = "defend_order";

    private GuardTaskCodec() {
    }

    public static CompoundTag encode(GuardTaskSnapshot snapshot) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(SCHEMA_VERSION, snapshot.schemaVersion());
        tag.putBoolean(FOLLOWING, snapshot.following());
        tag.putBoolean(PATROLLING, snapshot.patrolling());
        snapshot.patrolPosition().ifPresent(position -> tag.put(PATROL_POSITION, encodePosition(position)));
        tag.putBoolean(WAITING, snapshot.waiting());
        tag.putBoolean(RALLIED, snapshot.rallied());
        tag.put(ROUTE, encodeRoute(snapshot.route()));
        snapshot.moveOrder().ifPresent(order -> tag.put(MOVE_TARGET, encodePosition(order.target())));
        snapshot.defendOrder().ifPresent(order -> tag.put(DEFEND_ORDER, DefendAreaCodec.encode(order)));
        return tag;
    }

    public static Optional<GuardTaskSnapshot> decode(CompoundTag tag) {
        if (tag == null || tag.getInt(SCHEMA_VERSION).orElse(-1) != GuardTaskSnapshot.SCHEMA_VERSION) {
            return Optional.empty();
        }
        Optional<GuardRouteState> route = tag.getCompound(ROUTE).flatMap(GuardTaskCodec::decodeRoute);
        if (route.isEmpty()) return Optional.empty();

        Optional<BlockPos> patrolPosition = decodeOptionalPosition(tag, PATROL_POSITION);
        Optional<MoveOrderState> moveOrder = decodeOptionalPosition(tag, MOVE_TARGET).map(MoveOrderState::active);
        Optional<DefendAreaState> defendOrder = decodeDefendOrder(tag);
        if ((tag.get(PATROL_POSITION) != null && patrolPosition.isEmpty())
                || (tag.get(MOVE_TARGET) != null && moveOrder.isEmpty())
                || (tag.get(DEFEND_ORDER) != null && defendOrder.isEmpty())) return Optional.empty();

        return Optional.of(new GuardTaskSnapshot(
                GuardTaskSnapshot.SCHEMA_VERSION,
                tag.getBooleanOr(FOLLOWING, false),
                tag.getBooleanOr(PATROLLING, false),
                patrolPosition,
                tag.getBooleanOr(WAITING, false),
                tag.getBooleanOr(RALLIED, false),
                route.get(),
                moveOrder,
                defendOrder
        ));
    }

    private static Optional<DefendAreaState> decodeDefendOrder(CompoundTag tag) {
        if (tag.get(DEFEND_ORDER) == null) return Optional.empty();
        Optional<CompoundTag> encoded = tag.getCompound(DEFEND_ORDER);
        if (encoded.isEmpty()) return Optional.empty();
        DefendAreaRead decoded = DefendAreaCodec.decode(encoded.get());
        return decoded instanceof DefendAreaRead.Available available ? available.state() : Optional.empty();
    }

    private static CompoundTag encodeRoute(GuardRouteState route) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("active", route.active());
        tag.putInt("current_index", route.currentIndex());
        tag.putInt("wait_ticks", route.waitTicks());
        tag.putInt("dwell_ticks", route.dwellTicks());
        route.namedRouteId().ifPresent(id -> tag.putString("named_route_id", id.toString()));
        tag.putString("points", route.points().stream()
                .map(point -> point.getX() + "," + point.getY() + "," + point.getZ())
                .reduce((left, right) -> left + ";" + right).orElse(""));
        return tag;
    }

    private static Optional<GuardRouteState> decodeRoute(CompoundTag tag) {
        Optional<Integer> currentIndex = tag.getInt("current_index");
        Optional<Integer> waitTicks = tag.getInt("wait_ticks");
        Optional<Integer> dwellTicks = tag.getInt("dwell_ticks");
        Optional<String> encodedPoints = tag.getString("points");
        if (currentIndex.isEmpty() || waitTicks.isEmpty() || dwellTicks.isEmpty() || encodedPoints.isEmpty()) {
            return Optional.empty();
        }
        Optional<UUID> namedRouteId = Optional.empty();
        if (tag.get("named_route_id") != null) {
            Optional<String> encodedId = tag.getString("named_route_id");
            if (encodedId.isEmpty()) return Optional.empty();
            try {
                namedRouteId = Optional.of(UUID.fromString(encodedId.get()));
            } catch (IllegalArgumentException exception) {
                return Optional.empty();
            }
        }
        try {
            return Optional.of(new GuardRouteState(
                    tag.getBooleanOr("active", false), currentIndex.get(), waitTicks.get(), dwellTicks.get(),
                    decodePoints(encodedPoints.get()), namedRouteId
            ));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static List<BlockPos> decodePoints(String encoded) {
        List<BlockPos> points = new ArrayList<>();
        if (encoded.isBlank()) return points;
        for (String point : encoded.split(";")) {
            String[] coordinates = point.split(",", 3);
            if (coordinates.length != 3) throw new IllegalArgumentException("Malformed route point");
            points.add(new BlockPos(
                    Integer.parseInt(coordinates[0]),
                    Integer.parseInt(coordinates[1]),
                    Integer.parseInt(coordinates[2])
            ));
        }
        return points;
    }

    private static CompoundTag encodePosition(BlockPos position) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", position.getX());
        tag.putInt("y", position.getY());
        tag.putInt("z", position.getZ());
        return tag;
    }

    private static Optional<BlockPos> decodeOptionalPosition(CompoundTag tag, String key) {
        if (tag.get(key) == null) return Optional.empty();
        return tag.getCompound(key).flatMap(GuardTaskCodec::decodePosition);
    }

    private static Optional<BlockPos> decodePosition(CompoundTag tag) {
        Optional<Integer> x = tag.getInt("x");
        Optional<Integer> y = tag.getInt("y");
        Optional<Integer> z = tag.getInt("z");
        if (x.isEmpty() || y.isEmpty() || z.isEmpty()) return Optional.empty();
        return Optional.of(new BlockPos(x.get(), y.get(), z.get()));
    }
}
