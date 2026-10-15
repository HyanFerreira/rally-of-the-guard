package net.hfstack.rallyguard.order.defend;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefendAreaPersistencePolicyTest {
    @Test
    void supportedOrAbsentStateCanBeReplaced() {
        assertTrue(DefendAreaPersistencePolicy.canReplace(new DefendAreaRead.Available(Optional.empty())));
        assertTrue(DefendAreaPersistencePolicy.canReplace(new DefendAreaRead.Available(Optional.of(
                DefendAreaState.active(BlockPos.ZERO, BlockPos.ZERO, 16)
        ))));
    }

    @Test
    void futureStateRejectsAnotherDefendOrderButAllowsExplicitCancellation() {
        DefendAreaRead.Unsupported future = new DefendAreaRead.Unsupported(2);

        assertFalse(DefendAreaPersistencePolicy.canReplace(future));
        assertTrue(DefendAreaPersistencePolicy.canExplicitlyClear(future));
    }
}
