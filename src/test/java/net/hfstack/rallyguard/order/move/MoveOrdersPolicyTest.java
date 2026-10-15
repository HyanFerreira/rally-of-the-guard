package net.hfstack.rallyguard.order.move;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveOrdersPolicyTest {
    @Test
    void supportedOrAbsentStateCanBeReplaced() {
        assertTrue(MoveOrderPersistencePolicy.canReplace(new MoveOrderRead.Available(Optional.empty())));
        assertTrue(MoveOrderPersistencePolicy.canReplace(new MoveOrderRead.Available(
                Optional.of(MoveOrderState.active(new BlockPos(1, 2, 3)))
        )));
    }

    @Test
    void futureStateCannotBeReplacedByAnotherMoveOrder() {
        assertFalse(MoveOrderPersistencePolicy.canReplace(new MoveOrderRead.Unsupported(2)));
    }

    @Test
    void explicitMutuallyExclusiveOrdersMayAlwaysClearState() {
        assertTrue(MoveOrderPersistencePolicy.canExplicitlyClear(new MoveOrderRead.Unsupported(2)));
    }
}
