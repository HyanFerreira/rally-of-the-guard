package net.hfstack.rallyguard.squad;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;
import java.util.UUID;

public final class GuardSquadAssignmentCodec {
    private static final String SCHEMA_VERSION = "schema_version";
    private static final String SQUAD_ID = "squad_id";

    private GuardSquadAssignmentCodec() {
    }

    public static CompoundTag encode(GuardSquadAssignment assignment) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(SCHEMA_VERSION, assignment.schemaVersion());
        assignment.squadId().ifPresent(value -> tag.putString(SQUAD_ID, value.toString()));
        return tag;
    }

    public static GuardSquadAssignmentReadResult decode(CompoundTag tag) {
        if (tag == null) return new GuardSquadAssignmentReadResult.Absent();

        boolean repaired = false;
        Tag storedVersion = tag.get(SCHEMA_VERSION);
        int schemaVersion;
        if (storedVersion instanceof NumericTag numeric) {
            long number = numeric.longValue();
            if (number > Integer.MAX_VALUE) schemaVersion = Integer.MAX_VALUE;
            else if (number < Integer.MIN_VALUE) schemaVersion = Integer.MIN_VALUE;
            else schemaVersion = (int) number;
            repaired = storedVersion.getId() != Tag.TAG_INT;
        } else {
            schemaVersion = GuardSquadAssignment.CURRENT_SCHEMA_VERSION;
            repaired = true;
        }
        if (schemaVersion > GuardSquadAssignment.CURRENT_SCHEMA_VERSION) {
            return new GuardSquadAssignmentReadResult.FutureSchema(schemaVersion);
        }
        if (schemaVersion != GuardSquadAssignment.CURRENT_SCHEMA_VERSION) repaired = true;

        Optional<UUID> squadId = Optional.empty();
        Optional<String> storedId = tag.getString(SQUAD_ID);
        if (storedId.isPresent()) {
            try {
                squadId = Optional.of(UUID.fromString(storedId.get()));
            } catch (IllegalArgumentException exception) {
                repaired = true;
            }
        } else if (tag.get(SQUAD_ID) != null) {
            repaired = true;
        }

        return new GuardSquadAssignmentReadResult.Available(
                new GuardSquadAssignment(GuardSquadAssignment.CURRENT_SCHEMA_VERSION, squadId),
                repaired
        );
    }
}
