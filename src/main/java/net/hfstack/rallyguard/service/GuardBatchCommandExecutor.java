package net.hfstack.rallyguard.service;

import net.hfstack.rallyguard.api.command.GuardBatchCommandResult;
import net.hfstack.rallyguard.api.command.GuardCommandResult;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class GuardBatchCommandExecutor {
    private GuardBatchCommandExecutor() {
    }

    public static <T> GuardBatchCommandResult executeCandidates(
            List<T> candidates,
            Function<T, GuardCommandResult> command
    ) {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(command, "command");
        int succeeded = 0;
        int rejected = 0;
        int unavailable = 0;
        for (T candidate : candidates) {
            GuardCommandResult result = command.apply(candidate);
            if (result.succeeded()) {
                succeeded++;
            } else if (result.outcome() == GuardCommandResult.Outcome.INVALID_GUARD) {
                unavailable++;
            } else {
                rejected++;
            }
        }
        return new GuardBatchCommandResult(succeeded, rejected, unavailable);
    }
}
