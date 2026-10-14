package net.hfstack.rallyguard.profile;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.hfstack.rallyguard.RallyOfTheGuard;

import java.util.Objects;

public final class GuardProfileLifecycle {
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
                if (GuardProfileInitializationPolicy.shouldInitialize(true, current)) {
                    repository.getOrCreateLegacy(guard);
                    RallyOfTheGuard.LOGGER.debug("Initialized legacy profile for guard {}", guard.getUUID());
                    return;
                }

                if (current instanceof GuardProfileReadResult.Available available && available.repaired()) {
                    repository.save(guard, available.profile());
                    RallyOfTheGuard.LOGGER.warn("Repaired guard profile for {}", guard.getUUID());
                } else if (current instanceof GuardProfileReadResult.FutureSchema future) {
                    RallyOfTheGuard.LOGGER.warn(
                            "Guard {} uses unsupported future profile schema {}; profile left unchanged",
                            guard.getUUID(),
                            future.schemaVersion());
                }
            } catch (Exception exception) {
                RallyOfTheGuard.LOGGER.error("Failed to initialize profile for guard {}", guard.getUUID(), exception);
            }
        });
        registered = true;
    }
}
