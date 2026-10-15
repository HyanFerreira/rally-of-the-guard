package net.hfstack.rallyguard.order.defend;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefendAreaCodecTest {
    @Test
    void activeAreaRoundTripsWithoutChangingServerAcceptedValues() {
        DefendAreaState state = DefendAreaState.active(
                new BlockPos(12, 64, -7),
                new BlockPos(14, 64, -7),
                16
        );

        DefendAreaRead.Available result = available(DefendAreaCodec.decode(DefendAreaCodec.encode(state)));

        assertEquals(Optional.of(state), result.state());
    }

    @Test
    void absentMalformedAndInvalidRadiusDataAreInactive() {
        CompoundTag malformed = new CompoundTag();
        malformed.putInt("schema_version", DefendAreaState.SCHEMA_VERSION);
        malformed.putBoolean("active", true);
        malformed.putInt("radius", 0);

        assertTrue(available(DefendAreaCodec.decode(null)).state().isEmpty());
        assertTrue(available(DefendAreaCodec.decode(malformed)).state().isEmpty());
    }

    @Test
    void futureSchemaIsReportedWithoutMutatingStoredData() {
        CompoundTag future = new CompoundTag();
        future.putInt("schema_version", 4);
        future.putString("future_field", "keep-me");
        CompoundTag before = future.copy();

        DefendAreaRead.Unsupported result = assertInstanceOf(
                DefendAreaRead.Unsupported.class,
                DefendAreaCodec.decode(future)
        );

        assertEquals(4, result.schemaVersion());
        assertEquals(before, future);
    }

    private static DefendAreaRead.Available available(DefendAreaRead result) {
        return assertInstanceOf(DefendAreaRead.Available.class, result);
    }
}
