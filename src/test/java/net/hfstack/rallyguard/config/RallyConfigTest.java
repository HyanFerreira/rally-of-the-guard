package net.hfstack.rallyguard.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RallyConfigTest {
    @Test
    void retreatDefaultsAndDistanceClampsToSupportedRange() {
        boolean originalEnabled = RallyConfig.rallyRetreatEnabled;
        double originalDistance = RallyConfig.rallyRetreatDistance;
        try {
            assertTrue(RallyConfig.rallyRetreatEnabled());
            assertEquals(24.0, RallyConfig.rallyRetreatDistance());

            RallyConfig.rallyRetreatDistance = 0.0;
            assertEquals(1.0, RallyConfig.rallyRetreatDistance());

            RallyConfig.rallyRetreatDistance = 999.0;
            assertEquals(512.0, RallyConfig.rallyRetreatDistance());
        } finally {
            RallyConfig.rallyRetreatEnabled = originalEnabled;
            RallyConfig.rallyRetreatDistance = originalDistance;
        }
    }
}
