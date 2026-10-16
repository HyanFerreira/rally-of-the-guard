package net.hfstack.rallyguard.commandpost;

import net.hfstack.rallyguard.formation.RallyFormation;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandPostStateCodecTest {
    @Test
    void stateRoundTripsWithOwnerFormationAndAssignments() {
        UUID postId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        CommandPostState expected = new CommandPostState(
                CommandPostState.SCHEMA_VERSION,
                postId,
                Optional.of(ownerId),
                "Portão Norte",
                true,
                24,
                RallyFormation.SHIELD_WALL,
                List.of(UUID.randomUUID(), UUID.randomUUID())
        );

        CommandPostStateCodec.ReadResult.Available decoded = assertInstanceOf(
                CommandPostStateCodec.ReadResult.Available.class,
                CommandPostStateCodec.decode(CommandPostStateCodec.encode(expected), UUID.randomUUID())
        );

        assertEquals(expected, decoded.state());
        assertFalse(decoded.repaired());
    }

    @Test
    void malformedDataIsRepairedAndBounded() {
        UUID fallback = UUID.randomUUID();
        CompoundTag malformed = new CompoundTag();
        malformed.putInt("schema_version", CommandPostState.SCHEMA_VERSION);
        malformed.putString("post_id", "invalid");
        malformed.putString("formation", "future");
        malformed.putInt("radius", 999);

        CommandPostStateCodec.ReadResult.Available decoded = assertInstanceOf(
                CommandPostStateCodec.ReadResult.Available.class,
                CommandPostStateCodec.decode(malformed, fallback)
        );

        assertTrue(decoded.repaired());
        assertEquals(fallback, decoded.state().postId());
        assertEquals(CommandPostState.MAX_RADIUS, decoded.state().radius());
        assertEquals(RallyFormation.SQUARE, decoded.state().formation());
    }

    @Test
    void futureSchemaIsDetectedWithoutInterpretation() {
        CompoundTag future = new CompoundTag();
        future.putInt("schema_version", 7);

        CommandPostStateCodec.ReadResult.FutureSchema decoded = assertInstanceOf(
                CommandPostStateCodec.ReadResult.FutureSchema.class,
                CommandPostStateCodec.decode(future, UUID.randomUUID())
        );

        assertEquals(7, decoded.schemaVersion());
    }
}
