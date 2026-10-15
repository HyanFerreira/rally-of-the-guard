package net.hfstack.rallyguard.combat;

import net.hfstack.rallyguard.api.command.GuardUnitCategory;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardCombatRoleTest {
    @Test
    void bowOrCrossbowInEitherHandClassifiesAsRanged() {
        Identifier bow = Identifier.fromNamespaceAndPath("minecraft", "bow");
        Identifier crossbow = Identifier.fromNamespaceAndPath("minecraft", "crossbow");
        Identifier sword = Identifier.fromNamespaceAndPath("minecraft", "iron_sword");

        assertTrue(GuardCombatRole.isRanged(bow, sword));
        assertTrue(GuardCombatRole.isRanged(sword, crossbow));
        assertFalse(GuardCombatRole.isRanged(sword, sword));
    }

    @Test
    void categoriesMatchTheSharedRangedClassification() {
        assertTrue(GuardCombatRole.matches(GuardUnitCategory.ALL, false));
        assertTrue(GuardCombatRole.matches(GuardUnitCategory.ALL, true));
        assertTrue(GuardCombatRole.matches(GuardUnitCategory.INFANTRY, false));
        assertFalse(GuardCombatRole.matches(GuardUnitCategory.INFANTRY, true));
        assertFalse(GuardCombatRole.matches(GuardUnitCategory.RANGED, false));
        assertTrue(GuardCombatRole.matches(GuardUnitCategory.RANGED, true));
    }
}
