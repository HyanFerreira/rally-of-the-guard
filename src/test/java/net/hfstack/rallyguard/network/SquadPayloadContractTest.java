package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.GuardSquadAssignmentC2SPayload;
import net.hfstack.rallyguard.network.payload.ScopedGuardActionC2SPayload;
import net.hfstack.rallyguard.network.payload.SquadDefinitionC2SPayload;
import net.hfstack.rallyguard.network.payload.SquadOfficerC2SPayload;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SquadPayloadContractTest {
    private static final UUID SQUAD_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID GUARD_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");

    @Test
    void ledgerSnapshotCarriesSquadsGuardIdentityAssignmentAndCaptain() {
        GuardListS2CPayload.SquadEntry squad = new GuardListS2CPayload.SquadEntry(
                SQUAD_ID, "Alpha", Optional.of(GUARD_ID)
        );
        GuardListS2CPayload.Entry guard = new GuardListS2CPayload.Entry(
                7, GUARD_ID, "Aldric", false, 0, false, 30, List.of(),
                Optional.empty(), Optional.empty(), Optional.of(SQUAD_ID), true
        );
        GuardListS2CPayload payload = new GuardListS2CPayload(true, List.of(squad), List.of(guard));

        assertTrue(payload.squadsEnabled());
        assertEquals(SQUAD_ID, payload.squads().getFirst().id());
        assertEquals(GUARD_ID, payload.entries().getFirst().guardId());
        assertEquals(Optional.of(SQUAD_ID), payload.entries().getFirst().squadId());
        assertTrue(payload.entries().getFirst().captain());
    }

    @Test
    void mutationAndScopedActionPayloadsRetainExplicitIntent() {
        SquadDefinitionC2SPayload create = new SquadDefinitionC2SPayload(
                NetworkConstants.SQUAD_CREATE, Optional.empty(), "Alpha"
        );
        GuardSquadAssignmentC2SPayload assignment = new GuardSquadAssignmentC2SPayload(7, Optional.of(SQUAD_ID));
        SquadOfficerC2SPayload officer = new SquadOfficerC2SPayload(SQUAD_ID, 7);
        ScopedGuardActionC2SPayload action = new ScopedGuardActionC2SPayload(
                NetworkConstants.ACTION_FOLLOW, GuardCommandScope.squad(SQUAD_ID)
        );

        assertEquals("Alpha", create.name());
        assertEquals(Optional.of(SQUAD_ID), assignment.squadId());
        assertFalse(officer.clearsOfficer());
        assertEquals(GuardCommandScope.squad(SQUAD_ID), action.scope());
    }
}
