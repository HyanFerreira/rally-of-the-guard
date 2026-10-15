package net.hfstack.rallyguard.squad;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.squad.GuardSquadView;
import net.hfstack.rallyguard.api.squad.SquadView;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public final class GuardSquadAccess {
    public enum OfficerStatus {
        OFFICER,
        NOT_OFFICER,
        UNSUPPORTED
    }

    private GuardSquadAccess() {
    }

    public static GuardSquadView find(ServerPlayer owner, GuardEntity guard) {
        if (owner == null || guard == null || !GuardOwnership.isOwnedBy(guard, owner.getUUID())) {
            return GuardSquadView.unassigned();
        }
        SquadRegistryReadResult registryResult = SquadRegistryRepository.instance().readAndRepair(owner);
        GuardSquadAssignmentReadResult assignmentResult = GuardSquadRepository.instance().readAndRepair(guard);
        if (!(registryResult instanceof SquadRegistryReadResult.Available registry)
                || !(assignmentResult instanceof GuardSquadAssignmentReadResult.Available assignment)) {
            return GuardSquadView.unassigned();
        }
        Optional<UUID> assignedId = assignment.assignment().squadId();
        if (assignedId.isEmpty()) return GuardSquadView.unassigned();
        Optional<SquadDefinition> definition = registry.registry().squads().stream()
                .filter(squad -> squad.id().equals(assignedId.get()))
                .findFirst();
        if (definition.isEmpty()) {
            GuardSquadRepository.instance().save(guard, GuardSquadAssignment.unassigned());
            return GuardSquadView.unassigned();
        }
        SquadDefinition squad = definition.get();
        return new GuardSquadView(
                Optional.of(new SquadView(squad.id(), squad.name(), squad.officerGuardId())),
                squad.officerGuardId().filter(guard.getUUID()::equals).isPresent()
        );
    }

    public static OfficerStatus officerStatus(ServerPlayer owner, GuardEntity guard) {
        if (owner == null || guard == null || !GuardOwnership.isOwnedBy(guard, owner.getUUID())) {
            return OfficerStatus.NOT_OFFICER;
        }
        return officerStatus(
                guard.getUUID(),
                SquadRegistryRepository.instance().readAndRepair(owner),
                GuardSquadRepository.instance().readAndRepair(guard)
        );
    }

    static OfficerStatus officerStatus(
            UUID guardId,
            SquadRegistryReadResult registryResult,
            GuardSquadAssignmentReadResult assignmentResult
    ) {
        if (registryResult instanceof SquadRegistryReadResult.FutureSchema
                || assignmentResult instanceof GuardSquadAssignmentReadResult.FutureSchema) {
            return OfficerStatus.UNSUPPORTED;
        }
        if (!(registryResult instanceof SquadRegistryReadResult.Available registry)
                || !(assignmentResult instanceof GuardSquadAssignmentReadResult.Available assignment)) {
            return OfficerStatus.NOT_OFFICER;
        }
        Optional<UUID> assignedId = assignment.assignment().squadId();
        if (assignedId.isEmpty()) return OfficerStatus.NOT_OFFICER;
        return registry.registry().squads().stream()
                .filter(squad -> squad.id().equals(assignedId.get()))
                .findFirst()
                .flatMap(SquadDefinition::officerGuardId)
                .filter(guardId::equals)
                .map(ignored -> OfficerStatus.OFFICER)
                .orElse(OfficerStatus.NOT_OFFICER);
    }
}
