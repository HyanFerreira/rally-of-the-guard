package net.hfstack.rallyguard.squad;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SquadRegistryEditor {
    private SquadRegistryEditor() {
    }

    public static EditResult create(
            SquadRegistry registry,
            UUID id,
            String rawName,
            int maximumSquads,
            int maximumNameCodePoints
    ) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(id, "id");
        Optional<String> name = SquadNamePolicy.normalize(rawName, maximumNameCodePoints);
        if (name.isEmpty()) return new Failure(FailureReason.INVALID_NAME);
        if (registry.squads().size() >= maximumSquads) return new Failure(FailureReason.LIMIT_REACHED);
        if (registry.squads().stream().anyMatch(squad -> squad.id().equals(id))) {
            return new Failure(FailureReason.DUPLICATE_ID);
        }
        if (SquadNamePolicy.isDuplicate(registry, name.get(), Optional.empty())) {
            return new Failure(FailureReason.DUPLICATE_NAME);
        }
        List<SquadDefinition> squads = new ArrayList<>(registry.squads());
        SquadDefinition created = new SquadDefinition(id, name.get(), Optional.empty());
        squads.add(created);
        return new Success(new SquadRegistry(SquadRegistry.CURRENT_SCHEMA_VERSION, squads), created);
    }

    public static EditResult rename(SquadRegistry registry, UUID id, String rawName, int maximumNameCodePoints) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(id, "id");
        int index = indexOf(registry, id);
        if (index < 0) return new Failure(FailureReason.NOT_FOUND);
        Optional<String> name = SquadNamePolicy.normalize(rawName, maximumNameCodePoints);
        if (name.isEmpty()) return new Failure(FailureReason.INVALID_NAME);
        if (SquadNamePolicy.isDuplicate(registry, name.get(), Optional.of(id))) {
            return new Failure(FailureReason.DUPLICATE_NAME);
        }
        List<SquadDefinition> squads = new ArrayList<>(registry.squads());
        SquadDefinition updated = squads.get(index).withName(name.get());
        squads.set(index, updated);
        return new Success(new SquadRegistry(SquadRegistry.CURRENT_SCHEMA_VERSION, squads), updated);
    }

    public static EditResult delete(SquadRegistry registry, UUID id) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(id, "id");
        int index = indexOf(registry, id);
        if (index < 0) return new Failure(FailureReason.NOT_FOUND);
        List<SquadDefinition> squads = new ArrayList<>(registry.squads());
        SquadDefinition removed = squads.remove(index);
        return new Success(new SquadRegistry(SquadRegistry.CURRENT_SCHEMA_VERSION, squads), removed);
    }

    public static EditResult setOfficer(SquadRegistry registry, UUID id, Optional<UUID> officerGuardId) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(id, "id");
        int index = indexOf(registry, id);
        if (index < 0) return new Failure(FailureReason.NOT_FOUND);
        List<SquadDefinition> squads = new ArrayList<>(registry.squads());
        SquadDefinition updated = squads.get(index).withOfficer(
                officerGuardId == null ? Optional.empty() : officerGuardId
        );
        squads.set(index, updated);
        return new Success(new SquadRegistry(SquadRegistry.CURRENT_SCHEMA_VERSION, squads), updated);
    }

    private static int indexOf(SquadRegistry registry, UUID id) {
        for (int i = 0; i < registry.squads().size(); i++) {
            if (registry.squads().get(i).id().equals(id)) return i;
        }
        return -1;
    }

    public sealed interface EditResult permits Success, Failure {
    }

    public record Success(SquadRegistry registry, SquadDefinition affected) implements EditResult {
    }

    public record Failure(FailureReason reason) implements EditResult {
    }

    public enum FailureReason {
        INVALID_NAME,
        DUPLICATE_NAME,
        DUPLICATE_ID,
        LIMIT_REACHED,
        NOT_FOUND
    }
}
