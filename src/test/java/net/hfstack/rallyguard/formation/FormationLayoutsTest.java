package net.hfstack.rallyguard.formation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormationLayoutsTest {
    private static final double COLUMN = 2.0;
    private static final double ROW = 3.0;

    @Test
    void escortPreservesTwoColumnRows() {
        assertOffset(RallyFormation.ESCORT, 0, 4, -2.0, 1.8);
        assertOffset(RallyFormation.ESCORT, 1, 4, 2.0, 1.8);
        assertOffset(RallyFormation.ESCORT, 2, 4, -2.0, 4.8);
        assertOffset(RallyFormation.ESCORT, 3, 4, 2.0, 4.8);
    }

    @Test
    void columnUsesOneCenteredSlotPerRow() {
        assertOffset(RallyFormation.COLUMN, 0, 3, 0.0, 1.8);
        assertOffset(RallyFormation.COLUMN, 2, 3, 0.0, 7.8);
    }

    @Test
    void lineIsCenteredForOddAndEvenGroups() {
        assertOffset(RallyFormation.LINE, 0, 3, -2.0, 1.8);
        assertOffset(RallyFormation.LINE, 1, 3, 0.0, 1.8);
        assertOffset(RallyFormation.LINE, 0, 4, -3.0, 1.8);
        assertOffset(RallyFormation.LINE, 3, 4, 3.0, 1.8);
    }

    @Test
    void wedgeWidensInPairedRows() {
        assertOffset(RallyFormation.WEDGE, 0, 5, 0.0, 1.8);
        assertOffset(RallyFormation.WEDGE, 1, 5, -2.0, 4.8);
        assertOffset(RallyFormation.WEDGE, 2, 5, 2.0, 4.8);
        assertOffset(RallyFormation.WEDGE, 4, 5, 4.0, 7.8);
    }

    @Test
    void squareCentersEachPopulatedRow() {
        assertOffset(RallyFormation.SQUARE, 0, 5, -2.0, 1.8);
        assertOffset(RallyFormation.SQUARE, 2, 5, 2.0, 1.8);
        assertOffset(RallyFormation.SQUARE, 3, 5, -2.0, 4.8);
        assertOffset(RallyFormation.SQUARE, 4, 5, 2.0, 4.8);
    }

    @Test
    void nineGuardsProduceARegularThreeByThreeSquare() {
        for (int index = 0; index < 9; index++) {
            FormationOffset offset = FormationLayouts.slot(
                    RallyFormation.SQUARE,
                    index,
                    9,
                    COLUMN,
                    ROW
            );
            assertEquals((index % 3 - 1) * COLUMN, offset.right(), 1.0e-9);
            assertEquals(1.8 + (index / 3) * ROW, offset.forward(), 1.0e-9);
        }
    }

    @Test
    void circleDistributesSlotsAroundCommander() {
        FormationOffset first = FormationLayouts.slot(RallyFormation.CIRCLE, 0, 4, COLUMN, ROW);
        FormationOffset second = FormationLayouts.slot(RallyFormation.CIRCLE, 1, 4, COLUMN, ROW);
        FormationOffset third = FormationLayouts.slot(RallyFormation.CIRCLE, 2, 4, COLUMN, ROW);

        assertEquals(0.0, first.right(), 1.0e-9);
        assertTrue(first.forward() > 0.0);
        assertTrue(second.right() > 0.0);
        assertEquals(0.0, second.forward(), 1.0e-9);
        assertEquals(-first.forward(), third.forward(), 1.0e-9);
    }

    @Test
    void circleKeepsAProtectiveMinimumRadius() {
        FormationOffset offset = FormationLayouts.slot(
                RallyFormation.CIRCLE,
                0,
                8,
                1.35,
                1.8,
                3.25
        );

        assertEquals(3.25, offset.forward(), 1.0e-9);
    }

    @Test
    void shieldWallIsTighterThanLine() {
        FormationOffset line = FormationLayouts.slot(RallyFormation.LINE, 0, 4, COLUMN, ROW);
        FormationOffset wall = FormationLayouts.slot(RallyFormation.SHIELD_WALL, 0, 4, COLUMN, ROW);

        assertTrue(Math.abs(wall.right()) < Math.abs(line.right()));
        assertTrue(wall.forward() > line.forward());
    }

    @Test
    void everyLayoutProducesFiniteCoordinatesForSingleAndLargeGroups() {
        for (RallyFormation formation : RallyFormation.values()) {
            for (int total : new int[]{1, 32}) {
                for (int index = 0; index < total; index++) {
                    FormationOffset offset = FormationLayouts.slot(formation, index, total, COLUMN, ROW);
                    assertTrue(Double.isFinite(offset.right()));
                    assertTrue(Double.isFinite(offset.forward()));
                }
            }
        }
    }

    private static void assertOffset(
            RallyFormation formation,
            int index,
            int total,
            double expectedRight,
            double expectedForward
    ) {
        FormationOffset offset = FormationLayouts.slot(formation, index, total, COLUMN, ROW);
        assertEquals(expectedRight, offset.right(), 1.0e-9);
        assertEquals(expectedForward, offset.forward(), 1.0e-9);
    }
}
