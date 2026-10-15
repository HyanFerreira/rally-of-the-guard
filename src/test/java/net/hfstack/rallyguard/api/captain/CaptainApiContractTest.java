package net.hfstack.rallyguard.api.captain;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptainApiContractTest {
    private static final Identifier EMERALD = Identifier.fromNamespaceAndPath("minecraft", "emerald");

    @Test
    void serviceExposesInspectionPromotionAndDemotion() throws NoSuchMethodException {
        assertEquals(CaptainActionView.class,
                CaptainService.class.getMethod("inspect", ServerPlayer.class, GuardEntity.class).getReturnType());
        assertEquals(CaptainMutationResult.class,
                CaptainService.class.getMethod("promote", ServerPlayer.class, GuardEntity.class).getReturnType());
        assertEquals(CaptainMutationResult.class,
                CaptainService.class.getMethod("demote", ServerPlayer.class, GuardEntity.class).getReturnType());
    }

    @Test
    void offerAcceptsFreePromotionButRejectsInvalidValues() {
        assertDoesNotThrow(() -> new CaptainOffer(EMERALD, 0));
        assertThrows(NullPointerException.class, () -> new CaptainOffer(null, 4));
        assertThrows(IllegalArgumentException.class, () -> new CaptainOffer(EMERALD, -1));
    }

    @Test
    void actionViewNormalizesOptionalFieldsAndMinimumLevel() {
        CaptainActionView view = new CaptainActionView(false, CaptainAction.PROMOTE, true, null, 99, null);

        assertEquals(Optional.empty(), view.blockingOutcome());
        assertEquals(Optional.empty(), view.offer());
        assertEquals(10, view.minimumLevel());
    }

    @Test
    void promotionSuccessRequiresAnAppliedOffer() {
        CaptainOffer offer = new CaptainOffer(EMERALD, 4);
        CaptainMutationResult success = CaptainMutationResult.success(CaptainAction.PROMOTE, offer,
                "gui.rallyguard.captain.promoted");

        assertTrue(success.succeeded());
        assertEquals(CaptainAction.PROMOTE, success.action());
        assertEquals(Optional.of(offer), success.offer());
        assertThrows(NullPointerException.class, () -> CaptainMutationResult.success(
                CaptainAction.PROMOTE, null, "gui.rallyguard.captain.promoted"));
    }

    @Test
    void demotionSuccessHasNoOfferAndFailureCannotUseSuccessOutcome() {
        CaptainMutationResult success = CaptainMutationResult.success(CaptainAction.DEMOTE, null,
                "gui.rallyguard.captain.demoted");
        CaptainMutationResult failure = CaptainMutationResult.rejected(
                CaptainAction.PROMOTE,
                CaptainMutationResult.Outcome.LEVEL_TOO_LOW,
                "gui.rallyguard.captain.level_too_low"
        );

        assertTrue(success.succeeded());
        assertEquals(Optional.empty(), success.offer());
        assertFalse(failure.succeeded());
        assertThrows(IllegalArgumentException.class, () -> CaptainMutationResult.rejected(
                CaptainAction.PROMOTE,
                CaptainMutationResult.Outcome.SUCCESS,
                "gui.rallyguard.captain.promoted"
        ));
    }
}
