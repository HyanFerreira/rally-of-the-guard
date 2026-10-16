package net.hfstack.rallyguard.client.quick;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuickCommandLayoutTest {
    @Test
    void sixActionsOccupyDistinctClockwiseSlots() {
        QuickCommandLayout layout = QuickCommandLayout.forScreen(854, 480);

        assertEquals(6, layout.slots().size());
        assertEquals(6, new HashSet<>(layout.slots().stream()
                .map(slot -> slot.x() + ":" + slot.y())
                .toList()).size());
        assertEquals(QuickCommandAction.FOLLOW, layout.slots().get(0).action());
        assertEquals(QuickCommandAction.PATROL, layout.slots().get(5).action());
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
}
