package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.api.command.GuardBatchCommandResult;
import net.hfstack.rallyguard.api.command.GuardCommandResult;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public final class CombatStanceBatchPolicy {
    private CombatStanceBatchPolicy() {
    }

    public static <T> GuardBatchCommandResult execute(
            List<T> candidates,
            Function<T, UUID> id,
            Function<T, GuardCommandResult> command
    ) {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(command, "command");
        List<T> ordered = candidates.stream().sorted(Comparator.comparing(id)).toList();
        int succeeded = 0;
        int rejected = 0;
        int unavailable = 0;
        for (T candidate : ordered) {
            GuardCommandResult result = command.apply(candidate);
            if (result.succeeded()) succeeded++;
            else if (result.outcome() == GuardCommandResult.Outcome.INVALID_GUARD) unavailable++;
            else rejected++;
        }
        return new GuardBatchCommandResult(succeeded, rejected, unavailable);
    }
}
