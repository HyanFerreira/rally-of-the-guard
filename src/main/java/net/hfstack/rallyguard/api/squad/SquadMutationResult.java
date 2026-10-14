package net.hfstack.rallyguard.api.squad;

import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record SquadMutationResult(Outcome outcome, Component feedback, Optional<UUID> squadId) {
    public SquadMutationResult {
        outcome = Objects.requireNonNull(outcome, "outcome");
        feedback = Objects.requireNonNull(feedback, "feedback");
        squadId = squadId == null ? Optional.empty() : squadId;
    }

    public boolean succeeded() {
        return outcome == Outcome.SUCCESS;
    }

    public static SquadMutationResult success(String key, UUID squadId) {
        return new SquadMutationResult(Outcome.SUCCESS, Component.translatable(key), Optional.ofNullable(squadId));
    }

    public static SquadMutationResult rejected(Outcome outcome, String key) {
        return new SquadMutationResult(outcome, Component.translatable(key), Optional.empty());
    }

    public enum Outcome {
        SUCCESS, DISABLED, INVALID_NAME, DUPLICATE_NAME, LIMIT_REACHED, NOT_FOUND,
        NOT_OWNER, SQUAD_FULL, NOT_MEMBER, NOT_CAPTAIN, NOT_AVAILABLE, UNSUPPORTED_DATA
    }
}
