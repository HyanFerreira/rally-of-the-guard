package net.hfstack.rallyguard.squad;

import java.util.List;

public record SquadRegistry(int schemaVersion, List<SquadDefinition> squads) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public SquadRegistry {
        squads = List.copyOf(squads);
    }

    public static SquadRegistry empty() {
        return new SquadRegistry(CURRENT_SCHEMA_VERSION, List.of());
    }
}
