package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SquadScreenModelTest {
    private static final UUID ALPHA = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID BRAVO = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID CAPTAIN = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID RECRUIT = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID UNASSIGNED = UUID.fromString("20000000-0000-0000-0000-000000000003");

    @Test
    void filtersAllUnassignedAndNamedSquad() {
        SquadScreenModel model = new SquadScreenModel(snapshot(), GuardCommandScope.all());
        assertEquals(3, model.filteredGuards().size());
        assertEquals(List.of(UNASSIGNED), model.withScope(GuardCommandScope.unassigned())
                .filteredGuards().stream().map(GuardListS2CPayload.Entry::guardId).toList());
        assertEquals(List.of(CAPTAIN, RECRUIT), model.withScope(GuardCommandScope.squad(ALPHA))
                .filteredGuards().stream().map(GuardListS2CPayload.Entry::guardId).toList());
    }

    @Test
    void missingSelectedSquadFallsBackToAllAndOnlyMemberCaptainsAreOfficerCandidates() {
        SquadScreenModel missing = new SquadScreenModel(snapshot(), GuardCommandScope.squad(UUID.randomUUID()));
        assertEquals(GuardCommandScope.all(), missing.scope());

        SquadScreenModel model = new SquadScreenModel(snapshot(), GuardCommandScope.squad(ALPHA));
        assertEquals(List.of(CAPTAIN), model.officerCandidates(ALPHA).stream()
                .map(GuardListS2CPayload.Entry::guardId).toList());
        assertEquals(0, model.officerCandidates(BRAVO).size());
    }

    @Test
    void pageClampsAfterFiltering() {
        SquadScreenModel model = new SquadScreenModel(snapshot(), GuardCommandScope.squad(ALPHA));
        assertEquals(0, model.clampPage(9, 4));
        assertEquals(2, new SquadScreenModel(snapshot(), GuardCommandScope.all()).pageCount(2));
    }

    @Test
    void resolvesTheSquadNameShownInTheLedgerColumn() {
        SquadScreenModel model = new SquadScreenModel(snapshot(), GuardCommandScope.all());

        assertEquals(Optional.of("Alpha"), model.squadName(model.allGuards().getFirst().squadId()));
        assertEquals(Optional.empty(), model.squadName(model.allGuards().getLast().squadId()));
    }

    @Test
    void disabledSquadsForceTheAllScopeAndHideSquadData() {
        GuardListS2CPayload enabled = snapshot();
        GuardListS2CPayload disabled = new GuardListS2CPayload(false, enabled.squads(), enabled.entries());
        SquadScreenModel model = new SquadScreenModel(disabled, GuardCommandScope.squad(ALPHA));

        assertEquals(GuardCommandScope.all(), model.scope());
        assertEquals(List.of(), model.squads());
        assertEquals(3, model.filteredGuards().size());
    }

    private static GuardListS2CPayload snapshot() {
        return new GuardListS2CPayload(
                true,
                List.of(
                        new GuardListS2CPayload.SquadEntry(ALPHA, "Alpha", Optional.of(CAPTAIN)),
                        new GuardListS2CPayload.SquadEntry(BRAVO, "Bravo", Optional.empty())
                ),
                List.of(
                        guard(1, CAPTAIN, Optional.of(ALPHA), true),
                        guard(2, RECRUIT, Optional.of(ALPHA), false),
                        guard(3, UNASSIGNED, Optional.empty(), false)
                )
        );
    }

    private static GuardListS2CPayload.Entry guard(
            int entityId, UUID guardId, Optional<UUID> squadId, boolean captain
    ) {
        return new GuardListS2CPayload.Entry(
                entityId, guardId, "Guard " + entityId, false, 0, false, 30,
                List.of(), Optional.empty(), Optional.empty(), squadId, captain
        );
    }
}
