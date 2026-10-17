package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenGlobalGuardRegistryC2SPayload(int page) implements CustomPacketPayload {
    public static final Type<OpenGlobalGuardRegistryC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "open_global_guard_registry")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGlobalGuardRegistryC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, OpenGlobalGuardRegistryC2SPayload value) {
            buf.writeVarInt(value.page());
        }

        @Override
        public OpenGlobalGuardRegistryC2SPayload decode(RegistryFriendlyByteBuf buf) {
            return new OpenGlobalGuardRegistryC2SPayload(buf.readVarInt());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
