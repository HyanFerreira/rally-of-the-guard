package net.hfstack.rallyguard.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class GlobalGuardRegistryEditor {
    private GlobalGuardRegistryEditor() {
    }

    public static GlobalGuardRegistry upsert(GlobalGuardRegistry registry, GlobalGuardRecord record) {
        List<GlobalGuardRecord> updated = new ArrayList<>(registry.guards());
        int existing = indexOf(updated, record.guardId());
        if (existing >= 0) {
            updated.set(existing, record);
        } else if (updated.size() < GlobalGuardRegistryCodec.MAX_ENTRIES) {
            updated.add(record);
        }
        return new GlobalGuardRegistry(GlobalGuardRegistry.SCHEMA_VERSION, updated);
    }

    public static GlobalGuardRegistry remove(GlobalGuardRegistry registry, UUID guardId) {
        List<GlobalGuardRecord> updated = registry.guards().stream()
                .filter(record -> !record.guardId().equals(guardId))
                .toList();
        return updated.size() == registry.guards().size()
                ? registry
                : new GlobalGuardRegistry(GlobalGuardRegistry.SCHEMA_VERSION, updated);
    }

    private static int indexOf(List<GlobalGuardRecord> records, UUID guardId) {
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).guardId().equals(guardId)) return i;
        }
        return -1;
    }
}
