package net.hfstack.rallyguard.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NaturalCaptainPolicyTest {
    @Test
    void regionalPopulationReducesAndThenBlocksSelectionChance() {
        assertEquals(0.05, NaturalCaptainPolicy.adjustedChance(true, 0.05, 0, 2));
        assertEquals(0.025, NaturalCaptainPolicy.adjustedChance(true, 0.05, 1, 2));
        assertEquals(0.0, NaturalCaptainPolicy.adjustedChance(true, 0.05, 2, 2));
        assertEquals(0.0, NaturalCaptainPolicy.adjustedChance(true, 0.05, 50, 2));
    }

    @Test
    void disabledOrInvalidConfigurationCannotSelectCaptain() {
        assertEquals(0.0, NaturalCaptainPolicy.adjustedChance(false, 0.05, 0, 2));
        assertEquals(0.0, NaturalCaptainPolicy.adjustedChance(true, Double.NaN, 0, 2));
        assertEquals(0.0, NaturalCaptainPolicy.adjustedChance(true, 0.05, 0, 0));
        assertEquals(1.0, NaturalCaptainPolicy.adjustedChance(true, 2.0, -5, 2));
    }

    @Test
    void selectionUsesExclusiveUpperBoundAndRejectsInvalidRolls() {
        assertTrue(NaturalCaptainPolicy.isSelected(0.049, 0.05));
        assertFalse(NaturalCaptainPolicy.isSelected(0.05, 0.05));
        assertFalse(NaturalCaptainPolicy.isSelected(Double.NaN, 0.05));
        assertFalse(NaturalCaptainPolicy.isSelected(-0.1, 0.05));
    }

    @Test
    void selectedCaptainStartsAtConfiguredMinimumWithoutDemotingVeterans() {
        assertEquals(5, NaturalCaptainPolicy.minimumLevel(true, 2, 5));
        assertEquals(7, NaturalCaptainPolicy.minimumLevel(true, 7, 5));
        assertEquals(2, NaturalCaptainPolicy.minimumLevel(false, 2, 5));
        assertEquals(10, NaturalCaptainPolicy.minimumLevel(true, 2, 99));
    }
}
