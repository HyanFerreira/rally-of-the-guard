package net.hfstack.rallyguard.squad;

import java.util.Optional;
import java.util.UUID;

public record GuardSquadAssignment(int schemaVersion, Optional<UUID> squadId) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public GuardSquadAssignment {
        squadId = squadId == null ? Optional.empty() : squadId;
    }

    public static GuardSquadAssignment unassigned() {
        return new GuardSquadAssignment(CURRENT_SCHEMA_VERSION, Optional.empty());
    }

    public static GuardSquadAssignment assignedTo(UUID squadId) {
        return new GuardSquadAssignment(CURRENT_SCHEMA_VERSION, Optional.of(squadId));
    }
}
