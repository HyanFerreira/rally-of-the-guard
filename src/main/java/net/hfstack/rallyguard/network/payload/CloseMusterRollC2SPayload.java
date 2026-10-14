package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CloseMusterRollC2SPayload() implements CustomPacketPayload {
    public static final Type<CloseMusterRollC2SPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "close_muster_roll"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CloseMusterRollC2SPayload> CODEC =
            StreamCodec.of((buf, payload) -> { }, buf -> new CloseMusterRollC2SPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
