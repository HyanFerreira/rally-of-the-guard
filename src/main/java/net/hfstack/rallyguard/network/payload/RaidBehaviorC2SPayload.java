package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RaidBehaviorC2SPayload(int behaviorId) implements CustomPacketPayload {
    public static final int QUERY = -1;
    public static final Type<RaidBehaviorC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "raid_behavior")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RaidBehaviorC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, RaidBehaviorC2SPayload value) {
            buf.writeVarInt(value.behaviorId() + 1);
        }

        @Override
        public RaidBehaviorC2SPayload decode(RegistryFriendlyByteBuf buf) {
            return new RaidBehaviorC2SPayload(buf.readVarInt() - 1);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
