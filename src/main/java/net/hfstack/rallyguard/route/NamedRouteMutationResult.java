package net.hfstack.rallyguard.route;

import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record NamedRouteMutationResult(Outcome outcome, Component feedback, Optional<UUID> routeId) {
    public NamedRouteMutationResult {
        outcome = Objects.requireNonNull(outcome, "outcome");
        feedback = Objects.requireNonNull(feedback, "feedback");
        routeId = routeId == null ? Optional.empty() : routeId;
    }

    public boolean succeeded() {
        return outcome == Outcome.SUCCESS;
    }

    public static NamedRouteMutationResult success(String key, UUID routeId) {
        return new NamedRouteMutationResult(Outcome.SUCCESS, Component.translatable(key), Optional.ofNullable(routeId));
    }

    public static NamedRouteMutationResult rejected(Outcome outcome, String key) {
        return new NamedRouteMutationResult(outcome, Component.translatable(key), Optional.empty());
    }

    public enum Outcome {
        SUCCESS, INVALID_NAME, DUPLICATE_NAME, LIMIT_REACHED, NOT_FOUND, INVALID_ROUTE,
        WRONG_DIMENSION, NOT_OWNER, NOT_AVAILABLE, UNSUPPORTED_DATA
    }
}
