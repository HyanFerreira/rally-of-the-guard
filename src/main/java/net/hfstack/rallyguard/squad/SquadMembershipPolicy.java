package net.hfstack.rallyguard.squad;

import java.util.Optional;
import java.util.UUID;

public final class SquadMembershipPolicy {
    private SquadMembershipPolicy() {
    }

    public static AssignmentDecision canAssign(
            boolean ownedByCommander,
            boolean squadExists,
            int currentMembers,
            int maximumMembers,
            boolean alreadyMember
    ) {
        if (!ownedByCommander) return AssignmentDecision.NOT_OWNER;
        if (!squadExists) return AssignmentDecision.SQUAD_NOT_FOUND;
        if (!alreadyMember && currentMembers >= maximumMembers) return AssignmentDecision.SQUAD_FULL;
        return AssignmentDecision.ALLOW;
    }

    public static OfficerDecision canBeOfficer(
            boolean ownedByCommander,
            boolean member,
            boolean captain,
            boolean available
    ) {
        if (!ownedByCommander) return OfficerDecision.NOT_OWNER;
        if (!available) return OfficerDecision.NOT_AVAILABLE;
        if (!member) return OfficerDecision.NOT_MEMBER;
        if (!captain) return OfficerDecision.NOT_CAPTAIN;
        return OfficerDecision.ALLOW;
    }

    public static SquadRegistry clearOfficerIfLeaving(
            SquadRegistry registry,
            UUID guardId,
            Optional<UUID> previousSquad,
            Optional<UUID> nextSquad
    ) {
        if (previousSquad.isEmpty() || previousSquad.equals(nextSquad)) return registry;
        UUID previous = previousSquad.get();
        SquadDefinition squad = registry.squads().stream()
                .filter(value -> value.id().equals(previous))
                .findFirst()
                .orElse(null);
        if (squad == null || !squad.officerGuardId().filter(guardId::equals).isPresent()) return registry;
        SquadRegistryEditor.EditResult result = SquadRegistryEditor.setOfficer(registry, previous, Optional.empty());
        return result instanceof SquadRegistryEditor.Success success ? success.registry() : registry;
    }

    public enum AssignmentDecision { ALLOW, NOT_OWNER, SQUAD_NOT_FOUND, SQUAD_FULL }

    public enum OfficerDecision { ALLOW, NOT_OWNER, NOT_MEMBER, NOT_CAPTAIN, NOT_AVAILABLE }
}
