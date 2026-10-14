package net.hfstack.rallyguard.squad;

import java.util.Objects;

public sealed interface SquadRegistryReadResult {
    record Absent() implements SquadRegistryReadResult {
    }

    record Available(SquadRegistry registry, boolean repaired) implements SquadRegistryReadResult {
        public Available {
            registry = Objects.requireNonNull(registry, "registry");
        }
    }

    record FutureSchema(int schemaVersion) implements SquadRegistryReadResult {
    }
}
