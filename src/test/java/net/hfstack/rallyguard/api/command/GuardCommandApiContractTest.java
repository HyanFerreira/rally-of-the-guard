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
    void apiVersionEightExposesPositionalAndStanceCommands() throws NoSuchMethodException {
        assertEquals(8, RallyGuardApi.apiVersion());
        assertEquals(GuardCommandType.MOVE_TO, GuardCommandType.valueOf("MOVE_TO"));
        assertNotNull(GuardCommandService.class.getMethod(
                "moveTo",
                ServerPlayer.class,
                GuardEntity.class,
                BlockPos.class
        ));
        assertEquals(GuardCommandType.DEFEND_AREA, GuardCommandType.valueOf("DEFEND_AREA"));
        assertNotNull(GuardCommandService.class.getMethod(
                "defendArea",
                ServerPlayer.class,
                GuardEntity.class,
                BlockPos.class,
                BlockPos.class,
                int.class
        ));
        assertEquals(GuardCommandType.SET_COMBAT_STANCE, GuardCommandType.valueOf("SET_COMBAT_STANCE"));
        assertNotNull(GuardCommandService.class.getMethod(
                "setCombatStance",
                ServerPlayer.class,
                GuardEntity.class,
                GuardCombatStance.class
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
