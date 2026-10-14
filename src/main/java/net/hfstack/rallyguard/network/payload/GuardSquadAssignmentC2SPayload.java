package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.network.codec.GuardCommandScopeCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.UUID;

public record GuardSquadAssignmentC2SPayload(int entityId, Optional<UUID> squadId)
        implements CustomPacketPayload {
    public static final Type<GuardSquadAssignmentC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "guard_squad_assignment")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, GuardSquadAssignmentC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, GuardSquadAssignmentC2SPayload value) {
            buf.writeVarInt(value.entityId());
            GuardCommandScopeCodec.writeOptionalUuid(buf, value.squadId());
        }
        @Override
        public GuardSquadAssignmentC2SPayload decode(RegistryFriendlyByteBuf buf) {
            return new GuardSquadAssignmentC2SPayload(buf.readVarInt(), GuardCommandScopeCodec.readOptionalUuid(buf));
        }
    };

    public GuardSquadAssignmentC2SPayload {
        squadId = squadId == null ? Optional.empty() : squadId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}
