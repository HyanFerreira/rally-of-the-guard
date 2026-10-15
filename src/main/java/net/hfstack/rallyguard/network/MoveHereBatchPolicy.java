package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.api.command.GuardBatchCommandResult;
import net.hfstack.rallyguard.api.command.GuardCommandResult;
import net.minecraft.core.BlockPos;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public final class MoveHereBatchPolicy {
    private MoveHereBatchPolicy() {
    }

    public static <T> GuardBatchCommandResult execute(
            List<T> candidates,
            Function<T, UUID> id,
            Predicate<T> categoryMatches,
            DestinationResolver<T> destinations,
            BiFunction<T, BlockPos, GuardCommandResult> command
    ) {
        Objects.requireNonNull(candidates, "candidates");
        List<T> ordered = candidates.stream()
                .filter(categoryMatches)
                .sorted(Comparator.comparing(id))
                .toList();
        int succeeded = 0;
        int rejected = 0;
        int unavailable = 0;
        for (int slotIndex = 0; slotIndex < ordered.size(); slotIndex++) {
            T candidate = ordered.get(slotIndex);
            Optional<BlockPos> destination = destinations.resolve(candidate, slotIndex);
            if (destination.isEmpty()) {
                unavailable++;
                continue;
            }
            GuardCommandResult result = command.apply(candidate, destination.get());
            if (result.succeeded()) succeeded++;
            else if (result.outcome() == GuardCommandResult.Outcome.INVALID_GUARD) unavailable++;
            else rejected++;
        }
        return new GuardBatchCommandResult(succeeded, rejected, unavailable);
    }

    @FunctionalInterface
    public interface DestinationResolver<T> {
        Optional<BlockPos> resolve(T candidate, int slotIndex);
    }
}
