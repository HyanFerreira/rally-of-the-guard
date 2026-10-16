package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.api.command.GuardCombatStance;

import java.util.Optional;

public final class CombatStanceRequestPolicy {
    private CombatStanceRequestPolicy() {
    }

    public static Optional<GuardCombatStance> resolve(int stanceId, boolean validScope) {
        if (!validScope) return Optional.empty();
        return GuardCombatStance.fromWireId(stanceId);
    }
}
