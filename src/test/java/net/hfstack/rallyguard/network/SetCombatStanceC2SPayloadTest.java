package net.hfstack.rallyguard.network;

import io.netty.buffer.Unpooled;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SetCombatStanceC2SPayloadTest {
    @Test
    void payloadRoundTripsWithoutTrustingClientStanceIds() {
        SetCombatStanceC2SPayload payload = new SetCombatStanceC2SPayload(
                GuardCommandScope.unassigned(),
                91
        );
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        SetCombatStanceC2SPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, SetCombatStanceC2SPayload.CODEC.decode(buffer));
    }
}
