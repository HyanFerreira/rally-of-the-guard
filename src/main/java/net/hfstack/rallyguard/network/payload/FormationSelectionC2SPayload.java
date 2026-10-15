package net.hfstack.rallyguard.network.payload;

import net.hfstack.rallyguard.RallyOfTheGuard;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public record FormationSelectionC2SPayload(int handId, String formationId) implements CustomPacketPayload {
    public static final int MAX_FORMATION_ID_LENGTH = 32;
    public static final Type<FormationSelectionC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "formation_selection")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FormationSelectionC2SPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public void encode(RegistryFriendlyByteBuf buf, FormationSelectionC2SPayload payload) {
                    buf.writeVarInt(payload.handId());
                    buf.writeUtf(payload.formationId(), MAX_FORMATION_ID_LENGTH);
                }

                @Override
                public FormationSelectionC2SPayload decode(RegistryFriendlyByteBuf buf) {
                    return new FormationSelectionC2SPayload(
                            buf.readVarInt(),
                            buf.readUtf(MAX_FORMATION_ID_LENGTH)
                    );
                }
            };

    public FormationSelectionC2SPayload {
        Objects.requireNonNull(formationId, "formationId");
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
