package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.api.captain.CaptainAction;
import net.hfstack.rallyguard.api.captain.CaptainActionView;
import net.hfstack.rallyguard.api.captain.CaptainMutationResult;
import net.hfstack.rallyguard.api.captain.CaptainOffer;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptainActionScreenModelTest {
    private static final CaptainOffer OFFER = new CaptainOffer(
            Identifier.fromNamespaceAndPath("minecraft", "emerald"), 4
    );

    @Test
    void hidesActionWhenServerDidNotExposeOwnedGuardState() {
        CaptainActionScreenModel model = CaptainActionScreenModel.from(Optional.empty());

        assertFalse(model.visible());
        assertEquals(CaptainAction.NONE, model.action());
    }

    @Test
    void enabledPromotionCarriesServerItemAndCost() {
        CaptainActionScreenModel model = model(
                CaptainAction.PROMOTE, true, Optional.empty(), Optional.of(OFFER)
        );

        assertTrue(model.visible());
        assertTrue(model.enabled());
        assertEquals("gui.rallyguard.captain.promote_button", model.labelKey());
        assertEquals(Optional.of(OFFER), model.offer());
        assertTrue(model.blockingReasonKey().isEmpty());
    }

    @Test
    void lowLevelPromotionIsDisabledWithExactReason() {
        CaptainActionScreenModel model = model(
                CaptainAction.PROMOTE,
                false,
                Optional.of(CaptainMutationResult.Outcome.LEVEL_TOO_LOW),
                Optional.of(OFFER)
        );

        assertFalse(model.enabled());
        assertEquals(Optional.of("gui.rallyguard.captain.level_too_low"), model.blockingReasonKey());
    }

    @Test
    void demotionIsEnabledWithoutAnOffer() {
        CaptainActionScreenModel model = model(
                CaptainAction.DEMOTE, true, Optional.empty(), Optional.empty()
        );

        assertTrue(model.enabled());
        assertEquals("gui.rallyguard.captain.demote_button", model.labelKey());
        assertTrue(model.offer().isEmpty());
    }

    @Test
    void officerDemotionIsDisabledWithExactReason() {
        CaptainActionScreenModel model = model(
                CaptainAction.DEMOTE,
                false,
                Optional.of(CaptainMutationResult.Outcome.ACTIVE_OFFICER),
                Optional.empty()
        );

        assertFalse(model.enabled());
        assertEquals(Optional.of("gui.rallyguard.captain.active_officer"), model.blockingReasonKey());
    }

    private static CaptainActionScreenModel model(
            CaptainAction action,
            boolean allowed,
            Optional<CaptainMutationResult.Outcome> blocking,
            Optional<CaptainOffer> offer
    ) {
        return CaptainActionScreenModel.from(Optional.of(new CaptainActionView(
                action == CaptainAction.DEMOTE,
                action,
                allowed,
                blocking,
                7,
                offer
        )));
    }
}
