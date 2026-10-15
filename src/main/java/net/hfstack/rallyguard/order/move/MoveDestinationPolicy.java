package net.hfstack.rallyguard.order.move;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public final class MoveDestinationPolicy {
    private MoveDestinationPolicy() {
    }

    public static BlockPos anchor(BlockPos clicked, Direction face) {
        return Objects.requireNonNull(clicked, "clicked").relative(Objects.requireNonNull(face, "face"));
    }

    public static boolean withinInteractionRange(Vec3 playerPosition, BlockPos clicked, double maxDistance) {
        Objects.requireNonNull(playerPosition, "playerPosition");
        Objects.requireNonNull(clicked, "clicked");
        if (maxDistance < 0.0) return false;
        return playerPosition.distanceToSqr(Vec3.atCenterOf(clicked)) <= maxDistance * maxDistance;
    }
}
