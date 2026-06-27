package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * S2C: servidor envia lista de guardas (id, nome, patrulhando).
 */
public record GuardListS2CPayload(List<Entry> entries) implements CustomPayload {
    public record Point(int x, int y, int z) {
    }

    public record Entry(int entityId, String name, boolean patrolling, int status, boolean routeActive,
                        int routeWaitSeconds, List<Point> routePoints) {
    }

    public static final Id<GuardListS2CPayload> ID =
            new Id<>(Identifier.of(RallyOfTheGuard.MOD_ID, "guard_list"));

    /**
     * Codec explícito (sem lambdas) para evitar inferência errada de tipos.
     */
    public static final PacketCodec<RegistryByteBuf, GuardListS2CPayload> CODEC =
            new PacketCodec<>() {
                @Override
                public void encode(RegistryByteBuf buf, GuardListS2CPayload value) {
                    List<Entry> list = value.entries();
                    buf.writeVarInt(list.size());
                    for (Entry e : list) {
                        buf.writeVarInt(e.entityId());
                        buf.writeString(e.name());
                        buf.writeBoolean(e.patrolling());
                        buf.writeVarInt(e.status());
                        buf.writeBoolean(e.routeActive());
                        buf.writeVarInt(e.routeWaitSeconds());
                        buf.writeVarInt(e.routePoints().size());
                        for (Point point : e.routePoints()) {
                            buf.writeInt(point.x());
                            buf.writeInt(point.y());
                            buf.writeInt(point.z());
                        }
                    }
                }

                @Override
                public GuardListS2CPayload decode(RegistryByteBuf buf) {
                    int size = buf.readVarInt();
                    List<Entry> list = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        int id = buf.readVarInt();
                        String name = buf.readString();
                        boolean patrolling = buf.readBoolean();
                        int status = buf.readVarInt();
                        boolean routeActive = buf.readBoolean();
                        int routeWaitSeconds = buf.readVarInt();
                        int pointCount = buf.readVarInt();
                        List<Point> routePoints = new ArrayList<>(pointCount);
                        for (int p = 0; p < pointCount; p++) {
                            routePoints.add(new Point(buf.readInt(), buf.readInt(), buf.readInt()));
                        }
                        list.add(new Entry(id, name, patrolling, status, routeActive, routeWaitSeconds, routePoints));
                    }
                    return new GuardListS2CPayload(list);
                }
            };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
