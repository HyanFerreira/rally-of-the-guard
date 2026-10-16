package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.network.codec.GuardCommandScopeCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetCombatStanceC2SPayload(
        GuardCommandScope scope,
        int stanceId
) implements CustomPacketPayload {
    public static final Type<SetCombatStanceC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "set_combat_stance")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SetCombatStanceC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, SetCombatStanceC2SPayload value) {
            GuardCommandScopeCodec.encode(buf, value.scope());
            buf.writeVarInt(value.stanceId());
        }

        @Override
        public SetCombatStanceC2SPayload decode(RegistryFriendlyByteBuf buf) {
            return new SetCombatStanceC2SPayload(GuardCommandScopeCodec.decode(buf), buf.readVarInt());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
