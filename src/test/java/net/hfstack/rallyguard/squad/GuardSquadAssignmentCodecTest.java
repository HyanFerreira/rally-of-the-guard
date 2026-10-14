package net.hfstack.rallyguard.squad;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardSquadAssignmentCodecTest {
    private static final UUID SQUAD_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Test
    void assignedAndUnassignedValuesRoundTrip() {
        GuardSquadAssignment assigned = new GuardSquadAssignment(1, Optional.of(SQUAD_ID));
        GuardSquadAssignment unassigned = GuardSquadAssignment.unassigned();

        GuardSquadAssignmentReadResult.Available assignedResult = available(
                GuardSquadAssignmentCodec.decode(GuardSquadAssignmentCodec.encode(assigned))
        );
        GuardSquadAssignmentReadResult.Available unassignedResult = available(
                GuardSquadAssignmentCodec.decode(GuardSquadAssignmentCodec.encode(unassigned))
        );

        assertEquals(assigned, assignedResult.assignment());
        assertEquals(unassigned, unassignedResult.assignment());
        assertFalse(assignedResult.repaired());
        assertFalse(unassignedResult.repaired());
    }

    @Test
    void invalidUuidRepairsToUnassigned() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema_version", 1);
        tag.putString("squad_id", "invalid");

        GuardSquadAssignmentReadResult.Available result = available(GuardSquadAssignmentCodec.decode(tag));

        assertEquals(GuardSquadAssignment.unassigned(), result.assignment());
        assertTrue(result.repaired());
    }

    @Test
    void futureSchemaIsNotWritableAndInputIsUnchanged() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema_version", 7);
        tag.putString("future", "keep");
        CompoundTag before = tag.copy();

        GuardSquadAssignmentReadResult.FutureSchema result = assertInstanceOf(
                GuardSquadAssignmentReadResult.FutureSchema.class,
                GuardSquadAssignmentCodec.decode(tag)
        );

        assertEquals(7, result.schemaVersion());
        assertEquals(before, tag);
        assertFalse(SquadPersistencePolicy.canWrite(result));
    }

    private static GuardSquadAssignmentReadResult.Available available(GuardSquadAssignmentReadResult result) {
        return assertInstanceOf(GuardSquadAssignmentReadResult.Available.class, result);
    }
}
