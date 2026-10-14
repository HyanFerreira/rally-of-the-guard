package net.hfstack.rallyguard.squad;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.squad.GuardSquadView;
import net.hfstack.rallyguard.api.squad.SquadMutationResult;
import net.hfstack.rallyguard.api.squad.SquadService;
import net.hfstack.rallyguard.api.squad.SquadView;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.profile.GuardProfileReadResult;
import net.hfstack.rallyguard.profile.GuardProfileRepository;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DefaultSquadService implements SquadService {
    private static final Identifier GUARD_TYPE = Identifier.fromNamespaceAndPath("guardvillagers", "guard");
    private final SquadRegistryRepository registries = SquadRegistryRepository.instance();
    private final GuardSquadRepository assignments = GuardSquadRepository.instance();

    @Override
    public List<SquadView> list(ServerPlayer owner) {
        SquadRegistry registry = registry(owner).orElse(SquadRegistry.empty());
        return registry.squads().stream()
                .map(value -> new SquadView(value.id(), value.name(), value.officerGuardId()))
                .toList();
    }

    @Override
    public GuardSquadView find(ServerPlayer owner, GuardEntity guard) {
        return GuardSquadAccess.find(owner, guard);
    }

    @Override
    public SquadMutationResult create(ServerPlayer owner, String name) {
        if (!RallyConfig.squadsEnabled()) return rejected(SquadMutationResult.Outcome.DISABLED, "gui.rallyguard.squad.disabled");
        Optional<SquadRegistry> current = registry(owner);
        if (current.isEmpty()) return unsupported();
        SquadRegistryEditor.EditResult edit = SquadRegistryEditor.create(
                current.get(), UUID.randomUUID(), name,
                RallyConfig.maxSquadsPerPlayer(), RallyConfig.maxSquadNameLength()
        );
        return persistEdit(owner, edit, "gui.rallyguard.squad.created");
    }

    @Override
    public SquadMutationResult rename(ServerPlayer owner, UUID squadId, String name) {
        if (!RallyConfig.squadsEnabled()) return rejected(SquadMutationResult.Outcome.DISABLED, "gui.rallyguard.squad.disabled");
        Optional<SquadRegistry> current = registry(owner);
        if (current.isEmpty()) return unsupported();
        return persistEdit(owner, SquadRegistryEditor.rename(
                current.get(), squadId, name, RallyConfig.maxSquadNameLength()
        ), "gui.rallyguard.squad.renamed");
    }

    @Override
    public SquadMutationResult delete(ServerPlayer owner, UUID squadId) {
        if (!RallyConfig.squadsEnabled()) return rejected(SquadMutationResult.Outcome.DISABLED, "gui.rallyguard.squad.disabled");
        Optional<SquadRegistry> current = registry(owner);
        if (current.isEmpty()) return unsupported();
        SquadRegistryEditor.EditResult edit = SquadRegistryEditor.delete(current.get(), squadId);
        if (!(edit instanceof SquadRegistryEditor.Success success)) return editorFailure(edit);
        if (!registries.save(owner, success.registry())) return unsupported();
        for (GuardEntity guard : loadedOwnedGuards(owner)) {
            effectiveAssignment(guard).filter(squadId::equals)
                    .ifPresent(ignored -> assignments.save(guard, GuardSquadAssignment.unassigned()));
        }
        return SquadMutationResult.success("gui.rallyguard.squad.deleted", squadId);
    }

    @Override
    public SquadMutationResult assign(ServerPlayer owner, GuardEntity guard, Optional<UUID> requestedSquad) {
        if (!RallyConfig.squadsEnabled()) return rejected(SquadMutationResult.Outcome.DISABLED, "gui.rallyguard.squad.disabled");
        if (guard == null || !guard.isAlive() || guard.level() != owner.level()) {
            return rejected(SquadMutationResult.Outcome.NOT_AVAILABLE, "gui.rallyguard.squad.guard_unavailable");
        }
        if (!GuardOwnership.isOwnedBy(guard, owner.getUUID())) {
            return rejected(SquadMutationResult.Outcome.NOT_OWNER, "gui.rallyguard.command.not_owner");
        }
        Optional<SquadRegistry> current = registry(owner);
        if (current.isEmpty()) return unsupported();
        GuardSquadAssignmentReadResult assignmentState = assignments.readAndRepair(guard);
        if (!(assignmentState instanceof GuardSquadAssignmentReadResult.Available available)) return unsupported();

        Optional<UUID> previous = available.assignment().squadId();
        if (requestedSquad.isPresent()) {
            boolean exists = current.get().squads().stream().anyMatch(value -> value.id().equals(requestedSquad.get()));
            int memberCount = (int) loadedOwnedGuards(owner).stream()
                    .filter(value -> effectiveAssignment(value).filter(requestedSquad.get()::equals).isPresent())
                    .count();
            SquadMembershipPolicy.AssignmentDecision decision = SquadMembershipPolicy.canAssign(
                    true, exists, memberCount, RallyConfig.maxMembersPerSquad(), previous.equals(requestedSquad)
            );
            if (decision != SquadMembershipPolicy.AssignmentDecision.ALLOW) return assignmentFailure(decision);
        }

        SquadRegistry updatedRegistry = SquadMembershipPolicy.clearOfficerIfLeaving(
                current.get(), guard.getUUID(), previous, requestedSquad
        );
        if (!updatedRegistry.equals(current.get()) && !registries.save(owner, updatedRegistry)) return unsupported();
        GuardSquadAssignment updated = requestedSquad
                .map(GuardSquadAssignment::assignedTo)
                .orElseGet(GuardSquadAssignment::unassigned);
        if (!assignments.save(guard, updated)) return unsupported();
        return SquadMutationResult.success(
                requestedSquad.isPresent() ? "gui.rallyguard.squad.assigned" : "gui.rallyguard.squad.unassigned",
                requestedSquad.orElse(null)
        );
    }

    @Override
    public SquadMutationResult setOfficer(ServerPlayer owner, UUID squadId, Optional<GuardEntity> officer) {
        if (!RallyConfig.squadsEnabled()) return rejected(SquadMutationResult.Outcome.DISABLED, "gui.rallyguard.squad.disabled");
        Optional<SquadRegistry> current = registry(owner);
        if (current.isEmpty()) return unsupported();
        if (current.get().squads().stream().noneMatch(value -> value.id().equals(squadId))) {
            return rejected(SquadMutationResult.Outcome.NOT_FOUND, "gui.rallyguard.squad.not_found");
        }
        Optional<UUID> officerId = Optional.empty();
        if (officer.isPresent()) {
            GuardEntity guard = officer.get();
            boolean ownerMatches = GuardOwnership.isOwnedBy(guard, owner.getUUID());
            boolean member = effectiveAssignment(guard).filter(squadId::equals).isPresent();
            GuardProfileReadResult profile = GuardProfileRepository.instance().read(guard);
            boolean captain = profile instanceof GuardProfileReadResult.Available available
                    && available.profile().captain();
            SquadMembershipPolicy.OfficerDecision decision = SquadMembershipPolicy.canBeOfficer(
                    ownerMatches, member, captain, guard.isAlive() && guard.level() == owner.level()
            );
            if (decision != SquadMembershipPolicy.OfficerDecision.ALLOW) return officerFailure(decision);
            officerId = Optional.of(guard.getUUID());
        }
        SquadRegistryEditor.EditResult edit = SquadRegistryEditor.setOfficer(current.get(), squadId, officerId);
        return persistEdit(owner, edit, officer.isPresent()
                ? "gui.rallyguard.squad.officer_set"
                : "gui.rallyguard.squad.officer_cleared");
    }

    private Optional<SquadRegistry> registry(ServerPlayer owner) {
        SquadRegistryReadResult result = registries.readAndRepair(owner);
        return result instanceof SquadRegistryReadResult.Available available
                ? Optional.of(available.registry()) : Optional.empty();
    }

    private Optional<UUID> effectiveAssignment(GuardEntity guard) {
        GuardSquadAssignmentReadResult result = assignments.readAndRepair(guard);
        return result instanceof GuardSquadAssignmentReadResult.Available available
                ? available.assignment().squadId() : Optional.empty();
    }

    private List<GuardEntity> loadedOwnedGuards(ServerPlayer owner) {
        return owner.level().getEntities(
                BuiltInRegistries.ENTITY_TYPE.getValue(GUARD_TYPE),
                entity -> entity instanceof GuardEntity && GuardOwnership.isOwnedBy(entity, owner.getUUID())
        ).stream().filter(GuardEntity.class::isInstance).map(GuardEntity.class::cast).toList();
    }

    private SquadMutationResult persistEdit(ServerPlayer owner, SquadRegistryEditor.EditResult edit, String key) {
        if (!(edit instanceof SquadRegistryEditor.Success success)) return editorFailure(edit);
        if (!registries.save(owner, success.registry())) return unsupported();
        return SquadMutationResult.success(key, success.affected().id());
    }

    private static SquadMutationResult editorFailure(SquadRegistryEditor.EditResult edit) {
        SquadRegistryEditor.FailureReason reason = ((SquadRegistryEditor.Failure) edit).reason();
        return switch (reason) {
            case INVALID_NAME -> rejected(SquadMutationResult.Outcome.INVALID_NAME, "gui.rallyguard.squad.invalid_name");
            case DUPLICATE_NAME -> rejected(SquadMutationResult.Outcome.DUPLICATE_NAME, "gui.rallyguard.squad.duplicate_name");
            case LIMIT_REACHED -> rejected(SquadMutationResult.Outcome.LIMIT_REACHED, "gui.rallyguard.squad.limit");
            case NOT_FOUND -> rejected(SquadMutationResult.Outcome.NOT_FOUND, "gui.rallyguard.squad.not_found");
            case DUPLICATE_ID -> rejected(SquadMutationResult.Outcome.NOT_AVAILABLE, "gui.rallyguard.squad.unavailable");
        };
    }

    private static SquadMutationResult assignmentFailure(SquadMembershipPolicy.AssignmentDecision decision) {
        return switch (decision) {
            case NOT_OWNER -> rejected(SquadMutationResult.Outcome.NOT_OWNER, "gui.rallyguard.command.not_owner");
            case SQUAD_NOT_FOUND -> rejected(SquadMutationResult.Outcome.NOT_FOUND, "gui.rallyguard.squad.not_found");
            case SQUAD_FULL -> rejected(SquadMutationResult.Outcome.SQUAD_FULL, "gui.rallyguard.squad.full");
            case ALLOW -> throw new IllegalArgumentException("ALLOW is not a failure");
        };
    }

    private static SquadMutationResult officerFailure(SquadMembershipPolicy.OfficerDecision decision) {
        return switch (decision) {
            case NOT_OWNER -> rejected(SquadMutationResult.Outcome.NOT_OWNER, "gui.rallyguard.command.not_owner");
            case NOT_MEMBER -> rejected(SquadMutationResult.Outcome.NOT_MEMBER, "gui.rallyguard.squad.officer_not_member");
            case NOT_CAPTAIN -> rejected(SquadMutationResult.Outcome.NOT_CAPTAIN, "gui.rallyguard.squad.officer_not_captain");
            case NOT_AVAILABLE -> rejected(SquadMutationResult.Outcome.NOT_AVAILABLE, "gui.rallyguard.squad.guard_unavailable");
            case ALLOW -> throw new IllegalArgumentException("ALLOW is not a failure");
        };
    }

    private static SquadMutationResult unsupported() {
        return rejected(SquadMutationResult.Outcome.UNSUPPORTED_DATA, "gui.rallyguard.squad.unsupported_data");
    }

    private static SquadMutationResult rejected(SquadMutationResult.Outcome outcome, String key) {
        return SquadMutationResult.rejected(outcome, key);
    }
}
