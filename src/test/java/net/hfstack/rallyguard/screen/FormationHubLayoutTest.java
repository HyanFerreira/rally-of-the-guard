package net.hfstack.rallyguard.screen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormationHubLayoutTest {
    @Test
    void keepsAllSevenFormationsOnOneRowWhenSpaceAllows() {
        FormationHubLayout.Grid grid = FormationHubLayout.forScreen(320, 7);

        assertEquals(7, grid.columns());
        assertEquals(1, grid.rows());
    }

    @Test
    void wrapsTheFormationStripOnNarrowGuiScales() {
        FormationHubLayout.Grid grid = FormationHubLayout.forScreen(180, 7);

        assertEquals(4, grid.columns());
        assertEquals(2, grid.rows());
    }

    @Test
    void keepsAtLeastOneColumnOnExtremelyNarrowScreens() {
        FormationHubLayout.Grid grid = FormationHubLayout.forScreen(20, 7);

        assertEquals(1, grid.columns());
        assertEquals(7, grid.rows());
    }
}
