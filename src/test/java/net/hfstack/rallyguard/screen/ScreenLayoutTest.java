package net.hfstack.rallyguard.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScreenLayoutTest {
    @Test
    void centersContentInsideItsPanelInsteadOfUsingAColumnOrigin() {
        assertEquals(310, ScreenLayout.panelCenterX(100, 420));
    }

    @Test
    void centersSingleLineTextVerticallyInsideCommandRow() {
        assertEquals(109, ScreenLayout.centeredLineY(100, 28, 9));
    }

    @Test
    void keepsExpandedMusterPanelInsideNarrowScreens() {
        assertEquals(420, ScreenLayout.boundedPanelWidth(640, 420, 320, 12));
        assertEquals(336, ScreenLayout.boundedPanelWidth(360, 420, 320, 12));
        assertEquals(276, ScreenLayout.boundedPanelWidth(300, 420, 320, 12));
    }
}
