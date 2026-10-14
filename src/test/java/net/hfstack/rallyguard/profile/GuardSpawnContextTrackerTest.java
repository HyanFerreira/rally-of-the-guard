package net.hfstack.rallyguard.profile;

import net.minecraft.world.entity.EntitySpawnReason;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardSpawnContextTrackerTest {
    @AfterEach
    void clearTracker() {
        GuardSpawnContextTracker.clear();
    }

    @Test
    void spawnReasonIsConsumedExactlyOnceWhenTheGuardEntersTheWorld() {
        UUID guardId = UUID.fromString("42f81860-3c2d-4f03-a5ab-96afecf38050");

        GuardSpawnContextTracker.record(guardId, EntitySpawnReason.STRUCTURE);

        assertEquals(EntitySpawnReason.STRUCTURE,
                GuardSpawnContextTracker.consume(guardId).orElseThrow());
        assertTrue(GuardSpawnContextTracker.consume(guardId).isEmpty());
    }

    @Test
    void unrelatedLoadedGuardHasNoPendingSpawnContext() {
        UUID spawnedGuard = UUID.fromString("99612c32-bfe9-4aa5-a9fa-0249c2ddf67d");
        UUID loadedGuard = UUID.fromString("f860135f-2fd1-49cb-adf8-e7f713503b21");

        GuardSpawnContextTracker.record(spawnedGuard, EntitySpawnReason.NATURAL);

        assertTrue(GuardSpawnContextTracker.consume(loadedGuard).isEmpty());
        assertEquals(EntitySpawnReason.NATURAL,
                GuardSpawnContextTracker.consume(spawnedGuard).orElseThrow());
    }
}
