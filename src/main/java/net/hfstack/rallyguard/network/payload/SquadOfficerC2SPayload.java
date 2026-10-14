package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.network.codec.MoreCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record SquadOfficerC2SPayload(UUID squadId, int officerEntityId) implements CustomPacketPayload {
    public static final Type<SquadOfficerC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "squad_officer")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SquadOfficerC2SPayload> CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, SquadOfficerC2SPayload value) {
            MoreCodecs.UUID_CODEC.encode(buf, value.squadId());
            buf.writeVarInt(value.officerEntityId());
        }
        @Override
        public SquadOfficerC2SPayload decode(RegistryFriendlyByteBuf buf) {
            return new SquadOfficerC2SPayload(MoreCodecs.UUID_CODEC.decode(buf), buf.readVarInt());
        }
    };

    public boolean clearsOfficer() { return officerEntityId < 0; }

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }
}
