package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.api.captain.CaptainAction;
import net.hfstack.rallyguard.api.captain.CaptainActionView;
import net.hfstack.rallyguard.api.captain.CaptainMutationResult;
import net.hfstack.rallyguard.api.captain.CaptainOffer;

import java.util.Objects;
import java.util.Optional;

public record CaptainActionScreenModel(
        boolean visible,
        boolean enabled,
        CaptainAction action,
        String labelKey,
        Optional<CaptainOffer> offer,
        Optional<String> blockingReasonKey
) {
    public CaptainActionScreenModel {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(labelKey, "labelKey");
        offer = offer == null ? Optional.empty() : offer;
        blockingReasonKey = blockingReasonKey == null ? Optional.empty() : blockingReasonKey;
    }

    public static CaptainActionScreenModel from(Optional<CaptainActionView> actionView) {
        if (actionView == null || actionView.isEmpty()) return hidden();
        CaptainActionView view = actionView.orElseThrow();
        if (view.action() == CaptainAction.NONE) return hidden();
        return new CaptainActionScreenModel(
                true,
                view.allowed(),
                view.action(),
                view.action() == CaptainAction.PROMOTE
                        ? "gui.rallyguard.captain.promote_button"
                        : "gui.rallyguard.captain.demote_button",
                view.offer(),
                view.blockingOutcome().map(CaptainActionScreenModel::feedbackKey)
        );
    }

    private static CaptainActionScreenModel hidden() {
        return new CaptainActionScreenModel(
                false, false, CaptainAction.NONE, "", Optional.empty(), Optional.empty()
        );
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
            case SUCCESS -> throw new IllegalArgumentException("SUCCESS is not a blocking outcome");
        };
    }
}
