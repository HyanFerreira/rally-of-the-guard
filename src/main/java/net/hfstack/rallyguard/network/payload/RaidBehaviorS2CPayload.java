package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RaidBehaviorS2CPayload(int behaviorId) implements CustomPacketPayload {
    public static final Type<RaidBehaviorS2CPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "raid_behavior_snapshot")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RaidBehaviorS2CPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, RaidBehaviorS2CPayload value) {
            buf.writeVarInt(value.behaviorId());
        }

        @Override
        public RaidBehaviorS2CPayload decode(RegistryFriendlyByteBuf buf) {
            return new RaidBehaviorS2CPayload(buf.readVarInt());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
