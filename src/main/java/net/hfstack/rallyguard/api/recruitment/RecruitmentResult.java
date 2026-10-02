package net.hfstack.rallyguard.api.recruitment;

import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.Optional;

public record RecruitmentResult(Outcome outcome, Component feedback, RecruitmentOffer appliedOffer) {
    public RecruitmentResult {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(feedback, "feedback");
        if (outcome == Outcome.SUCCESS) {
            Objects.requireNonNull(appliedOffer, "appliedOffer");
        }
    }

    public boolean succeeded() {
        return outcome == Outcome.SUCCESS;
    }

    public Optional<RecruitmentOffer> offer() {
        return Optional.ofNullable(appliedOffer);
    }

    public static RecruitmentResult success(RecruitmentOffer offer) {
        return new RecruitmentResult(
                Outcome.SUCCESS,
                Component.translatable("gui.rallyguard.hire.success"),
                offer
        );
    }

    public static RecruitmentResult failure(Outcome outcome, Component feedback) {
        if (outcome == Outcome.SUCCESS) {
            throw new IllegalArgumentException("Use success for successful recruitment");
        }
        return new RecruitmentResult(outcome, feedback, null);
    }

    public enum Outcome {
        SUCCESS,
        INVALID_GUARD,
        ALREADY_OWNED,
        DENIED,
        NOT_ENOUGH_PAYMENT,
        INVALID_OFFER,
        POLICY_ERROR,
        OWNERSHIP_ERROR
    }
}
