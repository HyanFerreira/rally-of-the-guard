package net.hfstack.rallyguard.squad;

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

class SquadRegistryCodecTest {
    private static final UUID ALPHA_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID BRAVO_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID OFFICER_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");

    @Test
    void roundTripPreservesOrderedSquadsAndOfficer() {
        SquadRegistry original = new SquadRegistry(1, List.of(
                new SquadDefinition(ALPHA_ID, "Alpha", Optional.of(OFFICER_ID)),
                new SquadDefinition(BRAVO_ID, "Bravo", Optional.empty())
        ));

        SquadRegistryReadResult.Available result = available(SquadRegistryCodec.decode(
                SquadRegistryCodec.encode(original)
        ));

        assertEquals(original, result.registry());
        assertFalse(result.repaired());
    }

    @Test
    void duplicateIdsKeepFirstValidDefinitionAndMarkRepair() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema_version", 1);
        ListTag squads = new ListTag();
        squads.add(definition(ALPHA_ID, "Alpha", null));
        squads.add(definition(ALPHA_ID, "Duplicate", OFFICER_ID));
        tag.put("squads", squads);

        SquadRegistryReadResult.Available result = available(SquadRegistryCodec.decode(tag));

        assertEquals(List.of(new SquadDefinition(ALPHA_ID, "Alpha", Optional.empty())), result.registry().squads());
        assertTrue(result.repaired());
    }

    @Test
    void malformedDefinitionsAreDiscardedAndInvalidOfficerIsCleared() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema_version", 1);
        ListTag squads = new ListTag();
        CompoundTag valid = definition(ALPHA_ID, " Alpha ", null);
        valid.putString("officer", "not-a-uuid");
        squads.add(valid);
        CompoundTag invalid = new CompoundTag();
        invalid.putString("id", "not-a-uuid");
        invalid.putString("name", "Broken");
        squads.add(invalid);
        tag.put("squads", squads);

        SquadRegistryReadResult.Available result = available(SquadRegistryCodec.decode(tag));

        assertEquals(List.of(new SquadDefinition(ALPHA_ID, "Alpha", Optional.empty())), result.registry().squads());
        assertTrue(result.repaired());
    }

    @Test
    void nullTagIsAbsent() {
        assertInstanceOf(SquadRegistryReadResult.Absent.class, SquadRegistryCodec.decode(null));
    }

    @Test
    void futureSchemaIsPreservedWithoutMutatingInput() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema_version", 2);
        tag.putString("future", "keep");
        CompoundTag before = tag.copy();

        SquadRegistryReadResult.FutureSchema result = assertInstanceOf(
                SquadRegistryReadResult.FutureSchema.class,
                SquadRegistryCodec.decode(tag)
        );

        assertEquals(2, result.schemaVersion());
        assertEquals(before, tag);
    }

    private static CompoundTag definition(UUID id, String name, UUID officer) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id.toString());
        tag.putString("name", name);
        if (officer != null) tag.putString("officer", officer.toString());
        return tag;
    }

    private static SquadRegistryReadResult.Available available(SquadRegistryReadResult result) {
        return assertInstanceOf(SquadRegistryReadResult.Available.class, result);
    }
}
