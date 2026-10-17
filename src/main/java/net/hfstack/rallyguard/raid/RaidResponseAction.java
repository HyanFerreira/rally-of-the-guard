package net.hfstack.rallyguard.raid;

import java.util.Optional;

public enum RaidResponseAction {
    RALLY_GUARDS(0),
    DEFEND_VILLAGE(1),
    IGNORE(2);

    private final int wireId;

    RaidResponseAction(int wireId) {
        this.wireId = wireId;
    }

    public int wireId() {
        return wireId;
    }

    public static Optional<RaidResponseAction> fromWireId(int wireId) {
        for (RaidResponseAction action : values()) {
            if (action.wireId == wireId) return Optional.of(action);
        }
        return Optional.empty();
    }
}
