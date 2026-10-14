package net.hfstack.rallyguard.api.recruitment;

import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.Optional;

public record RecruitmentQuoteResult(Outcome outcome, Component feedback, RecruitmentQuote value) {
    public RecruitmentQuoteResult {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(feedback, "feedback");
        if (outcome == Outcome.AVAILABLE) Objects.requireNonNull(value, "value");
    }

    public boolean available() {
        return outcome == Outcome.AVAILABLE;
    }

    public Optional<RecruitmentQuote> quote() {
        return Optional.ofNullable(value);
    }

    public static RecruitmentQuoteResult available(RecruitmentQuote quote) {
        return new RecruitmentQuoteResult(Outcome.AVAILABLE, Component.empty(), quote);
    }

    public static RecruitmentQuoteResult unavailable(Outcome outcome, Component feedback) {
        if (outcome == Outcome.AVAILABLE) throw new IllegalArgumentException("Use available for a valid quote");
        return new RecruitmentQuoteResult(outcome, feedback, null);
    }

    public enum Outcome {
        AVAILABLE,
        INVALID_GUARD,
        ALREADY_OWNED,
        DENIED,
        INVALID_OFFER,
        POLICY_ERROR
    }
}
