package net.hfstack.rallyguard.api.captain;

import java.util.Objects;
import java.util.Optional;

public record CaptainActionView(
        boolean captain,
        CaptainAction action,
        boolean allowed,
        Optional<CaptainMutationResult.Outcome> blockingOutcome,
        int minimumLevel,
        Optional<CaptainOffer> offer
) {
    public CaptainActionView {
        Objects.requireNonNull(action, "action");
        blockingOutcome = blockingOutcome == null ? Optional.empty() : blockingOutcome;
        minimumLevel = Math.max(1, Math.min(10, minimumLevel));
        offer = offer == null ? Optional.empty() : offer;
    }
}
