package net.hfstack.rallyguard.squad;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SquadMembershipPolicyTest {
    private static final UUID SQUAD_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID GUARD_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");

    @Test
    void assignmentRequiresOwnerExistingSquadAndCapacity() {
        assertEquals(SquadMembershipPolicy.AssignmentDecision.ALLOW,
                SquadMembershipPolicy.canAssign(true, true, 32, 32, true));
        assertEquals(SquadMembershipPolicy.AssignmentDecision.NOT_OWNER,
                SquadMembershipPolicy.canAssign(false, true, 0, 32, false));
        assertEquals(SquadMembershipPolicy.AssignmentDecision.SQUAD_NOT_FOUND,
                SquadMembershipPolicy.canAssign(true, false, 0, 32, false));
        assertEquals(SquadMembershipPolicy.AssignmentDecision.SQUAD_FULL,
                SquadMembershipPolicy.canAssign(true, true, 32, 32, false));
    }

    @Test
    void officerMustBeAliveOwnedCaptainAndMember() {
        assertEquals(SquadMembershipPolicy.OfficerDecision.ALLOW,
                SquadMembershipPolicy.canBeOfficer(true, true, true, true));
        assertEquals(SquadMembershipPolicy.OfficerDecision.NOT_OWNER,
                SquadMembershipPolicy.canBeOfficer(false, true, true, true));
        assertEquals(SquadMembershipPolicy.OfficerDecision.NOT_MEMBER,
                SquadMembershipPolicy.canBeOfficer(true, false, true, true));
        assertEquals(SquadMembershipPolicy.OfficerDecision.NOT_CAPTAIN,
                SquadMembershipPolicy.canBeOfficer(true, true, false, true));
        assertEquals(SquadMembershipPolicy.OfficerDecision.NOT_AVAILABLE,
                SquadMembershipPolicy.canBeOfficer(true, true, true, false));
    }

    @Test
    void movingCurrentOfficerClearsLeadershipButKeepsSquad() {
        SquadRegistry registry = new SquadRegistry(1, List.of(
                new SquadDefinition(SQUAD_ID, "Alpha", Optional.of(GUARD_ID))
        ));

        SquadRegistry updated = SquadMembershipPolicy.clearOfficerIfLeaving(
                registry, GUARD_ID, Optional.of(SQUAD_ID), Optional.empty()
        );

        assertEquals(Optional.empty(), updated.squads().getFirst().officerGuardId());
        assertEquals("Alpha", updated.squads().getFirst().name());
    }
}
