package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CloseMusterRollS2CPayload() implements CustomPacketPayload {
    public static final Type<CloseMusterRollS2CPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "close_muster_roll"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CloseMusterRollS2CPayload> CODEC =
            StreamCodec.of((buf, payload) -> { }, buf -> new CloseMusterRollS2CPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
