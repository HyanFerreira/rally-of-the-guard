package net.hfstack.rallyguard.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardAttributePlanTest {
    @Test
    void levelTenUsesExactProgressionTable() {
        GuardProfile profile = GuardProfile.createV2(
                GuardProfileOrigin.NATURAL,
                855_000,
                5_000,
                0,
                0,
                0,
                0,
                0,
                0,
                false
        );

        GuardAttributePlan plan = GuardAttributePlan.from(profile, true, true, true);

        assertEquals(8.0, plan.levelHealth());
        assertEquals(2.0, plan.levelAttack());
        assertEquals(3.0, plan.levelArmor());
        assertTrue(plan.naturalVariationEnabled());
        assertTrue(plan.statGrowthEnabled());
    }

    @Test
    void featureFlagsRemoveOnlyTheirOwnRuntimeLayer() {
        GuardProfile profile = GuardProfile.createV2(
                GuardProfileOrigin.NATURAL,
                855_000,
                8_000,
                5_000,
                5_000,
                5_000,
                5_000,
                0,
                0,
                false
        );

        GuardAttributePlan naturalOnly = GuardAttributePlan.from(profile, false, true, true);
        GuardAttributePlan levelsOnly = GuardAttributePlan.from(profile, true, false, true);
        GuardAttributePlan noGrowth = GuardAttributePlan.from(profile, true, true, false);

        assertTrue(naturalOnly.naturalVariationEnabled());
        assertFalse(naturalOnly.statGrowthEnabled());
        assertFalse(levelsOnly.naturalVariationEnabled());
        assertTrue(levelsOnly.statGrowthEnabled());
        assertFalse(noGrowth.statGrowthEnabled());
    }

    @Test
    void naturalRollsMapToSpecifiedAttributeRanges() {
        GuardProfile profile = GuardProfile.createV2(
                GuardProfileOrigin.NATURAL,
                0,
                5_000,
                -10_000,
                10_000,
                -5_000,
                5_000,
                0,
                0,
                false
        );

        GuardAttributePlan plan = GuardAttributePlan.from(profile, true, true, true);

        assertEquals(-0.08, plan.naturalHealth());
        assertEquals(0.10, plan.naturalAttack());
        assertEquals(-0.5, plan.naturalArmor());
        assertEquals(0.01, plan.naturalSpeed());
    }
}
