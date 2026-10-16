package net.hfstack.rallyguard.combat;

import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.squad.GuardScopeMatcher;
import net.hfstack.rallyguard.api.command.GuardCombatStance;

import java.util.Optional;
import java.util.UUID;

public final class ScopedAttackPolicy {
    private ScopedAttackPolicy() {
    }

    public static boolean mayReceiveOrder(
            GuardCommandScope scope,
            Optional<UUID> effectiveSquadId,
            boolean ownedByCommander,
            boolean combatReady,
            boolean withinRange
    ) {
        return ownedByCommander
                && combatReady
                && withinRange
                && GuardScopeMatcher.matches(scope, effectiveSquadId);
    }

    public static boolean mayReceiveOrder(
            GuardCommandScope scope,
            Optional<UUID> effectiveSquadId,
            boolean ownedByCommander,
            boolean combatReady,
            boolean withinRange,
            GuardCombatStance stance
    ) {
        return stance != GuardCombatStance.PASSIVE
                && mayReceiveOrder(scope, effectiveSquadId, ownedByCommander, combatReady, withinRange);
    }
}
