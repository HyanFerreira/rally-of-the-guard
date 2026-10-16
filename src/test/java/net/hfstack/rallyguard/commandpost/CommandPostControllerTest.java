package net.hfstack.rallyguard.commandpost;

import net.hfstack.rallyguard.formation.RallyFormation;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandPostControllerTest {
    @Test
    void formationPostsAreStableAroundTheCommandPost() {
        CommandPostState state = new CommandPostState(
                CommandPostState.SCHEMA_VERSION,
                UUID.randomUUID(),
                Optional.of(UUID.randomUUID()),
                "Norte",
                true,
                16,
                RallyFormation.LINE,
                List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        );
        BlockPos center = new BlockPos(100, 64, -20);

        assertEquals(new BlockPos(98, 64, -18), CommandPostController.formationPost(center, state, 0));
        assertEquals(new BlockPos(100, 64, -18), CommandPostController.formationPost(center, state, 1));
        assertEquals(new BlockPos(102, 64, -18), CommandPostController.formationPost(center, state, 2));
    }
}
