package net.hfstack.rallyguard.registry;

import java.util.List;

public record GlobalGuardRegistry(int schemaVersion, List<GlobalGuardRecord> guards) {
    public static final int SCHEMA_VERSION = 1;

    public GlobalGuardRegistry {
        guards = List.copyOf(guards);
    }

    public static GlobalGuardRegistry empty() {
        return new GlobalGuardRegistry(SCHEMA_VERSION, List.of());
    }
}
