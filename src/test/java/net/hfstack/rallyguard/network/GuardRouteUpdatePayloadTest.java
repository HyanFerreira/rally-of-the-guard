package net.hfstack.rallyguard.network;

import io.netty.buffer.Unpooled;
import net.hfstack.rallyguard.network.payload.GuardRouteUpdateC2SPayload;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GuardRouteUpdatePayloadTest {
    @Test
    void roundTripPreservesBoundedRoute() {
        List<GuardRouteUpdateC2SPayload.Point> points = IntStream
                .range(0, GuardRouteUpdateC2SPayload.MAX_POINTS)
                .mapToObj(index -> new GuardRouteUpdateC2SPayload.Point(index, 64, index, "Point " + index))
                .toList();
        GuardRouteUpdateC2SPayload payload = new GuardRouteUpdateC2SPayload(
                7, NetworkConstants.ROUTE_SAVE, 30, points
        );
        RegistryFriendlyByteBuf buffer = buffer();

        GuardRouteUpdateC2SPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, GuardRouteUpdateC2SPayload.CODEC.decode(buffer));
    }

    @Test
    void negativePointCountIsRejectedBeforeReadingPoints() {
        assertThrows(IllegalArgumentException.class, () -> decodeDeclaredPointCount(-1));
    }

    @Test
    void excessivePointCountIsRejectedBeforeAllocation() {
        assertThrows(IllegalArgumentException.class,
                () -> decodeDeclaredPointCount(GuardRouteUpdateC2SPayload.MAX_POINTS + 1));
        assertThrows(IllegalArgumentException.class, () -> decodeDeclaredPointCount(Integer.MAX_VALUE));
    }

    private static void decodeDeclaredPointCount(int count) {
        RegistryFriendlyByteBuf buffer = buffer();
        buffer.writeVarInt(7);
        buffer.writeVarInt(NetworkConstants.ROUTE_SAVE);
        buffer.writeVarInt(30);
        buffer.writeVarInt(count);
        GuardRouteUpdateC2SPayload.CODEC.decode(buffer);
    }

    private static RegistryFriendlyByteBuf buffer() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
    }
}
