package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.network.codec.GuardCommandScopeCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.UUID;

public record SquadDefinitionC2SPayload(int action, Optional<UUID> squadId, String name)
        implements CustomPacketPayload {
    public static final Type<SquadDefinitionC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "squad_definition")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SquadDefinitionC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, SquadDefinitionC2SPayload value) {
            buf.writeVarInt(value.action());
            GuardCommandScopeCodec.writeOptionalUuid(buf, value.squadId());
            buf.writeUtf(value.name(), 48);
        }

        @Override
        public SquadDefinitionC2SPayload decode(RegistryFriendlyByteBuf buf) {
            return new SquadDefinitionC2SPayload(
                    buf.readVarInt(), GuardCommandScopeCodec.readOptionalUuid(buf), buf.readUtf(48)
            );
        }
    };

    public SquadDefinitionC2SPayload {
        squadId = squadId == null ? Optional.empty() : squadId;
        name = name == null ? "" : name;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}
