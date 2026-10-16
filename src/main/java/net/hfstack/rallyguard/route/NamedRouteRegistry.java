package net.hfstack.rallyguard.route;

import java.util.List;

public record NamedRouteRegistry(int schemaVersion, List<NamedRouteDefinition> routes) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public NamedRouteRegistry {
        routes = List.copyOf(routes);
    }

    public static NamedRouteRegistry empty() {
        return new NamedRouteRegistry(CURRENT_SCHEMA_VERSION, List.of());
    }
}
