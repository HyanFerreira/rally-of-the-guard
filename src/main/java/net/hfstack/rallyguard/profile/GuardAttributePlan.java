package net.hfstack.rallyguard.profile;

import java.util.Objects;

public record GuardAttributePlan(
        boolean naturalVariationEnabled,
        boolean statGrowthEnabled,
        double naturalHealth,
        double naturalAttack,
        double naturalArmor,
        double naturalSpeed,
        double levelHealth,
        double levelAttack,
        double levelArmor
) {
    public static GuardAttributePlan from(
            GuardProfile profile,
            boolean progressionEnabled,
            boolean naturalVariationEnabled,
            boolean statGrowthEnabled
    ) {
        Objects.requireNonNull(profile, "profile");
        int level = GuardLevelModel.level(profile.totalXpMilli());
        return new GuardAttributePlan(
                naturalVariationEnabled,
                progressionEnabled && statGrowthEnabled,
                NaturalAttributeModel.mapSignedRoll(profile.healthRoll(), -0.08, 0.10),
                NaturalAttributeModel.mapSignedRoll(profile.attackRoll(), -0.08, 0.10),
                NaturalAttributeModel.mapSignedRoll(profile.armorRoll(), -1.0, 2.0),
                NaturalAttributeModel.mapSignedRoll(profile.speedRoll(), -0.02, 0.02),
                GuardLevelModel.healthBonus(level),
                GuardLevelModel.attackBonus(level),
                GuardLevelModel.armorBonus(level)
        );
    }
}
