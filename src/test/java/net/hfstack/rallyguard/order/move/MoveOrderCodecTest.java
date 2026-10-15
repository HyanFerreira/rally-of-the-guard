package net.hfstack.rallyguard.order.move;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveOrderCodecTest {
    @Test
    void activeOrderRoundTripsWithoutChangingItsDestination() {
        MoveOrderState state = MoveOrderState.active(new BlockPos(12, 64, -7));

        MoveOrderRead.Available result = available(MoveOrderCodec.decode(MoveOrderCodec.encode(state)));

        assertEquals(Optional.of(state), result.state());
    }

    @Test
    void absentAndMalformedDataAreInactive() {
        CompoundTag malformed = new CompoundTag();
        malformed.putInt("schema_version", MoveOrderState.SCHEMA_VERSION);
        malformed.putBoolean("active", true);
        malformed.putString("target_x", "not-a-number");

        assertTrue(available(MoveOrderCodec.decode(null)).state().isEmpty());
        assertTrue(available(MoveOrderCodec.decode(malformed)).state().isEmpty());
    }

    @Test
    void futureSchemaIsReportedWithoutMutatingStoredData() {
        CompoundTag future = new CompoundTag();
        future.putInt("schema_version", 9);
        future.putString("future_field", "keep-me");
        CompoundTag before = future.copy();

        MoveOrderRead.Unsupported result = assertInstanceOf(
                MoveOrderRead.Unsupported.class,
                MoveOrderCodec.decode(future)
        );

        assertEquals(9, result.schemaVersion());
        assertEquals(before, future);
    }

    private static MoveOrderRead.Available available(MoveOrderRead result) {
        return assertInstanceOf(MoveOrderRead.Available.class, result);
    }
}
