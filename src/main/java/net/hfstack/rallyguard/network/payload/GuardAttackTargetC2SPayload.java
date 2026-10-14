package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.network.codec.GuardCommandScopeCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record GuardAttackTargetC2SPayload(int targetEntityId, int mode, GuardCommandScope scope) implements CustomPacketPayload {
    public static final Type<GuardAttackTargetC2SPayload> ID =
            new Type<>(Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "guard_attack_target"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GuardAttackTargetC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, GuardAttackTargetC2SPayload value) {
            buf.writeVarInt(value.targetEntityId());
            buf.writeVarInt(value.mode());
            GuardCommandScopeCodec.encode(buf, value.scope());
        }

        @Override
        public GuardAttackTargetC2SPayload decode(RegistryFriendlyByteBuf buf) {
            return new GuardAttackTargetC2SPayload(
                    buf.readVarInt(), buf.readVarInt(), GuardCommandScopeCodec.decode(buf)
            );
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
