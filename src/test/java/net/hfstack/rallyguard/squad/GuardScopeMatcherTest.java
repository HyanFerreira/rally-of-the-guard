package net.hfstack.rallyguard.squad;

import net.hfstack.rallyguard.api.command.GuardCommandScope;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardScopeMatcherTest {
    private static final UUID ALPHA = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Test
    void allUnassignedAndSquadScopesMatchOnlyTheirIntendedAssignments() {
        assertTrue(GuardScopeMatcher.matches(GuardCommandScope.all(), Optional.of(ALPHA)));
        assertTrue(GuardScopeMatcher.matches(GuardCommandScope.all(), Optional.empty()));
        assertTrue(GuardScopeMatcher.matches(GuardCommandScope.unassigned(), Optional.empty()));
        assertFalse(GuardScopeMatcher.matches(GuardCommandScope.unassigned(), Optional.of(ALPHA)));
        assertTrue(GuardScopeMatcher.matches(GuardCommandScope.squad(ALPHA), Optional.of(ALPHA)));
        assertFalse(GuardScopeMatcher.matches(GuardCommandScope.squad(ALPHA), Optional.empty()));
    }

    @Test
    void invalidScopeShapesAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new GuardCommandScope(net.hfstack.rallyguard.api.command.GuardCommandScopeKind.ALL,
                        Optional.of(ALPHA)));
        assertThrows(IllegalArgumentException.class,
                () -> new GuardCommandScope(net.hfstack.rallyguard.api.command.GuardCommandScopeKind.SQUAD,
                        Optional.empty()));
    }
}
