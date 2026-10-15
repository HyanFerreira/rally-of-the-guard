package net.hfstack.rallyguard.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormationButtonStyleTest {
    @Test
    void keyboardFocusOrHoverUsesAVisibleWhiteOutline() {
        assertEquals(0xFFFFFFFF, FormationButtonStyle.outlineColor(false, true));
        assertEquals(0xFFFFFFFF, FormationButtonStyle.outlineColor(true, true));
    }

    @Test
    void currentFormationUsesGoldWhenItIsNotFocused() {
        assertEquals(0xFFFFD25A, FormationButtonStyle.outlineColor(true, false));
    }

    @Test
    void idleUnselectedFormationHasNoOutline() {
        assertEquals(0, FormationButtonStyle.outlineColor(false, false));
    }
}
