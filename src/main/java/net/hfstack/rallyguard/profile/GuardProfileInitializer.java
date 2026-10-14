package net.hfstack.rallyguard.profile;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.config.RallyConfig;
import net.minecraft.world.entity.EntitySpawnReason;

import java.util.Objects;

public final class GuardProfileInitializer {
    private GuardProfileInitializer() {
    }

    public static boolean initializeSpawn(
            GuardEntity guard,
            EntitySpawnReason spawnReason,
            GuardProfileRepository repository
    ) {
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(spawnReason, "spawnReason");
        Objects.requireNonNull(repository, "repository");
        if (!(repository.read(guard) instanceof GuardProfileReadResult.Absent)) {
            return false;
        }

        GuardProfileOrigin origin = origin(spawnReason);
        boolean naturalLevelEligible = isNaturalLevelEligible(spawnReason)
                && RallyConfig.advancedNaturalGuardsEnabled();
        GuardProfile profile = GuardProfileGenerator.generate(
                guard.getUUID(),
                origin,
                naturalLevelEligible,
                naturalLevelEligible ? nearbyCaptains(guard, repository) : 0
        );
        return repository.save(guard, profile);
    }

    public static boolean initializeOrMigrateLegacy(
            GuardEntity guard,
            GuardProfileReadResult current,
            GuardProfileRepository repository
    ) {
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(repository, "repository");
        if (current instanceof GuardProfileReadResult.Absent) {
            return repository.save(
                    guard,
                    GuardProfileGenerator.generate(guard.getUUID(), GuardProfileOrigin.LEGACY, false)
            );
        }
        if (current instanceof GuardProfileReadResult.Available available
                && NaturalIdentityMigration.isProfileCorePlaceholder(available.profile())) {
            return repository.save(
                    guard,
                    GuardProfileGenerator.generate(guard.getUUID(), GuardProfileOrigin.LEGACY, false)
            );
        }
        return false;
    }

    static GuardProfileOrigin origin(EntitySpawnReason spawnReason) {
        return switch (spawnReason) {
            case NATURAL, CHUNK_GENERATION, STRUCTURE, PATROL -> GuardProfileOrigin.NATURAL;
            case COMMAND -> GuardProfileOrigin.COMMAND;
            default -> GuardProfileOrigin.OTHER;
        };
    }

    static boolean isNaturalLevelEligible(EntitySpawnReason spawnReason) {
        return switch (spawnReason) {
            case NATURAL, CHUNK_GENERATION, STRUCTURE, PATROL -> true;
            default -> false;
        };
    }

    private static int nearbyCaptains(GuardEntity guard, GuardProfileRepository repository) {
        if (!RallyConfig.naturalCaptainsEnabled()) return 0;
        double radius = RallyConfig.captainRegionalRadius();
        double radiusSquared = radius * radius;
        int limit = RallyConfig.captainRegionalLimit();
        int captains = 0;
        for (GuardEntity nearby : guard.level().getEntitiesOfClass(
                GuardEntity.class,
                guard.getBoundingBox().inflate(radius),
                candidate -> candidate != guard
                        && candidate.isAlive()
                        && candidate.distanceToSqr(guard) <= radiusSquared
        )) {
            GuardProfileReadResult result = repository.read(nearby);
            if (result instanceof GuardProfileReadResult.Available available
                    && available.profile().captain()) {
                captains++;
                if (captains >= limit) return captains;
            }
        }
        return captains;
    }
}
