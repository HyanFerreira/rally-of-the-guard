package net.hfstack.rallyguard.network;

import io.netty.buffer.Unpooled;
import net.hfstack.rallyguard.network.payload.FormationSelectionC2SPayload;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormationSelectionPayloadContractTest {
    @Test
    void selectedHandAndFormationRoundTripWithoutClientSideNormalization() {
        FormationSelectionC2SPayload payload = new FormationSelectionC2SPayload(1, "shield_wall");
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        FormationSelectionC2SPayload.CODEC.encode(buffer, payload);
        FormationSelectionC2SPayload decoded = FormationSelectionC2SPayload.CODEC.decode(buffer);

        assertEquals(1, decoded.handId());
        assertEquals("shield_wall", decoded.formationId());
    }
}
