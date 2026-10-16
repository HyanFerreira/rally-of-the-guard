package net.hfstack.rallyguard.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalGuardRegistryCodecTest {
    @Test
    void recordRoundTripsWithoutLosingLastKnownState() {
        GlobalGuardRecord guard = new GlobalGuardRecord(
                UUID.randomUUID(), "Robert", "minecraft:overworld", new BlockPos(10, 64, -12),
                900L, 6, "soldier", true, Optional.of("Village Guard"), 3
        );
        GlobalGuardRegistry original = new GlobalGuardRegistry(1, List.of(guard));

        GlobalGuardRegistryRead.Available decoded = assertInstanceOf(
                GlobalGuardRegistryRead.Available.class,
                GlobalGuardRegistryCodec.decode(GlobalGuardRegistryCodec.encode(original))
        );

        assertEquals(original, decoded.registry());
        assertFalse(decoded.repaired());
    }

    @Test
    void malformedAndDuplicateEntriesAreDiscarded() {
        UUID id = UUID.randomUUID();
        CompoundTag root = GlobalGuardRegistryCodec.encode(new GlobalGuardRegistry(1, List.of(
                record(id, "Arthur"), record(UUID.randomUUID(), "Beatrice")
        )));
        ListTag guards = root.getListOrEmpty("guards");
        guards.add(guards.getFirst().copy());
        CompoundTag malformed = new CompoundTag();
        malformed.putString("id", "invalid");
        guards.add(malformed);

        GlobalGuardRegistryRead.Available decoded = assertInstanceOf(
                GlobalGuardRegistryRead.Available.class, GlobalGuardRegistryCodec.decode(root)
        );
        assertEquals(2, decoded.registry().guards().size());
        assertTrue(decoded.repaired());
    }

    @Test
    void futureSchemaIsReadOnly() {
        CompoundTag root = new CompoundTag();
        root.putInt("schema_version", 5);
        root.putString("future", "preserve");
        CompoundTag before = root.copy();

        GlobalGuardRegistryRead.FutureSchema result = assertInstanceOf(
                GlobalGuardRegistryRead.FutureSchema.class, GlobalGuardRegistryCodec.decode(root)
        );
        assertEquals(5, result.schemaVersion());
        assertEquals(before, root);
    }

    private static GlobalGuardRecord record(UUID id, String name) {
        return new GlobalGuardRecord(
                id, name, "minecraft:overworld", BlockPos.ZERO, 1L, 1, "recruit", false, Optional.empty(), 0
        );
    }
}
