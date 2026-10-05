package net.hfstack.rallyguard.combat;

public final class RallyCombatPolicy {
    private RallyCombatPolicy() {
    }

    public enum Decision {
        FORM_UP,
        CONTINUE_COMBAT,
        DISENGAGE_AND_FORM_UP
    }

    public static Decision decide(
            boolean hasLiveTarget,
            boolean protectedTarget,
            boolean retreatEnabled,
            double commanderDistanceSquared,
            double retreatDistance
    ) {
        if (!hasLiveTarget) {
            return Decision.FORM_UP;
        }
        if (protectedTarget) {
            return Decision.DISENGAGE_AND_FORM_UP;
        }
        if (retreatEnabled && commanderDistanceSquared > retreatDistance * retreatDistance) {
            return Decision.DISENGAGE_AND_FORM_UP;
        }
        return Decision.CONTINUE_COMBAT;
    }
}
