package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.raid.RaidOpportunityKey;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RaidOpportunityS2CPayload(int raidId, String dimensionId, BlockPos center, int loadedGuards)
        implements CustomPacketPayload {
    public static final Type<RaidOpportunityS2CPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "raid_opportunity")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RaidOpportunityS2CPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, RaidOpportunityS2CPayload value) {
            buf.writeVarInt(value.raidId());
            buf.writeUtf(value.dimensionId(), 128);
            buf.writeBlockPos(value.center());
            buf.writeVarInt(value.loadedGuards());
        }

        @Override
        public RaidOpportunityS2CPayload decode(RegistryFriendlyByteBuf buf) {
            return new RaidOpportunityS2CPayload(
                    buf.readVarInt(), buf.readUtf(128), buf.readBlockPos(), buf.readVarInt()
            );
        }
    };

    public RaidOpportunityS2CPayload {
        if (raidId < 0) throw new IllegalArgumentException("raidId must be non-negative");
        if (dimensionId == null || dimensionId.isBlank()) throw new IllegalArgumentException("dimensionId is required");
        if (center == null) throw new IllegalArgumentException("center is required");
        if (loadedGuards <= 0) throw new IllegalArgumentException("loadedGuards must be positive");
        center = center.immutable();
    }

    public RaidOpportunityS2CPayload(RaidOpportunityKey key, int loadedGuards) {
        this(key.raidId(), key.dimensionId(), key.center(), loadedGuards);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
