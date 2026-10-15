package net.hfstack.rallyguard.network;

import io.netty.buffer.Unpooled;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveHereC2SPayloadTest {
    @Test
    void payloadRoundTripsWithoutNormalizingUnknownCategoryOrFaceIds() {
        MoveHereC2SPayload payload = new MoveHereC2SPayload(
                GuardCommandScope.unassigned(),
                91,
                new BlockPos(12, -4, 800),
                73
        );
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);

        MoveHereC2SPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, MoveHereC2SPayload.CODEC.decode(buffer));
    }
}
