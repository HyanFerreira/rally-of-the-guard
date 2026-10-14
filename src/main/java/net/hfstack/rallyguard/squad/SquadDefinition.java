package net.hfstack.rallyguard.squad;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record SquadDefinition(UUID id, String name, Optional<UUID> officerGuardId) {
    public SquadDefinition {
        id = Objects.requireNonNull(id, "id");
        name = Objects.requireNonNull(name, "name");
        officerGuardId = officerGuardId == null ? Optional.empty() : officerGuardId;
    }

    public SquadDefinition withName(String newName) {
        return new SquadDefinition(id, newName, officerGuardId);
    }

    public SquadDefinition withOfficer(Optional<UUID> officerId) {
        return new SquadDefinition(id, name, officerId);
    }
}
