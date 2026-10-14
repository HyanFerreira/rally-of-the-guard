package net.hfstack.rallyguard.profile;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.random.RandomGenerator;
import net.hfstack.rallyguard.config.RallyConfig;

public final class GuardProfileGenerator {
    private static final long SEED_SALT = 0x52414C4C59475541L;

    private GuardProfileGenerator() {
    }

    public static GuardProfile generate(UUID guardId, GuardProfileOrigin origin, boolean naturalLevelEligible) {
        return generate(guardId, origin, naturalLevelEligible, 0);
    }

    public static GuardProfile generate(
            UUID guardId,
            GuardProfileOrigin origin,
            boolean naturalLevelEligible,
            int nearbyCaptains
    ) {
        Objects.requireNonNull(guardId, "guardId");
        long seed = guardId.getMostSignificantBits()
                ^ Long.rotateLeft(guardId.getLeastSignificantBits(), 29)
                ^ SEED_SALT;
        return generate(origin, naturalLevelEligible, nearbyCaptains, new Random(seed));
    }

    static GuardProfile generate(
            GuardProfileOrigin origin,
            boolean naturalLevelEligible,
            int nearbyCaptains,
            RandomGenerator random
    ) {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(random, "random");
        int quality = NaturalAttributeModel.quality(random.nextInt(10_001), random.nextInt(10_001));
        int healthRoll = nextRoll(quality, random);
        int attackRoll = nextRoll(quality, random);
        int armorRoll = nextRoll(quality, random);
        int speedRoll = nextRoll(quality, random);
        int rolledLevel = naturalLevelEligible ? GuardLevelModel.naturalLevel(random.nextInt(1_000)) : 1;
        double captainChance = NaturalCaptainPolicy.adjustedChance(
                naturalLevelEligible && RallyConfig.naturalCaptainsEnabled(),
                RallyConfig.captainSpawnChance(),
                nearbyCaptains,
                RallyConfig.captainRegionalLimit()
        );
        boolean captain = NaturalCaptainPolicy.isSelected(random.nextDouble(), captainChance);
        int level = NaturalCaptainPolicy.minimumLevel(
                captain,
                rolledLevel,
                RallyConfig.captainMinimumLevel()
        );
        return GuardProfile.createV2(
                origin,
                GuardLevelModel.cumulativeXpMilli(level),
                quality,
                healthRoll,
                attackRoll,
                armorRoll,
                speedRoll,
                0,
                0,
                captain
        );
    }

    private static int nextRoll(int quality, RandomGenerator random) {
        return NaturalAttributeModel.storedRoll(
                quality,
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0)
        );
    }
}
