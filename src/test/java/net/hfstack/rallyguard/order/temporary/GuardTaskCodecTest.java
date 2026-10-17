package net.hfstack.rallyguard.order.temporary;

import net.hfstack.rallyguard.order.GuardRouteState;
import net.hfstack.rallyguard.order.defend.DefendAreaState;
import net.hfstack.rallyguard.order.move.MoveOrderState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardTaskCodecTest {
    @Test
    void roundTripsEveryRestorableTaskField() {
        UUID routeId = UUID.fromString("fe3f5c0a-4922-45e5-8df0-12b975585751");
        GuardTaskSnapshot snapshot = new GuardTaskSnapshot(
                GuardTaskSnapshot.SCHEMA_VERSION,
                true,
                true,
                Optional.of(new BlockPos(4, 70, 8)),
                false,
                false,
                new GuardRouteState(
                        true, 1, 100, 12,
                        List.of(new BlockPos(1, 64, 1), new BlockPos(8, 64, 8)),
                        Optional.of(routeId)
                ),
                Optional.of(MoveOrderState.active(new BlockPos(10, 65, 10))),
                Optional.of(DefendAreaState.active(
                        new BlockPos(20, 64, 20), new BlockPos(21, 64, 20), 16
                ))
        );

        assertEquals(snapshot, GuardTaskCodec.decode(GuardTaskCodec.encode(snapshot)).orElseThrow());
    }

    @Test
    void rejectsUnknownSchemaWithoutGuessing() {
        CompoundTag encoded = GuardTaskCodec.encode(new GuardTaskSnapshot(
                GuardTaskSnapshot.SCHEMA_VERSION,
                false, false, Optional.empty(), false, false,
                new GuardRouteState(false, 0, 40, 0, List.of()),
                Optional.empty(), Optional.empty()
        ));
        encoded.putInt("schema_version", GuardTaskSnapshot.SCHEMA_VERSION + 1);

        assertTrue(GuardTaskCodec.decode(encoded).isEmpty());
    }
}
