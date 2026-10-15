package net.hfstack.rallyguard.formation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormationSelectionPolicyTest {
    @Test
    void acceptsAValidFormationForTheHandHoldingTheScroll() {
        var selection = FormationSelectionPolicy.resolve(0, "wedge", true, false);

        assertTrue(selection.isPresent());
        assertEquals(0, selection.orElseThrow().handId());
        assertEquals(RallyFormation.WEDGE, selection.orElseThrow().formation());
    }

    @Test
    void rejectsUnknownHandsInsteadOfGuessing() {
        assertTrue(FormationSelectionPolicy.resolve(2, "line", true, true).isEmpty());
    }

    @Test
    void rejectsUnknownFormationsInsteadOfFallingBackToEscort() {
        assertTrue(FormationSelectionPolicy.resolve(0, "future_formation", true, false).isEmpty());
    }

    @Test
    void rejectsAHandThatNoLongerHoldsTheScroll() {
        assertTrue(FormationSelectionPolicy.resolve(1, "circle", true, false).isEmpty());
    }
}
