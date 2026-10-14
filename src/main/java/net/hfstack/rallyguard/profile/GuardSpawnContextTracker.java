package net.hfstack.rallyguard.profile;

import net.minecraft.world.entity.EntitySpawnReason;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GuardSpawnContextTracker {
    private static final Map<UUID, EntitySpawnReason> PENDING_SPAWNS = new ConcurrentHashMap<>();

    private GuardSpawnContextTracker() {
    }

    public static void record(UUID guardId, EntitySpawnReason spawnReason) {
        PENDING_SPAWNS.put(
                Objects.requireNonNull(guardId, "guardId"),
                Objects.requireNonNull(spawnReason, "spawnReason")
        );
    }

    public static Optional<EntitySpawnReason> consume(UUID guardId) {
        return Optional.ofNullable(PENDING_SPAWNS.remove(Objects.requireNonNull(guardId, "guardId")));
    }

    static void clear() {
        PENDING_SPAWNS.clear();
    }
}
