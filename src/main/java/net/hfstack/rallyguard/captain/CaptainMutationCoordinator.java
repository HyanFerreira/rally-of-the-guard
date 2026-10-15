package net.hfstack.rallyguard.captain;

import net.hfstack.rallyguard.api.captain.CaptainAction;
import net.hfstack.rallyguard.api.captain.CaptainMutationResult;
import net.hfstack.rallyguard.api.captain.CaptainOffer;
import net.hfstack.rallyguard.profile.GuardProfile;
import net.hfstack.rallyguard.profile.GuardProfilePersistencePolicy;
import net.hfstack.rallyguard.profile.GuardProfileReadResult;

import java.util.Objects;
import java.util.Optional;

final class CaptainMutationCoordinator {
    private CaptainMutationCoordinator() {
    }

    static CaptainMutationResult execute(Request request, PaymentPort payment, ProfileWriter profiles) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(payment, "payment");
        Objects.requireNonNull(profiles, "profiles");

        CaptainMutationResult.Outcome validation = validationOutcome(request);
        if (validation != CaptainMutationResult.Outcome.SUCCESS) {
            return rejected(request.action(), validation);
        }

        GuardProfile current = ((GuardProfileReadResult.Available) request.profile()).profile();
        if (request.action() == CaptainAction.PROMOTE) {
            CaptainOffer offer = request.offer().orElseThrow();
            if (!payment.canAfford(offer)) {
                return rejected(request.action(), CaptainMutationResult.Outcome.INSUFFICIENT_PAYMENT);
            }
            payment.remove(offer);
            try {
                if (!profiles.save(current.withCaptain(true))) {
                    payment.refund(offer);
                    return rejected(request.action(), CaptainMutationResult.Outcome.PERSISTENCE_ERROR);
                }
            } catch (RuntimeException exception) {
                payment.refund(offer);
                return rejected(request.action(), CaptainMutationResult.Outcome.PERSISTENCE_ERROR);
            }
            return CaptainMutationResult.success(
                    CaptainAction.PROMOTE,
                    offer,
                    "gui.rallyguard.captain.promoted"
            );
        }

        try {
            if (!profiles.save(current.withCaptain(false))) {
                return rejected(request.action(), CaptainMutationResult.Outcome.PERSISTENCE_ERROR);
            }
        } catch (RuntimeException exception) {
            return rejected(request.action(), CaptainMutationResult.Outcome.PERSISTENCE_ERROR);
        }
        return CaptainMutationResult.success(
                CaptainAction.DEMOTE,
                null,
                "gui.rallyguard.captain.demoted"
        );
    }

    static CaptainMutationResult.Outcome validationOutcome(Request request) {
        Objects.requireNonNull(request, "request");
        boolean hasProfile = request.profile() instanceof GuardProfileReadResult.Available;
        boolean writable = GuardProfilePersistencePolicy.canWrite(request.profile());
        GuardProfile profile = hasProfile
                ? ((GuardProfileReadResult.Available) request.profile()).profile()
                : GuardProfile.legacyNeutral();
        CaptainMutationResult.Outcome outcome = CaptainEligibilityPolicy.evaluate(
                request.action(),
                request.promotionEnabled(),
                request.available() && (hasProfile || request.profile() instanceof GuardProfileReadResult.FutureSchema),
                request.owner(),
                writable,
                profile.captain(),
                profile.level(),
                request.minimumLevel(),
                request.officer()
        );
        if (outcome == CaptainMutationResult.Outcome.SUCCESS
                && request.action() == CaptainAction.PROMOTE
                && request.offer().isEmpty()) {
            return CaptainMutationResult.Outcome.NOT_AVAILABLE;
        }
        return outcome;
    }

    static CaptainMutationResult rejected(CaptainAction action, CaptainMutationResult.Outcome outcome) {
        return CaptainMutationResult.rejected(action, outcome, feedbackKey(outcome));
    }

    private static String feedbackKey(CaptainMutationResult.Outcome outcome) {
        return switch (outcome) {
            case DISABLED -> "gui.rallyguard.captain.promotion_disabled";
            case NOT_AVAILABLE -> "gui.rallyguard.captain.unavailable";
            case NOT_OWNER -> "gui.rallyguard.captain.not_owner";
            case UNSUPPORTED_PROFILE -> "gui.rallyguard.captain.unsupported_profile";
            case ALREADY_CAPTAIN -> "gui.rallyguard.captain.already_captain";
            case NOT_CAPTAIN -> "gui.rallyguard.captain.not_captain";
            case LEVEL_TOO_LOW -> "gui.rallyguard.captain.level_too_low";
            case INSUFFICIENT_PAYMENT -> "gui.rallyguard.captain.insufficient_payment";
            case ACTIVE_OFFICER -> "gui.rallyguard.captain.active_officer";
            case PERSISTENCE_ERROR -> "gui.rallyguard.captain.persistence_error";
            case DENIED -> "gui.rallyguard.captain.denied";
            case SUCCESS -> throw new IllegalArgumentException("SUCCESS is not a rejection");
        };
    }

    record Request(
            CaptainAction action,
            boolean promotionEnabled,
            boolean available,
            boolean owner,
            GuardProfileReadResult profile,
            int minimumLevel,
            boolean officer,
            Optional<CaptainOffer> offer
    ) {
        Request {
            Objects.requireNonNull(action, "action");
            Objects.requireNonNull(profile, "profile");
            offer = offer == null ? Optional.empty() : offer;
        }
    }

    interface PaymentPort {
        boolean canAfford(CaptainOffer offer);

        void remove(CaptainOffer offer);

        void refund(CaptainOffer offer);
    }

    @FunctionalInterface
    interface ProfileWriter {
        boolean save(GuardProfile profile);
    }
}
