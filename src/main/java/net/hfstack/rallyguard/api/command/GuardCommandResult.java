package net.hfstack.rallyguard.api.command;

import net.minecraft.network.chat.Component;

import java.util.Objects;

/**
 * Structured outcome returned by tactical commands.
 *
 * @param outcome machine-readable outcome for integrations
 * @param feedback localized feedback suitable for the commanding player
 */
public record GuardCommandResult(Outcome outcome, Component feedback) {
    public GuardCommandResult {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(feedback, "feedback");
    }

    public boolean succeeded() {
        return outcome == Outcome.SUCCESS;
    }

    public static GuardCommandResult success(String translationKey) {
        return new GuardCommandResult(Outcome.SUCCESS, Component.translatable(translationKey));
    }

    public static GuardCommandResult invalidGuard() {
        return new GuardCommandResult(
                Outcome.INVALID_GUARD,
                Component.translatable("gui.rallyguard.command.not_found")
        );
    }

    public static GuardCommandResult notOwner() {
        return new GuardCommandResult(
                Outcome.NOT_OWNER,
                Component.translatable("gui.rallyguard.command.not_owner")
        );
    }

    public static GuardCommandResult denied(Component reason) {
        return new GuardCommandResult(Outcome.DENIED, Objects.requireNonNull(reason, "reason"));
    }

    public static GuardCommandResult policyError() {
        return new GuardCommandResult(
                Outcome.POLICY_ERROR,
                Component.translatable("gui.rallyguard.command.policy_error")
        );
    }

    public enum Outcome {
        SUCCESS,
        INVALID_GUARD,
        NOT_OWNER,
        DENIED,
        POLICY_ERROR
    }
}
