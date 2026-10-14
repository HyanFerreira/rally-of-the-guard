package net.hfstack.rallyguard.squad;

import net.hfstack.rallyguard.api.command.GuardCommandScope;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class GuardScopeMatcher {
    private GuardScopeMatcher() {
    }

    public static boolean matches(GuardCommandScope scope, Optional<UUID> effectiveSquadId) {
        Objects.requireNonNull(scope, "scope");
        Optional<UUID> assignment = effectiveSquadId == null ? Optional.empty() : effectiveSquadId;
        return switch (scope.kind()) {
            case ALL -> true;
            case UNASSIGNED -> assignment.isEmpty();
            case SQUAD -> scope.squadId().equals(assignment);
        };
    }
}
