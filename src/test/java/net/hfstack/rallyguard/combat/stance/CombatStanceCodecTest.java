package net.hfstack.rallyguard.combat.stance;

import net.hfstack.rallyguard.api.command.GuardCombatStance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CombatStanceCodecTest {
    @Test
    void allStancesRoundTripAndOnlyHoldPositionKeepsAnAnchor() {
        for (GuardCombatStance stance : GuardCombatStance.values()) {
            Optional<BlockPos> anchor = stance == GuardCombatStance.HOLD_POSITION
                    ? Optional.of(new BlockPos(12, 64, -7))
                    : Optional.empty();
            CombatStanceState state = new CombatStanceState(CombatStanceState.SCHEMA_VERSION, stance, anchor);

            CombatStanceRead.Available decoded = available(CombatStanceCodec.decode(CombatStanceCodec.encode(state)));

            assertEquals(state, decoded.state());
        }
    }

    @Test
    void absentAndMalformedDataFallBackToDefensive() {
        CompoundTag malformed = new CompoundTag();
        malformed.putInt("schema_version", CombatStanceState.SCHEMA_VERSION);
        malformed.putInt("stance", GuardCombatStance.HOLD_POSITION.wireId());

        assertEquals(CombatStanceState.defensive(), available(CombatStanceCodec.decode(null)).state());
        assertEquals(CombatStanceState.defensive(), available(CombatStanceCodec.decode(malformed)).state());
    }

    @Test
    void futureSchemaIsReportedWithoutMutatingStoredData() {
        CompoundTag future = new CompoundTag();
        future.putInt("schema_version", 9);
        future.putString("future_field", "keep-me");
        CompoundTag before = future.copy();

        CombatStanceRead.Unsupported result = assertInstanceOf(
                CombatStanceRead.Unsupported.class,
                CombatStanceCodec.decode(future)
        );

        assertEquals(9, result.schemaVersion());
        assertEquals(before, future);
    }

    private static CombatStanceRead.Available available(CombatStanceRead result) {
        return assertInstanceOf(CombatStanceRead.Available.class, result);
    }
}
