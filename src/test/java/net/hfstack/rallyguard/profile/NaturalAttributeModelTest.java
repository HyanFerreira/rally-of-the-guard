package net.hfstack.rallyguard.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NaturalAttributeModelTest {
    @Test
    void qualityUsesHalfUpAverage() {
        assertEquals(0, NaturalAttributeModel.quality(0, 0));
        assertEquals(5_001, NaturalAttributeModel.quality(5_000, 5_001));
        assertEquals(10_000, NaturalAttributeModel.quality(10_000, 10_000));
    }

    @Test
    void storedRollCombinesQualityBiasAndIndependentNoise() {
        assertEquals(0, NaturalAttributeModel.storedRoll(5_000, 0, 0));
        assertEquals(10_000, NaturalAttributeModel.storedRoll(10_000, 1, 1));
        assertEquals(-10_000, NaturalAttributeModel.storedRoll(0, -1, -1));
        assertEquals(5_500, NaturalAttributeModel.storedRoll(10_000, 0, 0));
    }

    @Test
    void signedMappingUsesIndependentNegativeAndPositiveBounds() {
        assertEquals(-0.08, NaturalAttributeModel.mapSignedRoll(-10_000, -0.08, 0.10));
        assertEquals(-0.04, NaturalAttributeModel.mapSignedRoll(-5_000, -0.08, 0.10));
        assertEquals(0.05, NaturalAttributeModel.mapSignedRoll(5_000, -0.08, 0.10));
        assertEquals(0.10, NaturalAttributeModel.mapSignedRoll(10_000, -0.08, 0.10));
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> NaturalAttributeModel.quality(-1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> NaturalAttributeModel.storedRoll(5_000, Double.NaN, 0));
    }
}
