package net.hfstack.rallyguard.squad;

import java.util.Objects;

public sealed interface GuardSquadAssignmentReadResult {
    record Absent() implements GuardSquadAssignmentReadResult {
    }

    record Available(GuardSquadAssignment assignment, boolean repaired)
            implements GuardSquadAssignmentReadResult {
        public Available {
            assignment = Objects.requireNonNull(assignment, "assignment");
        }
    }

    record FutureSchema(int schemaVersion) implements GuardSquadAssignmentReadResult {
    }
}
