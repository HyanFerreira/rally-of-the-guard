package net.hfstack.rallyguard.captain;

import net.hfstack.rallyguard.api.captain.CaptainAction;
import net.hfstack.rallyguard.api.captain.CaptainMutationResult;
import net.hfstack.rallyguard.api.captain.CaptainOffer;
import net.hfstack.rallyguard.profile.GuardLevelModel;
import net.hfstack.rallyguard.profile.GuardProfile;
import net.hfstack.rallyguard.profile.GuardProfileOrigin;
import net.hfstack.rallyguard.profile.GuardProfileReadResult;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptainMutationCoordinatorTest {
    private static final CaptainOffer OFFER = new CaptainOffer(
            Identifier.fromNamespaceAndPath("minecraft", "emerald"), 4
    );

    @Test
    void validatesBeforeConsultingOrRemovingPayment() {
        RecordingPayment payment = new RecordingPayment(true);

        CaptainMutationResult result = CaptainMutationCoordinator.execute(
                request(CaptainAction.PROMOTE, veteran(false), true, false, false, true),
                payment,
                profile -> true
        );

        assertEquals(CaptainMutationResult.Outcome.NOT_OWNER, result.outcome());
        assertTrue(payment.calls.isEmpty());
    }

    @Test
    void promotionRemovesPaymentThenPersistsCaptainProfile() {
        List<String> calls = new ArrayList<>();
        RecordingPayment payment = new RecordingPayment(true, calls);
        List<GuardProfile> saved = new ArrayList<>();

        CaptainMutationResult result = CaptainMutationCoordinator.execute(
                request(CaptainAction.PROMOTE, veteran(false), true, true, false, true),
                payment,
                profile -> {
                    calls.add("save");
                    saved.add(profile);
                    return true;
                }
        );

        assertTrue(result.succeeded());
        assertEquals(List.of("canAfford", "remove", "save"), calls);
        assertTrue(saved.getFirst().captain());
        assertEquals(OFFER, result.offer().orElseThrow());
    }

    @Test
    void promotionRefundsFullOfferWhenPersistenceFails() {
        List<String> calls = new ArrayList<>();
        RecordingPayment payment = new RecordingPayment(true, calls);

        CaptainMutationResult result = CaptainMutationCoordinator.execute(
                request(CaptainAction.PROMOTE, veteran(false), true, true, false, true),
                payment,
                profile -> {
                    calls.add("save");
                    return false;
                }
        );

        assertEquals(CaptainMutationResult.Outcome.PERSISTENCE_ERROR, result.outcome());
        assertEquals(List.of("canAfford", "remove", "save", "refund"), calls);
        assertEquals(OFFER, payment.refunded);
    }

    @Test
    void insufficientPaymentDoesNotRemoveOrPersist() {
        RecordingPayment payment = new RecordingPayment(false);
        List<GuardProfile> saved = new ArrayList<>();

        CaptainMutationResult result = CaptainMutationCoordinator.execute(
                request(CaptainAction.PROMOTE, veteran(false), true, true, false, true),
                payment,
                profile -> {
                    saved.add(profile);
                    return true;
                }
        );

        assertEquals(CaptainMutationResult.Outcome.INSUFFICIENT_PAYMENT, result.outcome());
        assertEquals(List.of("canAfford"), payment.calls);
        assertTrue(saved.isEmpty());
    }

    @Test
    void repeatPromotionIsRejectedWithoutPayment() {
        RecordingPayment payment = new RecordingPayment(true);

        CaptainMutationResult result = CaptainMutationCoordinator.execute(
                request(CaptainAction.PROMOTE, veteran(true), true, true, false, true),
                payment,
                profile -> true
        );

        assertEquals(CaptainMutationResult.Outcome.ALREADY_CAPTAIN, result.outcome());
        assertTrue(payment.calls.isEmpty());
    }

    @Test
    void futureProfileIsPreservedWithoutPaymentOrSave() {
        RecordingPayment payment = new RecordingPayment(true);
        List<GuardProfile> saved = new ArrayList<>();
        CaptainMutationCoordinator.Request request = new CaptainMutationCoordinator.Request(
                CaptainAction.PROMOTE,
                true,
                true,
                true,
                new GuardProfileReadResult.FutureSchema(GuardProfile.CURRENT_SCHEMA_VERSION + 1),
                7,
                false,
                Optional.of(OFFER)
        );

        CaptainMutationResult result = CaptainMutationCoordinator.execute(
                request,
                payment,
                profile -> {
                    saved.add(profile);
                    return true;
                }
        );

        assertEquals(CaptainMutationResult.Outcome.UNSUPPORTED_PROFILE, result.outcome());
        assertTrue(payment.calls.isEmpty());
        assertTrue(saved.isEmpty());
    }

    @Test
    void demotionNeedsNoPaymentOrRefundAndWorksWhenPromotionIsDisabled() {
        RecordingPayment payment = new RecordingPayment(true);
        List<GuardProfile> saved = new ArrayList<>();

        CaptainMutationResult result = CaptainMutationCoordinator.execute(
                request(CaptainAction.DEMOTE, veteran(true), false, true, false, false),
                payment,
                profile -> {
                    saved.add(profile);
                    return true;
                }
        );

        assertTrue(result.succeeded());
        assertFalse(saved.getFirst().captain());
        assertTrue(payment.calls.isEmpty());
        assertEquals(Optional.empty(), result.offer());
    }

    @Test
    void activeOfficerCannotBeDemotedEvenWhenPromotionIsDisabled() {
        RecordingPayment payment = new RecordingPayment(true);

        CaptainMutationResult result = CaptainMutationCoordinator.execute(
                request(CaptainAction.DEMOTE, veteran(true), false, true, true, false),
                payment,
                profile -> true
        );

        assertEquals(CaptainMutationResult.Outcome.ACTIVE_OFFICER, result.outcome());
        assertTrue(payment.calls.isEmpty());
    }

    private static CaptainMutationCoordinator.Request request(
            CaptainAction action,
            GuardProfile profile,
            boolean promotionEnabled,
            boolean owner,
            boolean officer,
            boolean includeOffer
    ) {
        return new CaptainMutationCoordinator.Request(
                action,
                promotionEnabled,
                true,
                owner,
                new GuardProfileReadResult.Available(profile, false),
                7,
                officer,
                includeOffer ? Optional.of(OFFER) : Optional.empty()
        );
    }

    private static GuardProfile veteran(boolean captain) {
        return GuardProfile.createV2(
                GuardProfileOrigin.LEGACY,
                GuardLevelModel.cumulativeXpMilli(7),
                5_000,
                0,
                0,
                0,
                0,
                0,
                0,
                captain
        );
    }

    private static final class RecordingPayment implements CaptainMutationCoordinator.PaymentPort {
        private final boolean affordable;
        private final List<String> calls;
        private CaptainOffer refunded;

        private RecordingPayment(boolean affordable) {
            this(affordable, new ArrayList<>());
        }

        private RecordingPayment(boolean affordable, List<String> calls) {
            this.affordable = affordable;
            this.calls = calls;
        }

        @Override
        public boolean canAfford(CaptainOffer offer) {
            calls.add("canAfford");
            return affordable;
        }

        @Override
        public void remove(CaptainOffer offer) {
            calls.add("remove");
        }

        @Override
        public void refund(CaptainOffer offer) {
            calls.add("refund");
            refunded = offer;
        }
    }
}
