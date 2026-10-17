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
import java.util.UUID;
import net.hfstack.rallyguard.network.codec.GuardCommandScopeCodec;
import net.hfstack.rallyguard.network.codec.MoreCodecs;
import net.hfstack.rallyguard.api.command.GuardUnitCategory;

/**
 * S2C: servidor envia lista de guardas (id, nome, patrulhando).
 */
public record GuardListS2CPayload(boolean squadsEnabled, List<SquadEntry> squads,
                                  List<NamedRouteEntry> namedRoutes, List<Entry> entries)
        implements CustomPacketPayload {
    public record Point(int x, int y, int z) {
    }

    public record SquadEntry(UUID id, String name, Optional<UUID> officerGuardId) {
        public SquadEntry {
            officerGuardId = officerGuardId == null ? Optional.empty() : officerGuardId;
        }
    }

    public record NamedRouteEntry(UUID id, String name, String dimensionId, int waitSeconds, List<Point> points) {
        public NamedRouteEntry {
            points = List.copyOf(points);
        }
    }

    public record Entry(int entityId, UUID guardId, String name, boolean patrolling, int status, boolean routeActive,
                        int routeWaitSeconds, List<Point> routePoints, Optional<Component> rank,
                        Optional<Component> settlement, Optional<UUID> squadId, Optional<UUID> namedRouteId,
                        boolean captain, int categoryId, boolean dismissible) {
        public Entry {
            routePoints = List.copyOf(routePoints);
            rank = rank == null ? Optional.empty() : rank;
            settlement = settlement == null ? Optional.empty() : settlement;
            squadId = squadId == null ? Optional.empty() : squadId;
            namedRouteId = namedRouteId == null ? Optional.empty() : namedRouteId;
        }

        public Entry(int entityId, UUID guardId, String name, boolean patrolling, int status, boolean routeActive,
                     int routeWaitSeconds, List<Point> routePoints, Optional<Component> rank,
                     Optional<Component> settlement, Optional<UUID> squadId, Optional<UUID> namedRouteId,
                     boolean captain) {
            this(entityId, guardId, name, patrolling, status, routeActive, routeWaitSeconds, routePoints,
                    rank, settlement, squadId, namedRouteId, captain, GuardUnitCategory.INFANTRY.wireId(), false);
        }

        public Entry(int entityId, UUID guardId, String name, boolean patrolling, int status, boolean routeActive,
                     int routeWaitSeconds, List<Point> routePoints, Optional<Component> rank,
                     Optional<Component> settlement, Optional<UUID> squadId, boolean captain) {
            this(entityId, guardId, name, patrolling, status, routeActive, routeWaitSeconds, routePoints,
                    rank, settlement, squadId, Optional.empty(), captain);
        }
    }

    public GuardListS2CPayload(boolean squadsEnabled, List<SquadEntry> squads, List<Entry> entries) {
        this(squadsEnabled, squads, List.of(), entries);
    }

    public GuardListS2CPayload {
        squads = List.copyOf(squads);
        namedRoutes = List.copyOf(namedRoutes);
        entries = List.copyOf(entries);
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
                    buf.writeBoolean(value.squadsEnabled());
                    buf.writeVarInt(value.squads().size());
                    for (SquadEntry squad : value.squads()) {
                        MoreCodecs.UUID_CODEC.encode(buf, squad.id());
                        buf.writeUtf(squad.name(), 48);
                        GuardCommandScopeCodec.writeOptionalUuid(buf, squad.officerGuardId());
                    }
                    buf.writeVarInt(value.namedRoutes().size());
                    for (NamedRouteEntry route : value.namedRoutes()) {
                        MoreCodecs.UUID_CODEC.encode(buf, route.id());
                        buf.writeUtf(route.name(), 48);
                        buf.writeUtf(route.dimensionId(), 128);
                        buf.writeVarInt(route.waitSeconds());
                        buf.writeVarInt(route.points().size());
                        for (Point point : route.points()) {
                            buf.writeInt(point.x());
                            buf.writeInt(point.y());
                            buf.writeInt(point.z());
                        }
                    }
                    List<Entry> list = value.entries();
                    buf.writeVarInt(list.size());
                    for (Entry e : list) {
                        buf.writeVarInt(e.entityId());
                        MoreCodecs.UUID_CODEC.encode(buf, e.guardId());
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
                        GuardCommandScopeCodec.writeOptionalUuid(buf, e.squadId());
                        GuardCommandScopeCodec.writeOptionalUuid(buf, e.namedRouteId());
                        buf.writeBoolean(e.captain());
                        buf.writeVarInt(e.categoryId());
                        buf.writeBoolean(e.dismissible());
                    }
                }

                @Override
                public GuardListS2CPayload decode(RegistryFriendlyByteBuf buf) {
                    boolean squadsEnabled = buf.readBoolean();
                    int squadCount = buf.readVarInt();
                    if (squadCount < 0 || squadCount > 64) throw new IllegalArgumentException("Invalid squad count");
                    List<SquadEntry> squads = new ArrayList<>(squadCount);
                    for (int i = 0; i < squadCount; i++) {
                        squads.add(new SquadEntry(
                                MoreCodecs.UUID_CODEC.decode(buf),
                                buf.readUtf(48),
                                GuardCommandScopeCodec.readOptionalUuid(buf)
                        ));
                    }
                    int routeCount = buf.readVarInt();
                    if (routeCount < 0 || routeCount > 64) throw new IllegalArgumentException("Invalid named route count");
                    List<NamedRouteEntry> namedRoutes = new ArrayList<>(routeCount);
                    for (int i = 0; i < routeCount; i++) {
                        UUID id = MoreCodecs.UUID_CODEC.decode(buf);
                        String name = buf.readUtf(48);
                        String dimensionId = buf.readUtf(128);
                        int waitSeconds = buf.readVarInt();
                        int pointCount = buf.readVarInt();
                        if (pointCount < 0 || pointCount > 10) throw new IllegalArgumentException("Invalid named route point count");
                        List<Point> points = new ArrayList<>(pointCount);
                        for (int p = 0; p < pointCount; p++) {
                            points.add(new Point(buf.readInt(), buf.readInt(), buf.readInt()));
                        }
                        namedRoutes.add(new NamedRouteEntry(id, name, dimensionId, waitSeconds, points));
                    }
                    int size = buf.readVarInt();
                    if (size < 0 || size > 4096) throw new IllegalArgumentException("Invalid guard count");
                    List<Entry> list = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        int id = buf.readVarInt();
                        UUID guardId = MoreCodecs.UUID_CODEC.decode(buf);
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
                        Optional<UUID> squadId = GuardCommandScopeCodec.readOptionalUuid(buf);
                        Optional<UUID> namedRouteId = GuardCommandScopeCodec.readOptionalUuid(buf);
                        boolean captain = buf.readBoolean();
                        int categoryId = buf.readVarInt();
                        boolean dismissible = buf.readBoolean();
                        list.add(new Entry(
                                id,
                                guardId,
                                name,
                                patrolling,
                                status,
                                routeActive,
                                routeWaitSeconds,
                                routePoints,
                                rank,
                                settlement,
                                squadId,
                                namedRouteId,
                                captain,
                                categoryId,
                                dismissible
                        ));
                    }
                    return new GuardListS2CPayload(squadsEnabled, squads, namedRoutes, list);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
