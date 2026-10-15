package net.hfstack.rallyguard.api.captain;

import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.Optional;

public record CaptainMutationResult(
        Outcome outcome,
        Component feedback,
        CaptainAction action,
        CaptainOffer appliedOffer
) {
    public CaptainMutationResult {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(feedback, "feedback");
        Objects.requireNonNull(action, "action");
        if (outcome == Outcome.SUCCESS && action == CaptainAction.NONE) {
            throw new IllegalArgumentException("A successful Captain mutation requires an action");
        }
        if (outcome == Outcome.SUCCESS && action == CaptainAction.PROMOTE) {
            Objects.requireNonNull(appliedOffer, "appliedOffer");
        }
        if (action != CaptainAction.PROMOTE && appliedOffer != null) {
            throw new IllegalArgumentException("Only promotion can carry an applied offer");
        }
    }

    public boolean succeeded() {
        return outcome == Outcome.SUCCESS;
    }

    public Optional<CaptainOffer> offer() {
        return Optional.ofNullable(appliedOffer);
    }

    public static CaptainMutationResult success(CaptainAction action, CaptainOffer offer, String key) {
        return new CaptainMutationResult(Outcome.SUCCESS, Component.translatable(key), action, offer);
    }

    public static CaptainMutationResult rejected(CaptainAction action, Outcome outcome, String key) {
        if (outcome == Outcome.SUCCESS) {
            throw new IllegalArgumentException("Use success for successful Captain mutations");
        }
        return new CaptainMutationResult(outcome, Component.translatable(key), action, null);
    }

    public enum Outcome {
        SUCCESS,
        DISABLED,
        NOT_AVAILABLE,
        NOT_OWNER,
        UNSUPPORTED_PROFILE,
        ALREADY_CAPTAIN,
        NOT_CAPTAIN,
        LEVEL_TOO_LOW,
        INSUFFICIENT_PAYMENT,
        ACTIVE_OFFICER,
        PERSISTENCE_ERROR,
        DENIED
    }
}
