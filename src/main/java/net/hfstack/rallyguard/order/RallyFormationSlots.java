package net.hfstack.rallyguard.order;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class RallyFormationSlots {
    private RallyFormationSlots() {
    }

    private static final double SIDE_SPACING = 1.35;
    private static final double ROW_SPACING = 1.8;
    private static final double FIRST_ROW_OFFSET = 1.8;

    public static Vec3d escortSlot(PlayerEntity player, int index) {
        return escortSlot(player, index, player.getYaw(), false);
    }

    public static Vec3d escortSlot(PlayerEntity player, int index, float yawDegrees, boolean inFront) {
        double yaw = Math.toRadians(yawDegrees);
        Vec3d forward = new Vec3d(-Math.sin(yaw), 0.0, Math.cos(yaw)).normalize();
        Vec3d right = new Vec3d(forward.z, 0.0, -forward.x).normalize();

        int row = index / 2;
        double side = index % 2 == 0 ? -1.0 : 1.0;
        double rowOffset = FIRST_ROW_OFFSET + row * ROW_SPACING;
        Vec3d rowVector = forward.multiply(inFront ? rowOffset : -rowOffset);

        return new Vec3d(player.getX(), player.getY(), player.getZ())
                .add(right.multiply(side * SIDE_SPACING))
                .add(rowVector);
    }

    public static Vec3d safeEscortSlot(ServerWorld world, PlayerEntity player, int index) {
        return safeEscortSlot(world, player, index, player.getYaw(), false);
    }

    public static Vec3d safeEscortSlot(ServerWorld world, PlayerEntity player, int index, float yawDegrees, boolean inFront) {
        Vec3d raw = escortSlot(player, index, yawDegrees, inFront);
        BlockPos base = BlockPos.ofFloored(raw.x, player.getY(), raw.z);

        for (int dy = 1; dy >= -3; dy--) {
            BlockPos candidate = base.add(0, dy, 0);
            if (isSafeStandPosition(world, candidate)) {
                return new Vec3d(raw.x, candidate.getY(), raw.z);
            }
        }

        return new Vec3d(raw.x, player.getY(), raw.z);
    }

    private static boolean isSafeStandPosition(ServerWorld world, BlockPos pos) {
        return !world.getBlockState(pos.down()).isAir()
                && world.getBlockState(pos).isAir()
                && world.getBlockState(pos.up()).isAir();
    }
}
