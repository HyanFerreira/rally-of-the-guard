package net.hfstack.rallyguard.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuardHealthAdjustmentTest {
    @Test
    void loadAndRefreshPreserveHealthPercentage() {
        assertEquals(15.0F, GuardHealthAdjustment.adjustedHealth(
                10.0F, 20.0F, 30.0F, GuardAttributeApplicationCause.LOAD));
        assertEquals(5.0F, GuardHealthAdjustment.adjustedHealth(
                10.0F, 20.0F, 10.0F, GuardAttributeApplicationCause.CONFIGURATION_REFRESH));
    }

    @Test
    void levelUpAddsOnlyPositiveMaximumHealthDelta() {
        assertEquals(16.0F, GuardHealthAdjustment.adjustedHealth(
                10.0F, 20.0F, 26.0F, GuardAttributeApplicationCause.LEVEL_UP));
        assertEquals(10.0F, GuardHealthAdjustment.adjustedHealth(
                10.0F, 20.0F, 18.0F, GuardAttributeApplicationCause.LEVEL_UP));
    }

    @Test
    void unchangedApplicationNeverHeals() {
        assertEquals(7.0F, GuardHealthAdjustment.adjustedHealth(
                7.0F, 20.0F, 20.0F, GuardAttributeApplicationCause.LOAD));
    }

    @Test
    void reducedMaximumClampsHealth() {
        assertEquals(12.0F, GuardHealthAdjustment.adjustedHealth(
                18.0F, 20.0F, 12.0F, GuardAttributeApplicationCause.LEVEL_UP));
    }
}
