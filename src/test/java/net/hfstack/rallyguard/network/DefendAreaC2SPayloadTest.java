package net.hfstack.rallyguard.network;

import io.netty.buffer.Unpooled;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefendAreaC2SPayloadTest {
    @Test
    void payloadRoundTripsWithoutTrustingOrNormalizingClientIds() {
        DefendAreaC2SPayload payload = new DefendAreaC2SPayload(
                GuardCommandScope.unassigned(),
                91,
                new BlockPos(-20, 70, 400),
                73
        );
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        DefendAreaC2SPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, DefendAreaC2SPayload.CODEC.decode(buffer));
    }
}
