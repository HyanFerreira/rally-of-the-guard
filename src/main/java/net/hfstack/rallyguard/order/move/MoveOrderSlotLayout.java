package net.hfstack.rallyguard.order.move;

import net.minecraft.core.BlockPos;

public final class MoveOrderSlotLayout {
    private static final int SPACING = 2;

    private MoveOrderSlotLayout() {
    }

    public static BlockPos offset(int index) {
        if (index < 0) throw new IllegalArgumentException("Slot index must be non-negative");
        if (index == 0) return BlockPos.ZERO;

        int remaining = index - 1;
        int ring = 1;
        while (remaining >= 8 * ring) {
            remaining -= 8 * ring;
            ring++;
        }

        int edge = 2 * ring;
        int x;
        int z;
        if (remaining < edge) {
            x = -ring + remaining;
            z = -ring;
        } else if ((remaining -= edge) < edge) {
            x = ring;
            z = -ring + remaining;
        } else if ((remaining -= edge) < edge) {
            x = ring - remaining;
            z = ring;
        } else {
            remaining -= edge;
            x = -ring;
            z = ring - remaining;
        }
        return new BlockPos(x * SPACING, 0, z * SPACING);
    }
}
