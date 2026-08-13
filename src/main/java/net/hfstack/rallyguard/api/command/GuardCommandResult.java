package net.hfstack.rallyguard.api.command;

import net.minecraft.text.Text;

import java.util.Objects;

/**
 * Structured outcome returned by tactical commands.
 *
 * @param outcome machine-readable outcome for integrations
 * @param feedback localized feedback suitable for the commanding player
 */
public record GuardCommandResult(Outcome outcome, Text feedback) {
    public GuardCommandResult {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(feedback, "feedback");
    }

    public boolean succeeded() {
        return outcome == Outcome.SUCCESS;
    }

    public static GuardCommandResult success(String translationKey) {
        return new GuardCommandResult(Outcome.SUCCESS, Text.translatable(translationKey));
    }

    public static GuardCommandResult invalidGuard() {
        return new GuardCommandResult(
                Outcome.INVALID_GUARD,
                Text.translatable("gui.rallyguard.command.not_found")
        );
    }

    public static GuardCommandResult notOwner() {
        return new GuardCommandResult(
                Outcome.NOT_OWNER,
                Text.translatable("gui.rallyguard.command.not_owner")
        );
    }

    public enum Outcome {
        SUCCESS,
        INVALID_GUARD,
        NOT_OWNER
    }
}
