package net.hfstack.rallyguard.commandpost;

import net.hfstack.rallyguard.formation.RallyFormation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record CommandPostState(
        int schemaVersion,
        UUID postId,
        Optional<UUID> ownerId,
        String name,
        boolean active,
        int radius,
        RallyFormation formation,
        List<UUID> assignedGuardIds
) {
    public static final int SCHEMA_VERSION = 1;
    public static final int MIN_RADIUS = 4;
    public static final int MAX_RADIUS = 64;
    public static final int DEFAULT_RADIUS = 16;
    public static final int MAX_NAME_CODE_POINTS = 32;
    public static final int MAX_ASSIGNED_GUARDS = 128;

    public CommandPostState {
        if (postId == null) throw new IllegalArgumentException("postId is required");
        ownerId = ownerId == null ? Optional.empty() : ownerId;
        name = normalizeName(name);
        radius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
        formation = formation == null ? RallyFormation.SQUARE : formation;
        assignedGuardIds = assignedGuardIds == null
                ? List.of()
                : assignedGuardIds.stream().filter(id -> id != null).distinct().limit(MAX_ASSIGNED_GUARDS).toList();
    }

    public static CommandPostState create(UUID postId, UUID ownerId) {
        return new CommandPostState(
                SCHEMA_VERSION,
                postId,
                Optional.ofNullable(ownerId),
                "Posto de Comando",
                true,
                DEFAULT_RADIUS,
                RallyFormation.SQUARE,
                List.of()
        );
    }

    public CommandPostState withOwner(UUID ownerId) {
        return new CommandPostState(schemaVersion, postId, Optional.ofNullable(ownerId), name, active, radius, formation, assignedGuardIds);
    }

    public CommandPostState withSettings(String name, boolean active, int radius, RallyFormation formation) {
        return new CommandPostState(schemaVersion, postId, ownerId, name, active, radius, formation, assignedGuardIds);
    }

    public CommandPostState withAssignedGuard(UUID guardId, boolean assigned) {
        List<UUID> updated = new java.util.ArrayList<>(assignedGuardIds);
        if (assigned) {
            if (!updated.contains(guardId) && updated.size() < MAX_ASSIGNED_GUARDS) updated.add(guardId);
        } else {
            updated.remove(guardId);
        }
        return new CommandPostState(schemaVersion, postId, ownerId, name, active, radius, formation, updated);
    }

    private static String normalizeName(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) normalized = "Posto de Comando";
        if (normalized.codePointCount(0, normalized.length()) > MAX_NAME_CODE_POINTS) {
            normalized = normalized.substring(0, normalized.offsetByCodePoints(0, MAX_NAME_CODE_POINTS));
        }
        return normalized;
    }
}
