package net.hfstack.rallyguard.api.command;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GuardCommandApiContractTest {
    @Test
    void apiVersionSixExposesMoveToAsAPositionalCommand() throws NoSuchMethodException {
        assertEquals(6, RallyGuardApi.apiVersion());
        assertEquals(GuardCommandType.MOVE_TO, GuardCommandType.valueOf("MOVE_TO"));
        assertNotNull(GuardCommandService.class.getMethod(
                "moveTo",
                ServerPlayer.class,
                GuardEntity.class,
                BlockPos.class
        ));
    }

    @Test
    void unsupportedMoveStateHasAMachineReadableOutcome() {
        assertEquals(
                GuardCommandResult.Outcome.UNSUPPORTED_DATA,
                GuardCommandResult.unsupportedData().outcome()
        );
    }
}
