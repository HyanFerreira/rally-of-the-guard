package net.hfstack.rallyguard.profile;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardProfileCodecTest {
    private static final Set<String> SCHEMA_KEYS = Set.of(
            "schema_version",
            "generation_version",
            "origin",
            "total_xp_milli",
            "natural_quality",
            "health_roll",
            "attack_roll",
            "armor_roll",
            "speed_roll",
            "kills",
            "assists"
    );

    @Test
    void roundTripPreservesEverySchemaV1Field() {
        GuardProfile original = GuardProfile.createV1(
                GuardProfileOrigin.NATURAL,
                155_750,
                7_350,
                4_200,
                -1_800,
                6_100,
                900,
                14,
                9
        );

        GuardProfileReadResult.Available result = available(GuardProfileCodec.decode(
                GuardProfileCodec.encode(original)
        ));

        assertEquals(original, result.profile());
        assertFalse(result.repaired());
    }

    @Test
    void missingSchemaIsRepairedAsVersionOne() {
        CompoundTag tag = fullTag();
        tag.remove("schema_version");

        GuardProfileReadResult.Available result = available(GuardProfileCodec.decode(tag));

        assertEquals(1, result.profile().schemaVersion());
        assertTrue(result.repaired());
    }

    @Test
    void partialVersionOnePreservesValidFieldsAndDefaultsMissingFields() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema_version", 1);
        tag.putInt("natural_quality", 7_000);
        tag.putInt("attack_roll", -2_500);
        tag.putLong("kills", -4);

        GuardProfileReadResult.Available result = available(GuardProfileCodec.decode(tag));

        assertEquals(7_000, result.profile().naturalQuality());
        assertEquals(-2_500, result.profile().attackRoll());
        assertEquals(0, result.profile().healthRoll());
        assertEquals(0, result.profile().kills());
        assertTrue(result.repaired());
    }

    @Test
    void negativeCountersAndOutOfRangeRollsAreClamped() {
        CompoundTag tag = fullTag();
        tag.putLong("total_xp_milli", Long.MIN_VALUE);
        tag.putInt("health_roll", Integer.MIN_VALUE);
        tag.putInt("speed_roll", Integer.MAX_VALUE);
        tag.putLong("kills", -1);
        tag.putLong("assists", Long.MIN_VALUE);

        GuardProfileReadResult.Available result = available(GuardProfileCodec.decode(tag));

        assertEquals(0, result.profile().totalXpMilli());
        assertEquals(-10_000, result.profile().healthRoll());
        assertEquals(10_000, result.profile().speedRoll());
        assertEquals(0, result.profile().kills());
        assertEquals(0, result.profile().assists());
        assertTrue(result.repaired());
    }

    @Test
    void unknownOriginBecomesOtherAndMarksResultRepaired() {
        CompoundTag tag = fullTag();
        tag.putString("origin", "outsider");

        GuardProfileReadResult.Available result = available(GuardProfileCodec.decode(tag));

        assertEquals(GuardProfileOrigin.OTHER, result.profile().originType());
        assertTrue(result.repaired());
    }

    @Test
    void malformedTypedFieldsUseConservativeDefaultsAndMarkRepaired() {
        CompoundTag tag = fullTag();
        tag.putString("total_xp_milli", "not-a-long");
        tag.putString("natural_quality", "not-an-int");
        tag.putInt("origin", 7);

        GuardProfileReadResult.Available result = available(GuardProfileCodec.decode(tag));

        assertEquals(0, result.profile().totalXpMilli());
        assertEquals(5_000, result.profile().naturalQuality());
        assertEquals(GuardProfileOrigin.LEGACY, result.profile().originType());
        assertTrue(result.repaired());
    }

    @Test
    void futureSchemaReturnsFutureResultWithoutProfile() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema_version", 2);
        tag.putString("future_field", "keep-me");

        GuardProfileReadResult.FutureSchema result = assertInstanceOf(
                GuardProfileReadResult.FutureSchema.class,
                GuardProfileCodec.decode(tag)
        );

        assertEquals(2, result.schemaVersion());
    }

    @Test
    void encodeWritesOnlyTheApprovedSchemaFields() {
        CompoundTag encoded = GuardProfileCodec.encode(GuardProfile.legacyNeutral());

        assertEquals(SCHEMA_KEYS, encoded.keySet());
    }

    @Test
    void nullCompoundReturnsAbsent() {
        assertInstanceOf(GuardProfileReadResult.Absent.class, GuardProfileCodec.decode(null));
    }

    @Test
    void futureSchemaDecodeDoesNotMutateOriginalCompound() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema_version", 2);
        tag.putString("future_field", "keep-me");
        CompoundTag before = tag.copy();

        GuardProfileCodec.decode(tag);

        assertEquals(before, tag);
    }

    @Test
    void decodeDoesNotMutateRepairableOriginalCompound() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("natural_quality", 12_000);
        tag.putLong("kills", -2);
        CompoundTag before = tag.copy();

        GuardProfileCodec.decode(tag);

        assertEquals(before, tag);
    }

    private static GuardProfileReadResult.Available available(GuardProfileReadResult result) {
        return assertInstanceOf(GuardProfileReadResult.Available.class, result);
    }

    private static CompoundTag fullTag() {
        return GuardProfileCodec.encode(GuardProfile.createV1(
                GuardProfileOrigin.NATURAL,
                155_750,
                7_350,
                4_200,
                -1_800,
                6_100,
                900,
                14,
                9
        ));
    }
}
