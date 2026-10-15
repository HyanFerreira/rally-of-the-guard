package net.hfstack.rallyguard.formation;

import java.util.Optional;

public final class FormationSelectionPolicy {
    private FormationSelectionPolicy() {
    }

    public static Optional<Selection> resolve(
            int handId,
            String formationId,
            boolean mainHandHoldsScroll,
            boolean offHandHoldsScroll
    ) {
        if (handId != 0 && handId != 1) return Optional.empty();
        if (handId == 0 && !mainHandHoldsScroll) return Optional.empty();
        if (handId == 1 && !offHandHoldsScroll) return Optional.empty();
        return RallyFormation.findById(formationId).map(formation -> new Selection(handId, formation));
    }

    public record Selection(int handId, RallyFormation formation) {
    }
}
