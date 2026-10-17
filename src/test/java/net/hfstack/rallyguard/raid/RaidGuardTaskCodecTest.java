package net.hfstack.rallyguard.raid;

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

class RaidGuardTaskCodecTest {
    @Test
    void roundTripsEverySuspendedTaskDetail() {
        RaidGuardTaskSnapshot snapshot = new RaidGuardTaskSnapshot(
                RaidGuardTaskSnapshot.SCHEMA_VERSION,
                new RaidOpportunityKey(23, "minecraft:overworld", new BlockPos(100, 64, -50)),
                false,
                true,
                Optional.of(new BlockPos(8, 70, 9)),
                false,
                false,
                new GuardRouteState(
                        true,
                        1,
                        80,
                        17,
                        List.of(new BlockPos(1, 64, 1), new BlockPos(8, 64, 8)),
                        Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000123"))
                ),
                Optional.of(MoveOrderState.active(new BlockPos(20, 65, 20))),
                Optional.of(DefendAreaState.active(new BlockPos(30, 64, 30), new BlockPos(31, 64, 31), 12))
        );

        RaidGuardTaskSnapshot decoded = RaidGuardTaskCodec.decode(
                RaidGuardTaskCodec.encode(snapshot)
        ).orElseThrow();

        assertEquals(snapshot, decoded);
    }

    @Test
    void rejectsUnknownOrMalformedSnapshots() {
        CompoundTag future = RaidGuardTaskCodec.encode(new RaidGuardTaskSnapshot(
                RaidGuardTaskSnapshot.SCHEMA_VERSION,
                new RaidOpportunityKey(1, "minecraft:overworld", BlockPos.ZERO),
                false, false, Optional.empty(), false, false,
                new GuardRouteState(false, 0, 0, 0, List.of()),
                Optional.empty(), Optional.empty()
        ));
        future.putInt("schema_version", 99);
        assertTrue(RaidGuardTaskCodec.decode(future).isEmpty());

        CompoundTag malformed = new CompoundTag();
        malformed.putInt("schema_version", RaidGuardTaskSnapshot.SCHEMA_VERSION);
        assertTrue(RaidGuardTaskCodec.decode(malformed).isEmpty());
    }
}
