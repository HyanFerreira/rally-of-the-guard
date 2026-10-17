package net.hfstack.rallyguard.raid;

import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;

final class RaidDefensePostSearch {
    private RaidDefensePostSearch() {
    }

    static Optional<BlockPos> firstAvailable(
            int candidateLimit,
            IntFunction<Optional<BlockPos>> resolver,
            Predicate<BlockPos> withinTerritory,
            Set<BlockPos> reserved
    ) {
        for (int candidateIndex = 0; candidateIndex < candidateLimit; candidateIndex++) {
            Optional<BlockPos> candidate = resolver.apply(candidateIndex)
                    .filter(withinTerritory)
                    .filter(position -> !reserved.contains(position));
            if (candidate.isPresent()) {
                BlockPos selected = candidate.orElseThrow().immutable();
                reserved.add(selected);
                return Optional.of(selected);
            }
        }
        return Optional.empty();
    }
}
