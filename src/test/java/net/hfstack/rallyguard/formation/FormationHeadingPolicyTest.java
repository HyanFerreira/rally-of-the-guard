package net.hfstack.rallyguard.formation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormationHeadingPolicyTest {
    private static final double MARGIN = 1.25;

    @Test
    void smallLateralDriftDoesNotRotateFormation() {
        FormationHeadingState state = FormationHeadingState.initial(0.0F);

        for (int i = 0; i < 5; i++) {
            state = FormationHeadingPolicy.update(state, 0.2, 1.0, MARGIN);
        }

        assertEquals(0.0F, state.yawDegrees(), 1.0e-6F);
        assertFalse(state.adapting());
    }

    @Test
    void oppositeSmallCorrectionsCancelInsideMargin() {
        FormationHeadingState state = FormationHeadingState.initial(0.0F);
        state = FormationHeadingPolicy.update(state, 0.8, 3.0, MARGIN);
        state = FormationHeadingPolicy.update(state, -0.8, 3.0, MARGIN);

        assertEquals(0.0F, state.yawDegrees(), 1.0e-6F);
        assertEquals(0.0, state.lateralDrift(), 1.0e-9);
    }

    @Test
    void crossingMarginStartsAGradualTurnInsteadOfSnapping() {
        FormationHeadingState state = FormationHeadingState.initial(0.0F);
        state = FormationHeadingPolicy.update(state, 1.3, 0.0, MARGIN);

        assertTrue(state.adapting());
        assertEquals(-10.0F, state.yawDegrees(), 1.0e-6F);
    }

    @Test
    void decisiveDirectionChangeDoesNotWaitForLateralMargin() {
        FormationHeadingState state = FormationHeadingPolicy.update(
                FormationHeadingState.initial(0.0F),
                0.1,
                0.0,
                MARGIN
        );

        assertTrue(state.adapting());
        assertEquals(-10.0F, state.yawDegrees(), 1.0e-6F);
    }

    @Test
    void sustainedDirectionChangeEventuallyAlignsFormation() {
        FormationHeadingState state = FormationHeadingState.initial(0.0F);
        for (int i = 0; i < 10; i++) {
            state = FormationHeadingPolicy.update(state, 0.2, 0.0, MARGIN);
        }

        assertEquals(-90.0F, state.yawDegrees(), 1.0e-6F);
        assertFalse(state.adapting());
    }

    @Test
    void stoppingClearsPendingDrift() {
        FormationHeadingState state = FormationHeadingPolicy.update(
                FormationHeadingState.initial(0.0F),
                0.8,
                3.0,
                MARGIN
        );

        state = FormationHeadingPolicy.update(state, 0.0, 0.0, MARGIN);

        assertEquals(0.0, state.lateralDrift(), 1.0e-9);
        assertFalse(state.adapting());
    }
}
