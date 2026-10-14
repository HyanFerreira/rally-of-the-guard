package net.hfstack.rallyguard.squad;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SquadRegistryEditorTest {
    private static final UUID ALPHA_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID BRAVO_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID OFFICER_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");

    @Test
    void createAppendsNormalizedSquadAndEnforcesNameAndCountLimits() {
        SquadRegistryEditor.Success created = success(SquadRegistryEditor.create(
                SquadRegistry.empty(), ALPHA_ID, " Alpha ", 1, 24
        ));
        assertEquals(List.of(new SquadDefinition(ALPHA_ID, "Alpha", Optional.empty())), created.registry().squads());

        assertEquals(SquadRegistryEditor.FailureReason.LIMIT_REACHED,
                failure(SquadRegistryEditor.create(created.registry(), BRAVO_ID, "Bravo", 1, 24)).reason());
        assertEquals(SquadRegistryEditor.FailureReason.DUPLICATE_NAME,
                failure(SquadRegistryEditor.create(created.registry(), BRAVO_ID, "alpha", 2, 24)).reason());
        assertEquals(SquadRegistryEditor.FailureReason.INVALID_NAME,
                failure(SquadRegistryEditor.create(SquadRegistry.empty(), ALPHA_ID, "", 2, 24)).reason());
    }

    @Test
    void renamePreservesOrderIdentityAndOfficer() {
        SquadRegistry registry = registryWithOfficer();

        SquadRegistryEditor.Success renamed = success(SquadRegistryEditor.rename(registry, ALPHA_ID, " First ", 24));

        assertEquals(new SquadDefinition(ALPHA_ID, "First", Optional.of(OFFICER_ID)),
                renamed.registry().squads().getFirst());
        assertEquals(BRAVO_ID, renamed.registry().squads().get(1).id());
    }

    @Test
    void deleteAndOfficerUpdatesRejectMissingSquads() {
        SquadRegistry registry = registryWithOfficer();
        assertEquals(List.of(BRAVO_ID), success(SquadRegistryEditor.delete(registry, ALPHA_ID))
                .registry().squads().stream().map(SquadDefinition::id).toList());
        assertEquals(SquadRegistryEditor.FailureReason.NOT_FOUND,
                failure(SquadRegistryEditor.delete(registry, UUID.randomUUID())).reason());

        SquadRegistryEditor.Success cleared = success(SquadRegistryEditor.setOfficer(
                registry, ALPHA_ID, Optional.empty()
        ));
        assertEquals(Optional.empty(), cleared.registry().squads().getFirst().officerGuardId());
    }

    private static SquadRegistry registryWithOfficer() {
        return new SquadRegistry(1, List.of(
                new SquadDefinition(ALPHA_ID, "Alpha", Optional.of(OFFICER_ID)),
                new SquadDefinition(BRAVO_ID, "Bravo", Optional.empty())
        ));
    }

    private static SquadRegistryEditor.Success success(SquadRegistryEditor.EditResult result) {
        return assertInstanceOf(SquadRegistryEditor.Success.class, result);
    }

    private static SquadRegistryEditor.Failure failure(SquadRegistryEditor.EditResult result) {
        return assertInstanceOf(SquadRegistryEditor.Failure.class, result);
    }
}
