package net.hfstack.rallyguard.api.command;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record GuardCommandScope(GuardCommandScopeKind kind, Optional<UUID> squadId) {
    public GuardCommandScope {
        kind = Objects.requireNonNull(kind, "kind");
        squadId = squadId == null ? Optional.empty() : squadId;
        if ((kind == GuardCommandScopeKind.SQUAD) != squadId.isPresent()) {
            throw new IllegalArgumentException("Only SQUAD scope carries a squad ID");
        }
    }

    public static GuardCommandScope all() {
        return new GuardCommandScope(GuardCommandScopeKind.ALL, Optional.empty());
    }

    public static GuardCommandScope unassigned() {
        return new GuardCommandScope(GuardCommandScopeKind.UNASSIGNED, Optional.empty());
    }

    public static GuardCommandScope squad(UUID squadId) {
        return new GuardCommandScope(GuardCommandScopeKind.SQUAD, Optional.of(squadId));
    }
}
