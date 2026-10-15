package net.hfstack.rallyguard.order.move;

import net.minecraft.core.BlockPos;

import java.util.Objects;

public record MoveOrderState(int schemaVersion, boolean active, BlockPos target) {
    public static final int SCHEMA_VERSION = 1;

    public MoveOrderState {
        target = Objects.requireNonNull(target, "target").immutable();
    }

    public static MoveOrderState active(BlockPos target) {
        return new MoveOrderState(SCHEMA_VERSION, true, target);
    }
}
