package net.hfstack.rallyguard.combat;

import dev.sterner.guardvillagers.common.entity.GuardEntity;

public final class GuardCombatState {
    private GuardCombatState() {
    }

    public static void disengage(GuardEntity guard) {
        guard.setTarget(null);
        guard.setAggressive(false);
        guard.stopUsingItem();
        guard.getNavigation().stop();
    }
}
