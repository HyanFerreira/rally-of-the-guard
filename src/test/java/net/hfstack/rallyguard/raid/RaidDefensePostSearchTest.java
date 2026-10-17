package net.hfstack.rallyguard.raid;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RaidDefensePostSearchTest {
    @Test
    void skipsUnsafeOutOfBoundsAndReservedPosts() {
        BlockPos reservedPost = new BlockPos(0, 64, 0);
        BlockPos outside = new BlockPos(20, 64, 0);
        BlockPos selected = new BlockPos(4, 64, 0);
        Set<BlockPos> reserved = new HashSet<>(Set.of(reservedPost));

        Optional<BlockPos> result = RaidDefensePostSearch.firstAvailable(
                5,
                index -> switch (index) {
                    case 0 -> Optional.empty();
                    case 1 -> Optional.of(reservedPost);
                    case 2 -> Optional.of(outside);
                    default -> Optional.of(selected);
                },
                position -> position.getX() <= 16,
                reserved
        );

        assertEquals(Optional.of(selected), result);
        assertEquals(Set.of(reservedPost, selected), reserved);
    }
}
