package net.hfstack.rallyguard.order.move;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public final class MoveOrderSlotResolver {
    private MoveOrderSlotResolver() {
    }

    public static Optional<BlockPos> resolve(ServerLevel world, BlockPos anchor, int slotIndex) {
        Objects.requireNonNull(world, "world");
        return resolve(anchor, slotIndex, candidate -> isSafeStandPosition(world, candidate));
    }

    static Optional<BlockPos> resolve(BlockPos anchor, int slotIndex, Predicate<BlockPos> safePosition) {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(safePosition, "safePosition");
        BlockPos base = anchor.offset(MoveOrderSlotLayout.offset(slotIndex));
        for (int dy = 1; dy >= -3; dy--) {
            BlockPos candidate = base.offset(0, dy, 0);
            if (safePosition.test(candidate)) return Optional.of(candidate);
        }
        return Optional.empty();
    }

    static boolean isSafeCandidate(
            boolean loaded,
            boolean withinBorder,
            boolean solidFloor,
            boolean feetClear,
            boolean headClear
    ) {
        return loaded && withinBorder && solidFloor && feetClear && headClear;
    }

    private static boolean isSafeStandPosition(ServerLevel world, BlockPos position) {
        BlockPos floor = position.below();
        BlockPos head = position.above();
        boolean loaded = world.hasChunkAt(floor) && world.hasChunkAt(position) && world.hasChunkAt(head);
        boolean withinBorder = world.getWorldBorder().isWithinBounds(floor)
                && world.getWorldBorder().isWithinBounds(position)
                && world.getWorldBorder().isWithinBounds(head);
        if (!loaded || !withinBorder) return false;

        return isSafeCandidate(
                true,
                true,
                world.getBlockState(floor).isFaceSturdy(world, floor, Direction.UP),
                world.getBlockState(position).isAir(),
                world.getBlockState(head).isAir()
        );
    }
}
