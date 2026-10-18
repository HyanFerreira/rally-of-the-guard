package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record GuardRouteUpdateC2SPayload(int entityId, int action, int waitSeconds, List<Point> points)
        implements CustomPacketPayload {
    public static final int MAX_POINTS = 10;

    public record Point(int x, int y, int z, String label) {
        public Point {
            label = label == null ? "" : label;
        }

        public Point(int x, int y, int z) {
            this(x, y, z, "");
        }
    }

    public static final Type<GuardRouteUpdateC2SPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "guard_route_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GuardRouteUpdateC2SPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public void encode(RegistryFriendlyByteBuf buf, GuardRouteUpdateC2SPayload value) {
                    buf.writeVarInt(value.entityId());
                    buf.writeVarInt(value.action());
                    buf.writeVarInt(value.waitSeconds());
                    buf.writeVarInt(value.points().size());
                    for (Point point : value.points()) {
                        buf.writeInt(point.x());
                        buf.writeInt(point.y());
                        buf.writeInt(point.z());
                        buf.writeUtf(point.label(), 32);
                    }
                }

                @Override
                public GuardRouteUpdateC2SPayload decode(RegistryFriendlyByteBuf buf) {
                    int entityId = buf.readVarInt();
                    int action = buf.readVarInt();
                    int waitSeconds = buf.readVarInt();
                    int size = buf.readVarInt();
                    if (size < 0 || size > MAX_POINTS) {
                        throw new IllegalArgumentException("Invalid guard route point count: " + size);
                    }
                    List<Point> points = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        points.add(new Point(buf.readInt(), buf.readInt(), buf.readInt(), buf.readUtf(32)));
                    }
                    return new GuardRouteUpdateC2SPayload(entityId, action, waitSeconds, points);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
