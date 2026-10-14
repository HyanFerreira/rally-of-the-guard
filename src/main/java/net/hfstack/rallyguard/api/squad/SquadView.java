package net.hfstack.rallyguard.api.squad;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record SquadView(UUID id, String name, Optional<UUID> officerGuardId) {
    public SquadView {
        id = Objects.requireNonNull(id, "id");
        name = Objects.requireNonNull(name, "name");
        officerGuardId = officerGuardId == null ? Optional.empty() : officerGuardId;
    }
}
