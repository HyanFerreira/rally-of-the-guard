package net.hfstack.rallyguard.squad;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

public final class GuardSquadRepository {
    private static final GuardSquadRepository INSTANCE = new GuardSquadRepository();

    private GuardSquadRepository() {
    }

    public static GuardSquadRepository instance() {
        return INSTANCE;
    }

    public GuardSquadAssignmentReadResult read(GuardEntity guard) {
        Objects.requireNonNull(guard, "guard");
        CompoundTag stored = guard.getAttached(SquadAttachments.ASSIGNMENT);
        return GuardSquadAssignmentCodec.decode(stored);
    }

    public GuardSquadAssignmentReadResult readAndRepair(GuardEntity guard) {
        GuardSquadAssignmentReadResult result = read(guard);
        if (result instanceof GuardSquadAssignmentReadResult.Absent) {
            GuardSquadAssignment empty = GuardSquadAssignment.unassigned();
            guard.setAttached(SquadAttachments.ASSIGNMENT, GuardSquadAssignmentCodec.encode(empty));
            return new GuardSquadAssignmentReadResult.Available(empty, false);
        }
        if (SquadPersistencePolicy.shouldCanonicalize(result)) {
            GuardSquadAssignmentReadResult.Available available =
                    (GuardSquadAssignmentReadResult.Available) result;
            guard.setAttached(SquadAttachments.ASSIGNMENT, GuardSquadAssignmentCodec.encode(available.assignment()));
            return new GuardSquadAssignmentReadResult.Available(available.assignment(), false);
        }
        return result;
    }

    public boolean save(GuardEntity guard, GuardSquadAssignment assignment) {
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(assignment, "assignment");
        if (!SquadPersistencePolicy.canWrite(read(guard))) return false;
        guard.setAttached(SquadAttachments.ASSIGNMENT, GuardSquadAssignmentCodec.encode(assignment));
        return true;
    }
}
