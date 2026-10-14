package net.hfstack.rallyguard.progression;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record CombatLedgerSnapshot(
        double totalEffectiveDamage,
        Map<UUID, Double> guardDamage,
        Optional<UUID> finalGuard
) {
    public CombatLedgerSnapshot {
        guardDamage = Map.copyOf(guardDamage);
        finalGuard = finalGuard == null ? Optional.empty() : finalGuard;
    }
}
