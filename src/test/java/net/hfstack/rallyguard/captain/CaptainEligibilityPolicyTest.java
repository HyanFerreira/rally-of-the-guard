package net.hfstack.rallyguard.captain;

import net.hfstack.rallyguard.api.captain.CaptainAction;
import net.hfstack.rallyguard.api.captain.CaptainMutationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CaptainEligibilityPolicyTest {
    @Test
    void promotionReturnsFirstActionableFailureInStableOrder() {
        assertEquals(CaptainMutationResult.Outcome.DISABLED,
                evaluate(CaptainAction.PROMOTE, false, false, false, false, true, 1, true));
        assertEquals(CaptainMutationResult.Outcome.NOT_AVAILABLE,
                evaluate(CaptainAction.PROMOTE, true, false, false, false, true, 1, true));
        assertEquals(CaptainMutationResult.Outcome.NOT_OWNER,
                evaluate(CaptainAction.PROMOTE, true, true, false, false, true, 1, true));
        assertEquals(CaptainMutationResult.Outcome.UNSUPPORTED_PROFILE,
                evaluate(CaptainAction.PROMOTE, true, true, true, false, true, 1, true));
        assertEquals(CaptainMutationResult.Outcome.ALREADY_CAPTAIN,
                evaluate(CaptainAction.PROMOTE, true, true, true, true, true, 1, true));
        assertEquals(CaptainMutationResult.Outcome.LEVEL_TOO_LOW,
                evaluate(CaptainAction.PROMOTE, true, true, true, true, false, 6, false));
    }

    @Test
    void promotionAcceptsConfiguredBoundaryAndVeteransAboveIt() {
        assertEquals(CaptainMutationResult.Outcome.SUCCESS,
                evaluate(CaptainAction.PROMOTE, true, true, true, true, false, 7, false));
        assertEquals(CaptainMutationResult.Outcome.SUCCESS,
                evaluate(CaptainAction.PROMOTE, true, true, true, true, false, 10, false));
    }

    @Test
    void demotionIgnoresPromotionToggleAndMinimumLevelButProtectsOfficer() {
        assertEquals(CaptainMutationResult.Outcome.SUCCESS,
                evaluate(CaptainAction.DEMOTE, false, true, true, true, true, 1, false));
        assertEquals(CaptainMutationResult.Outcome.ACTIVE_OFFICER,
                evaluate(CaptainAction.DEMOTE, false, true, true, true, true, 10, true));
        assertEquals(CaptainMutationResult.Outcome.NOT_CAPTAIN,
                evaluate(CaptainAction.DEMOTE, false, true, true, true, false, 10, false));
    }

    @Test
    void noneIsNeverAnEligibleMutation() {
        assertEquals(CaptainMutationResult.Outcome.NOT_AVAILABLE,
                evaluate(CaptainAction.NONE, true, true, true, true, false, 10, false));
    }

    private static CaptainMutationResult.Outcome evaluate(
            CaptainAction action,
            boolean enabled,
            boolean available,
            boolean owner,
            boolean writable,
            boolean captain,
            int level,
            boolean officer
    ) {
        return CaptainEligibilityPolicy.evaluate(
                action, enabled, available, owner, writable, captain, level, 7, officer
        );
    }
}
