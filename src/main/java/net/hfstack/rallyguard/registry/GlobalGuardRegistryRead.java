package net.hfstack.rallyguard.registry;

public sealed interface GlobalGuardRegistryRead {
    record Absent() implements GlobalGuardRegistryRead {
    }

    record Available(GlobalGuardRegistry registry, boolean repaired) implements GlobalGuardRegistryRead {
    }

    record FutureSchema(int schemaVersion) implements GlobalGuardRegistryRead {
    }
}
