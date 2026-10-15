package net.hfstack.rallyguard.api.command;

import java.util.Arrays;
import java.util.Optional;

public enum GuardUnitCategory {
    ALL(0),
    INFANTRY(1),
    RANGED(2);

    private final int wireId;

    GuardUnitCategory(int wireId) {
        this.wireId = wireId;
    }

    public int wireId() {
        return wireId;
    }

    public static Optional<GuardUnitCategory> fromWireId(int wireId) {
        return Arrays.stream(values()).filter(value -> value.wireId == wireId).findFirst();
    }
}
