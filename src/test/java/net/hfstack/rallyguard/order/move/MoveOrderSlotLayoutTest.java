package net.hfstack.rallyguard.order.move;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoveOrderSlotLayoutTest {
    @Test
    void centerAndFirstTwoRingsHaveStableTwoBlockSpacing() {
        List<BlockPos> expected = List.of(
                pos(0, 0),
                pos(-2, -2), pos(0, -2), pos(2, -2), pos(2, 0),
                pos(2, 2), pos(0, 2), pos(-2, 2), pos(-2, 0),
                pos(-4, -4), pos(-2, -4), pos(0, -4), pos(2, -4), pos(4, -4),
                pos(4, -2), pos(4, 0), pos(4, 2), pos(4, 4),
                pos(2, 4), pos(0, 4), pos(-2, 4), pos(-4, 4),
                pos(-4, 2), pos(-4, 0), pos(-4, -2)
        );

        assertEquals(expected, IntStream.range(0, expected.size())
                .mapToObj(MoveOrderSlotLayout::offset)
                .toList());
    }

    @Test
    void negativeSlotIndexIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> MoveOrderSlotLayout.offset(-1));
    }

    private static BlockPos pos(int x, int z) {
        return new BlockPos(x, 0, z);
    }
}
