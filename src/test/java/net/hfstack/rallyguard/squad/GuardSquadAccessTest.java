package net.hfstack.rallyguard.squad;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuardSquadAccessTest {
    private final UUID guardId = UUID.randomUUID();
    private final UUID squadId = UUID.randomUUID();

    @Test
    void futureRegistryMakesOfficerStatusUnsupported() {
        assertEquals(
                GuardSquadAccess.OfficerStatus.UNSUPPORTED,
                GuardSquadAccess.officerStatus(
                        guardId,
                        new SquadRegistryReadResult.FutureSchema(SquadRegistry.CURRENT_SCHEMA_VERSION + 1),
                        availableAssignment()
                )
        );
    }

    @Test
    void futureAssignmentMakesOfficerStatusUnsupported() {
        assertEquals(
                GuardSquadAccess.OfficerStatus.UNSUPPORTED,
                GuardSquadAccess.officerStatus(
                        guardId,
                        availableRegistry(Optional.of(guardId)),
                        new GuardSquadAssignmentReadResult.FutureSchema(
                                GuardSquadAssignment.CURRENT_SCHEMA_VERSION + 1
                        )
                )
        );
    }

    @Test
    void matchingOfficerIsReported() {
        assertEquals(
                GuardSquadAccess.OfficerStatus.OFFICER,
                GuardSquadAccess.officerStatus(
                        guardId,
                        availableRegistry(Optional.of(guardId)),
                        availableAssignment()
                )
        );
    }

    @Test
    void ordinaryMemberIsNotReportedAsOfficer() {
        assertEquals(
                GuardSquadAccess.OfficerStatus.NOT_OFFICER,
                GuardSquadAccess.officerStatus(
                        guardId,
                        availableRegistry(Optional.empty()),
                        availableAssignment()
                )
        );
    }

    private SquadRegistryReadResult.Available availableRegistry(Optional<UUID> officerId) {
        return new SquadRegistryReadResult.Available(
                new SquadRegistry(
                        SquadRegistry.CURRENT_SCHEMA_VERSION,
                        List.of(new SquadDefinition(squadId, "Alpha", officerId))
                ),
                false
        );
    }

    private GuardSquadAssignmentReadResult.Available availableAssignment() {
        return new GuardSquadAssignmentReadResult.Available(
                GuardSquadAssignment.assignedTo(squadId),
                false
        );
    }
}
