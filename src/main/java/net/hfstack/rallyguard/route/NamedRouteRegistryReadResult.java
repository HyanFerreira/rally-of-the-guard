package net.hfstack.rallyguard.route;

import java.util.Objects;

public sealed interface NamedRouteRegistryReadResult {
    record Absent() implements NamedRouteRegistryReadResult {
    }

    record Available(NamedRouteRegistry registry, boolean repaired) implements NamedRouteRegistryReadResult {
        public Available {
            registry = Objects.requireNonNull(registry, "registry");
        }
    }

    record FutureSchema(int schemaVersion) implements NamedRouteRegistryReadResult {
    }
}
