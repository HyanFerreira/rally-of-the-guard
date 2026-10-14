package net.hfstack.rallyguard.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XpSharesTest {
    @Test
    void normalizesConfiguredWeights() {
        XpShares shares = XpShares.normalize(3.0, 1.0);

        assertEquals(0.75, shares.contribution());
        assertEquals(0.25, shares.finalBlow());
    }

    @Test
    void invalidOrEmptyWeightsUseDocumentedDefaults() {
        assertEquals(new XpShares(0.80, 0.20), XpShares.normalize(Double.NaN, -4.0));
        assertEquals(new XpShares(0.80, 0.20), XpShares.normalize(0.0, 0.0));
    }
}
