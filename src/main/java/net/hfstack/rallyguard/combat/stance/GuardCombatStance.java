package net.hfstack.rallyguard.combat.stance;

import java.util.Arrays;
import java.util.Optional;

public enum GuardCombatStance {
    DEFENSIVE(0),
    AGGRESSIVE(1),
    PASSIVE(2),
    HOLD_POSITION(3),
    PROTECT_COMMANDER(4);

    private final int wireId;

    GuardCombatStance(int wireId) {
        this.wireId = wireId;
    }

    public int wireId() {
        return wireId;
    }

    public static Optional<GuardCombatStance> fromWireId(int wireId) {
        return Arrays.stream(values()).filter(stance -> stance.wireId == wireId).findFirst();
    }
}
