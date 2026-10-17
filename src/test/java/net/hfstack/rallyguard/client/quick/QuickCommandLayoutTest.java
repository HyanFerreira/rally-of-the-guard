package net.hfstack.rallyguard.client.quick;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickCommandLayoutTest {
    @Test
    void sixActionsOccupyACompactTwoColumnGrid() {
        QuickCommandLayout layout = QuickCommandLayout.forScreen(854, 480);

        assertEquals(6, layout.slots().size());
        assertEquals(6, new HashSet<>(layout.slots().stream()
                .map(slot -> slot.x() + ":" + slot.y())
                .toList()).size());
        assertEquals(QuickCommandAction.FOLLOW, layout.slots().get(0).action());
        assertEquals(QuickCommandAction.WAIT, layout.slots().get(1).action());
        assertEquals(QuickCommandAction.DEFEND, layout.slots().get(5).action());

        assertEquals(2, layout.slots().stream().map(QuickCommandLayout.Slot::x).distinct().count());
        assertEquals(3, layout.slots().stream().map(QuickCommandLayout.Slot::y).distinct().count());
    }

    @Test
    void buttonsRemainInsideASmallGui() {
        QuickCommandLayout layout = QuickCommandLayout.forScreen(320, 240);

        for (QuickCommandLayout.Slot slot : layout.slots()) {
            assertTrue(slot.x() >= 4);
            assertTrue(slot.y() >= 4);
            assertTrue(slot.x() + QuickCommandLayout.BUTTON_WIDTH <= 316);
            assertTrue(slot.y() + QuickCommandLayout.BUTTON_HEIGHT <= 236);
        }
    }

    @Test
    void everyButtonRemainsInsideTheUnifiedPanel() {
        QuickCommandLayout layout = QuickCommandLayout.forScreen(854, 480);
        int panelLeft = (854 - QuickCommandLayout.PANEL_WIDTH) / 2;
        int panelRight = panelLeft + QuickCommandLayout.PANEL_WIDTH;
        int panelTop = (480 - QuickCommandLayout.PANEL_HEIGHT) / 2;
        int panelBottom = panelTop + QuickCommandLayout.PANEL_HEIGHT;

        for (QuickCommandLayout.Slot slot : layout.slots()) {
            assertTrue(slot.x() >= panelLeft + QuickCommandLayout.PANEL_PADDING);
            assertTrue(slot.x() + QuickCommandLayout.BUTTON_WIDTH <= panelRight - QuickCommandLayout.PANEL_PADDING);
            assertTrue(slot.y() >= panelTop + QuickCommandLayout.ACTIONS_TOP);
            assertTrue(slot.y() + QuickCommandLayout.BUTTON_HEIGHT <= panelBottom - QuickCommandLayout.PANEL_PADDING);
        }
    }

    @Test
    void raidSettingsRowRemainsInsideTheUnifiedPanel() {
        int panelTop = (240 - QuickCommandLayout.PANEL_HEIGHT) / 2;
        int settingsTop = panelTop + QuickCommandLayout.RAID_SETTINGS_TOP;
        assertTrue(settingsTop >= panelTop);
        assertTrue(settingsTop + QuickCommandLayout.BUTTON_HEIGHT
                <= panelTop + QuickCommandLayout.PANEL_HEIGHT - QuickCommandLayout.PANEL_PADDING);
    }
}
