package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.raid.RaidOpportunityKey;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RaidResponseC2SPayload(int raidId, String dimensionId, BlockPos center, int actionId)
        implements CustomPacketPayload {
    public static final Type<RaidResponseC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "raid_response")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RaidResponseC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, RaidResponseC2SPayload value) {
            buf.writeVarInt(value.raidId());
            buf.writeUtf(value.dimensionId(), 128);
            buf.writeBlockPos(value.center());
            buf.writeVarInt(value.actionId());
        }

        @Override
        public RaidResponseC2SPayload decode(RegistryFriendlyByteBuf buf) {
            return new RaidResponseC2SPayload(
                    buf.readVarInt(), buf.readUtf(128), buf.readBlockPos(), buf.readVarInt()
            );
        }
    };

    public RaidOpportunityKey key() {
        return new RaidOpportunityKey(raidId, dimensionId, center);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
