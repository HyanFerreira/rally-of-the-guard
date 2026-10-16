package net.hfstack.rallyguard.order.defend;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

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

    @Test
    void commandPostOriginRoundTripsAndLegacyStateMigratesWithoutOrigin() {
        UUID postId = UUID.randomUUID();
        DefendAreaState sourced = DefendAreaState.fromCommandPost(BlockPos.ZERO, BlockPos.ZERO, 20, postId);

        DefendAreaState decoded = available(DefendAreaCodec.decode(DefendAreaCodec.encode(sourced))).state().orElseThrow();
        assertEquals(Optional.of(postId), decoded.commandPostId());

        CompoundTag legacy = DefendAreaCodec.encode(DefendAreaState.active(BlockPos.ZERO, BlockPos.ZERO, 12));
        legacy.putInt("schema_version", 1);
        DefendAreaState migrated = available(DefendAreaCodec.decode(legacy)).state().orElseThrow();
        assertTrue(migrated.commandPostId().isEmpty());
        assertEquals(DefendAreaState.SCHEMA_VERSION, migrated.schemaVersion());
    }

    private static DefendAreaRead.Available available(DefendAreaRead result) {
        return assertInstanceOf(DefendAreaRead.Available.class, result);
    }
}
