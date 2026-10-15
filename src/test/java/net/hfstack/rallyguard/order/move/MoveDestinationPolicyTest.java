package net.hfstack.rallyguard.order.move;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveDestinationPolicyTest {
    @Test
    void clickedFaceSelectsTheAdjacentAnchorForEveryDirection() {
        BlockPos clicked = new BlockPos(10, 20, 30);
        for (Direction face : Direction.values()) {
            assertEquals(clicked.relative(face), MoveDestinationPolicy.anchor(clicked, face));
        }
    }

    @Test
    void interactionRangeIncludesExactlyEightBlocksFromClickedCenter() {
        BlockPos clicked = new BlockPos(8, 0, 0);

        assertTrue(MoveDestinationPolicy.withinInteractionRange(new Vec3(0.5, 0.5, 0.5), clicked, 8.0));
        assertFalse(MoveDestinationPolicy.withinInteractionRange(new Vec3(0.499, 0.5, 0.5), clicked, 8.0));
    }
}
