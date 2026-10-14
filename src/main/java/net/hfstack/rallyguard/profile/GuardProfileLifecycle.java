package net.hfstack.rallyguard.profile;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.hfstack.rallyguard.RallyOfTheGuard;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GuardProfileLifecycle {
    private static final Set<FutureSchemaWarning> FUTURE_SCHEMA_WARNINGS = ConcurrentHashMap.newKeySet();
    private static boolean registered;

    private GuardProfileLifecycle() {
    }

    public static synchronized void register(GuardProfileRepository repository) {
        Objects.requireNonNull(repository, "repository");
        if (registered) {
            return;
        }

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof GuardEntity guard)) {
                return;
            }

            try {
                GuardProfileReadResult current = repository.read(guard);
                var spawnReason = GuardSpawnContextTracker.consume(guard.getUUID());
                boolean initialized = spawnReason.isPresent()
                        ? GuardProfileInitializer.initializeSpawn(
                                guard,
                                spawnReason.orElseThrow(),
                                repository
                        )
                        : GuardProfileInitializer.initializeOrMigrateLegacy(guard, current, repository);
                if (initialized) {
                    RallyOfTheGuard.LOGGER.debug("Initialized guard profile for {}", guard.getUUID());
                    current = repository.read(guard);
                }

                if (GuardProfilePersistencePolicy.shouldCanonicalize(current)) {
                    GuardProfileReadResult.Available available = (GuardProfileReadResult.Available) current;
                    repository.save(guard, available.profile());
                    RallyOfTheGuard.LOGGER.warn("Repaired guard profile for {}", guard.getUUID());
                } else if (current instanceof GuardProfileReadResult.FutureSchema future
                        && shouldWarnFutureSchema(guard.getUUID(), future.schemaVersion())) {
                    RallyOfTheGuard.LOGGER.warn(
                            "Guard {} uses unsupported future profile schema {}; profile left unchanged",
                            guard.getUUID(),
                            future.schemaVersion());
                }
                if (current instanceof GuardProfileReadResult.Available available) {
                    GuardAttributeApplier.apply(
                            guard,
                            available.profile(),
                            initialized
                                    ? GuardAttributeApplicationCause.INITIALIZATION
                                    : GuardAttributeApplicationCause.LOAD
                    );
                }
            } catch (Exception exception) {
                RallyOfTheGuard.LOGGER.error("Failed to initialize profile for guard {}", guard.getUUID(), exception);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            clearFutureSchemaWarnings();
            GuardSpawnContextTracker.clear();
        });
        registered = true;
    }

    static boolean shouldWarnFutureSchema(UUID guardId, int schemaVersion) {
        Objects.requireNonNull(guardId, "guardId");
        return FUTURE_SCHEMA_WARNINGS.add(new FutureSchemaWarning(guardId, schemaVersion));
    }

    static void clearFutureSchemaWarnings() {
        FUTURE_SCHEMA_WARNINGS.clear();
    }

    private record FutureSchemaWarning(UUID guardId, int schemaVersion) {
    }
}
