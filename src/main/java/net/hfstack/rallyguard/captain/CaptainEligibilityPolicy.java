package net.hfstack.rallyguard.captain;

import net.hfstack.rallyguard.api.captain.CaptainAction;
import net.hfstack.rallyguard.api.captain.CaptainMutationResult;

import java.util.Objects;

public final class CaptainEligibilityPolicy {
    private CaptainEligibilityPolicy() {
    }

    public static CaptainMutationResult.Outcome evaluate(
            CaptainAction action,
            boolean promotionEnabled,
            boolean available,
            boolean owner,
            boolean writableProfile,
            boolean captain,
            int level,
            int minimumLevel,
            boolean officer
    ) {
        Objects.requireNonNull(action, "action");
        if (action == CaptainAction.NONE) return CaptainMutationResult.Outcome.NOT_AVAILABLE;
        if (action == CaptainAction.PROMOTE && !promotionEnabled) return CaptainMutationResult.Outcome.DISABLED;
        if (!available) return CaptainMutationResult.Outcome.NOT_AVAILABLE;
        if (!owner) return CaptainMutationResult.Outcome.NOT_OWNER;
        if (!writableProfile) return CaptainMutationResult.Outcome.UNSUPPORTED_PROFILE;

        if (action == CaptainAction.PROMOTE) {
            if (captain) return CaptainMutationResult.Outcome.ALREADY_CAPTAIN;
            int requiredLevel = Math.max(1, Math.min(10, minimumLevel));
            return level < requiredLevel
                    ? CaptainMutationResult.Outcome.LEVEL_TOO_LOW
                    : CaptainMutationResult.Outcome.SUCCESS;
        }

        if (!captain) return CaptainMutationResult.Outcome.NOT_CAPTAIN;
        return officer
                ? CaptainMutationResult.Outcome.ACTIVE_OFFICER
                : CaptainMutationResult.Outcome.SUCCESS;
    }
}
