package net.hfstack.rallyguard.order.move;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

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

    public static Optional<BlockPos> resolve(ServerLevel world, BlockPos anchor, int slotIndex, Mob mob) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(mob, "mob");
        return resolve(anchor, slotIndex, candidate -> isSafeStandPosition(world, candidate, mob));
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

        BlockState floorState = world.getBlockState(floor);
        BlockState feetState = world.getBlockState(position);
        BlockState headState = world.getBlockState(head);
        return isSafeCandidate(
                true,
                true,
                usableFloor(world, floor, floorState),
                clearSpace(world, position, feetState),
                clearSpace(world, head, headState)
        );
    }

    private static boolean isSafeStandPosition(ServerLevel world, BlockPos position, Mob mob) {
        BlockPos floor = position.below();
        BlockPos head = position.above();
        boolean loaded = world.hasChunkAt(floor) && world.hasChunkAt(position) && world.hasChunkAt(head);
        boolean withinBorder = world.getWorldBorder().isWithinBounds(floor)
                && world.getWorldBorder().isWithinBounds(position)
                && world.getWorldBorder().isWithinBounds(head);
        if (!loaded || !withinBorder) return false;

        BlockState floorState = world.getBlockState(floor);
        BlockState feetState = world.getBlockState(position);
        BlockState headState = world.getBlockState(head);
        if (!isSafeCandidate(
                true,
                true,
                usableFloor(world, floor, floorState),
                clearSpace(world, position, feetState),
                clearSpace(world, head, headState)
        )) return false;
        if (hazardous(floorState) || hazardous(feetState) || hazardous(headState)) return false;

        AABB destination = mob.getDimensions(Pose.STANDING).makeBoundingBox(
                position.getX() + 0.5,
                position.getY(),
                position.getZ() + 0.5
        );
        return world.noBlockCollision(mob, destination);
    }

    private static boolean usableFloor(ServerLevel world, BlockPos position, BlockState state) {
        return usableFloor(state.getFluidState().isEmpty(), state.getCollisionShape(world, position).isEmpty());
    }

    private static boolean clearSpace(ServerLevel world, BlockPos position, BlockState state) {
        return clearSpace(state.getFluidState().isEmpty(), state.getCollisionShape(world, position).isEmpty());
    }

    static boolean usableFloor(boolean fluidEmpty, boolean collisionEmpty) {
        return fluidEmpty && !collisionEmpty;
    }

    static boolean clearSpace(boolean fluidEmpty, boolean collisionEmpty) {
        return fluidEmpty && collisionEmpty;
    }

    static boolean hazardous(BlockState state) {
        return state.is(Blocks.CACTUS)
                || state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.CAMPFIRE)
                || state.is(Blocks.SOUL_CAMPFIRE)
                || state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.WITHER_ROSE)
                || state.is(Blocks.POINTED_DRIPSTONE);
    }
}
