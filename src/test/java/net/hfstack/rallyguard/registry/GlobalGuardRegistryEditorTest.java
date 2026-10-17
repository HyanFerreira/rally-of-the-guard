package net.hfstack.rallyguard.registry;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class GlobalGuardRegistryEditorTest {
    @Test
    void upsertReplacesMatchingGuardWithoutChangingOrder() {
        UUID firstId = UUID.randomUUID();
        GlobalGuardRecord first = record(firstId, "Old", 1L);
        GlobalGuardRecord second = record(UUID.randomUUID(), "Second", 1L);
        GlobalGuardRegistry registry = new GlobalGuardRegistry(1, List.of(first, second));

        GlobalGuardRegistry updated = GlobalGuardRegistryEditor.upsert(registry, record(firstId, "New", 20L));

        assertEquals(List.of("New", "Second"), updated.guards().stream().map(GlobalGuardRecord::name).toList());
        assertEquals(20L, updated.guards().getFirst().lastSeenGameTime());
    }

    @Test
    void removeIsStableWhenGuardIsUnknown() {
        GlobalGuardRegistry registry = new GlobalGuardRegistry(1, List.of(record(UUID.randomUUID(), "Known", 1L)));

        assertSame(registry, GlobalGuardRegistryEditor.remove(registry, UUID.randomUUID()));
    }

    private static GlobalGuardRecord record(UUID id, String name, long seen) {
        return new GlobalGuardRecord(
                id, name, "minecraft:overworld", BlockPos.ZERO, seen, 1,
                "recruit", false, Optional.empty(), 0
        );
    }
}
