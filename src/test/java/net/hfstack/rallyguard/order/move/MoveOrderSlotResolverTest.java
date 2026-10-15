package net.hfstack.rallyguard.order.move;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveOrderSlotResolverTest {
    @Test
    void verticalSearchUsesAShortDeterministicWindow() {
        BlockPos anchor = new BlockPos(10, 64, 10);
        List<BlockPos> visited = new ArrayList<>();
        BlockPos accepted = new BlockPos(8, 63, 8);

        Optional<BlockPos> result = MoveOrderSlotResolver.resolve(anchor, 1, candidate -> {
            visited.add(candidate);
            return candidate.equals(accepted);
        });

        assertEquals(Optional.of(accepted), result);
        assertEquals(List.of(
                new BlockPos(8, 65, 8),
                new BlockPos(8, 64, 8),
                new BlockPos(8, 63, 8)
        ), visited);
    }

    @Test
    void noSafePositionRejectsOnlyThatSlot() {
        assertEquals(Optional.empty(), MoveOrderSlotResolver.resolve(BlockPos.ZERO, 0, ignored -> false));
    }

    @Test
    void worldCandidateRequiresLoadedBorderedSolidAndClearBlocks() {
        assertTrue(MoveOrderSlotResolver.isSafeCandidate(true, true, true, true, true));
        assertFalse(MoveOrderSlotResolver.isSafeCandidate(false, true, true, true, true));
        assertFalse(MoveOrderSlotResolver.isSafeCandidate(true, false, true, true, true));
        assertFalse(MoveOrderSlotResolver.isSafeCandidate(true, true, false, true, true));
        assertFalse(MoveOrderSlotResolver.isSafeCandidate(true, true, true, false, true));
        assertFalse(MoveOrderSlotResolver.isSafeCandidate(true, true, true, true, false));
    }
}
