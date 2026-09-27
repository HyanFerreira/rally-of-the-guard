package net.hfstack.rallyguard.order;

import net.hfstack.rallyguard.config.RallyConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public final class RallyFormationSlots {
    private RallyFormationSlots() {
    }

    private static final double FIRST_ROW_OFFSET = 1.8;

    public static Vec3 escortSlot(Player player, int index) {
        return escortSlot(player, index, player.getYRot(), false);
    }

    public static Vec3 escortSlot(Player player, int index, float yawDegrees, boolean inFront) {
        double yaw = Math.toRadians(yawDegrees);
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw)).normalize();
        Vec3 right = new Vec3(forward.z, 0.0, -forward.x).normalize();

        int row = index / 2;
        double side = index % 2 == 0 ? -1.0 : 1.0;
        double rowOffset = FIRST_ROW_OFFSET + row * RallyConfig.formationRowSpacing();
        Vec3 rowVector = forward.scale(inFront ? rowOffset : -rowOffset);

        return new Vec3(player.getX(), player.getY(), player.getZ())
                .add(right.scale(side * RallyConfig.formationColumnSpacing()))
                .add(rowVector);
    }

    public static Vec3 safeEscortSlot(ServerLevel world, Player player, int index) {
        return safeEscortSlot(world, player, index, player.getYRot(), false);
    }

    public static Vec3 safeEscortSlot(ServerLevel world, Player player, int index, float yawDegrees, boolean inFront) {
        Vec3 raw = escortSlot(player, index, yawDegrees, inFront);
        BlockPos base = BlockPos.containing(raw.x, player.getY(), raw.z);

        for (int dy = 1; dy >= -3; dy--) {
            BlockPos candidate = base.offset(0, dy, 0);
            if (isSafeStandPosition(world, candidate)) {
                return new Vec3(raw.x, candidate.getY(), raw.z);
            }
        }

        return new Vec3(raw.x, player.getY(), raw.z);
    }

    private static boolean isSafeStandPosition(ServerLevel world, BlockPos pos) {
        return !world.getBlockState(pos.below()).isAir()
                && world.getBlockState(pos).isAir()
                && world.getBlockState(pos.above()).isAir();
    }
}
