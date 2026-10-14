package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.network.codec.GuardCommandScopeCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ScopedGuardActionC2SPayload(int action, GuardCommandScope scope) implements CustomPacketPayload {
    public static final Type<ScopedGuardActionC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "scoped_guard_action")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ScopedGuardActionC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, ScopedGuardActionC2SPayload value) {
            buf.writeVarInt(value.action());
            GuardCommandScopeCodec.encode(buf, value.scope());
        }
        @Override
        public ScopedGuardActionC2SPayload decode(RegistryFriendlyByteBuf buf) {
            return new ScopedGuardActionC2SPayload(buf.readVarInt(), GuardCommandScopeCodec.decode(buf));
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}
