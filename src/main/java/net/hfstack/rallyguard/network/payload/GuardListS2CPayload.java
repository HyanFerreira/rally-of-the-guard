package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * S2C: servidor envia lista de guardas (id, nome, patrulhando).
 */
public record GuardListS2CPayload(List<Entry> entries) implements CustomPacketPayload {
    public record Point(int x, int y, int z) {
    }

    public record Entry(int entityId, String name, boolean patrolling, int status, boolean routeActive,
                        int routeWaitSeconds, List<Point> routePoints, Optional<Component> rank,
                        Optional<Component> settlement) {
        public Entry {
            routePoints = List.copyOf(routePoints);
            rank = rank == null ? Optional.empty() : rank;
            settlement = settlement == null ? Optional.empty() : settlement;
        }
    }

    public static final Type<GuardListS2CPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "guard_list"));

    /**
     * Codec explícito (sem lambdas) para evitar inferência errada de tipos.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, GuardListS2CPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public void encode(RegistryFriendlyByteBuf buf, GuardListS2CPayload value) {
                    List<Entry> list = value.entries();
                    buf.writeVarInt(list.size());
                    for (Entry e : list) {
                        buf.writeVarInt(e.entityId());
                        buf.writeUtf(e.name());
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
                        ComponentSerialization.OPTIONAL_STREAM_CODEC.encode(buf, e.rank());
                        ComponentSerialization.OPTIONAL_STREAM_CODEC.encode(buf, e.settlement());
                    }
                }

                @Override
                public GuardListS2CPayload decode(RegistryFriendlyByteBuf buf) {
                    int size = buf.readVarInt();
                    List<Entry> list = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        int id = buf.readVarInt();
                        String name = buf.readUtf();
                        boolean patrolling = buf.readBoolean();
                        int status = buf.readVarInt();
                        boolean routeActive = buf.readBoolean();
                        int routeWaitSeconds = buf.readVarInt();
                        int pointCount = buf.readVarInt();
                        List<Point> routePoints = new ArrayList<>(pointCount);
                        for (int p = 0; p < pointCount; p++) {
                            routePoints.add(new Point(buf.readInt(), buf.readInt(), buf.readInt()));
                        }
                        Optional<Component> rank = ComponentSerialization.OPTIONAL_STREAM_CODEC.decode(buf);
                        Optional<Component> settlement = ComponentSerialization.OPTIONAL_STREAM_CODEC.decode(buf);
                        list.add(new Entry(
                                id,
                                name,
                                patrolling,
                                status,
                                routeActive,
                                routeWaitSeconds,
                                routePoints,
                                rank,
                                settlement
                        ));
                    }
                    return new GuardListS2CPayload(list);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
