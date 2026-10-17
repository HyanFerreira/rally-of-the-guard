package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.network.codec.MoreCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record GlobalGuardRegistryS2CPayload(int page, int totalPages, int totalGuards, List<Entry> entries)
        implements CustomPacketPayload {
    public record Entry(UUID guardId, String name, String dimensionId, int x, int y, int z, long lastSeen,
                        int level, String rankId, boolean captain, String squadName, int orderStatus,
                        boolean loaded, int distance) {
    }

    public static final Type<GlobalGuardRegistryS2CPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "global_guard_registry")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, GlobalGuardRegistryS2CPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, GlobalGuardRegistryS2CPayload value) {
            buf.writeVarInt(value.page());
            buf.writeVarInt(value.totalPages());
            buf.writeVarInt(value.totalGuards());
            buf.writeVarInt(value.entries().size());
            for (Entry entry : value.entries()) {
                MoreCodecs.UUID_CODEC.encode(buf, entry.guardId());
                buf.writeUtf(entry.name(), 256);
                buf.writeUtf(entry.dimensionId(), 128);
                buf.writeInt(entry.x());
                buf.writeInt(entry.y());
                buf.writeInt(entry.z());
                buf.writeLong(entry.lastSeen());
                buf.writeVarInt(entry.level());
                buf.writeUtf(entry.rankId(), 32);
                buf.writeBoolean(entry.captain());
                buf.writeUtf(entry.squadName(), 192);
                buf.writeVarInt(entry.orderStatus());
                buf.writeBoolean(entry.loaded());
                buf.writeVarInt(entry.distance() + 1);
            }
        }

        @Override
        public GlobalGuardRegistryS2CPayload decode(RegistryFriendlyByteBuf buf) {
            int page = buf.readVarInt();
            int totalPages = buf.readVarInt();
            int totalGuards = buf.readVarInt();
            int count = buf.readVarInt();
            if (count < 0 || count > 32) throw new IllegalArgumentException("Invalid global guard page size");
            List<Entry> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                entries.add(new Entry(
                        MoreCodecs.UUID_CODEC.decode(buf), buf.readUtf(256), buf.readUtf(128),
                        buf.readInt(), buf.readInt(), buf.readInt(), buf.readLong(), buf.readVarInt(),
                        buf.readUtf(32), buf.readBoolean(), buf.readUtf(192), buf.readVarInt(),
                        buf.readBoolean(), buf.readVarInt() - 1
                ));
            }
            return new GlobalGuardRegistryS2CPayload(page, totalPages, totalGuards, entries);
        }
    };

    public GlobalGuardRegistryS2CPayload {
        entries = List.copyOf(entries);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
