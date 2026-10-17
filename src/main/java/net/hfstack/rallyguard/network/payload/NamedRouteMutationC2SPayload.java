package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.network.codec.GuardCommandScopeCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record NamedRouteMutationC2SPayload(int action, Optional<UUID> routeId, String name,
                                           int waitSeconds, List<Point> points,
                                           int sourceGuardEntityId) implements CustomPacketPayload {
    public static final int NO_SOURCE_GUARD = -1;

    public record Point(int x, int y, int z, String label) {
        public Point {
            label = label == null ? "" : label;
        }

        public Point(int x, int y, int z) {
            this(x, y, z, "");
        }
    }

    public static final Type<NamedRouteMutationC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "named_route_mutation")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, NamedRouteMutationC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, NamedRouteMutationC2SPayload value) {
            buf.writeVarInt(value.action());
            GuardCommandScopeCodec.writeOptionalUuid(buf, value.routeId());
            buf.writeUtf(value.name(), 48);
            buf.writeVarInt(value.waitSeconds());
            buf.writeVarInt(value.points().size());
            for (Point point : value.points()) {
                buf.writeInt(point.x());
                buf.writeInt(point.y());
                buf.writeInt(point.z());
                buf.writeUtf(point.label(), 32);
            }
            buf.writeVarInt(value.sourceGuardEntityId() + 1);
        }

        @Override
        public NamedRouteMutationC2SPayload decode(RegistryFriendlyByteBuf buf) {
            int action = buf.readVarInt();
            Optional<UUID> routeId = GuardCommandScopeCodec.readOptionalUuid(buf);
            String name = buf.readUtf(48);
            int waitSeconds = buf.readVarInt();
            int size = buf.readVarInt();
            if (size < 0 || size > 10) throw new IllegalArgumentException("Invalid named route point count");
            List<Point> points = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                points.add(new Point(buf.readInt(), buf.readInt(), buf.readInt(), buf.readUtf(32)));
            }
            int sourceGuardEntityId = buf.readVarInt() - 1;
            return new NamedRouteMutationC2SPayload(
                    action, routeId, name, waitSeconds, points, sourceGuardEntityId
            );
        }
    };

    public NamedRouteMutationC2SPayload {
        routeId = routeId == null ? Optional.empty() : routeId;
        name = name == null ? "" : name;
        points = List.copyOf(points);
        if (sourceGuardEntityId < NO_SOURCE_GUARD) sourceGuardEntityId = NO_SOURCE_GUARD;
    }

    public NamedRouteMutationC2SPayload(int action, Optional<UUID> routeId, String name,
                                       int waitSeconds, List<Point> points) {
        this(action, routeId, name, waitSeconds, points, NO_SOURCE_GUARD);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
