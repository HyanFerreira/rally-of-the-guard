package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record GuardRouteUpdateC2SPayload(int entityId, int action, int waitSeconds, List<Point> points)
        implements CustomPayload {
    public record Point(int x, int y, int z) {
    }

    public static final Id<GuardRouteUpdateC2SPayload> ID =
            new Id<>(Identifier.of(RallyOfTheGuard.MOD_ID, "guard_route_update"));

    public static final PacketCodec<RegistryByteBuf, GuardRouteUpdateC2SPayload> CODEC =
            new PacketCodec<>() {
                @Override
                public void encode(RegistryByteBuf buf, GuardRouteUpdateC2SPayload value) {
                    buf.writeVarInt(value.entityId());
                    buf.writeVarInt(value.action());
                    buf.writeVarInt(value.waitSeconds());
                    buf.writeVarInt(value.points().size());
                    for (Point point : value.points()) {
                        buf.writeInt(point.x());
                        buf.writeInt(point.y());
                        buf.writeInt(point.z());
                    }
                }

                @Override
                public GuardRouteUpdateC2SPayload decode(RegistryByteBuf buf) {
                    int entityId = buf.readVarInt();
                    int action = buf.readVarInt();
                    int waitSeconds = buf.readVarInt();
                    int size = buf.readVarInt();
                    List<Point> points = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        points.add(new Point(buf.readInt(), buf.readInt(), buf.readInt()));
                    }
                    return new GuardRouteUpdateC2SPayload(entityId, action, waitSeconds, points);
                }
            };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
