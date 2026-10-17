package net.hfstack.rallyguard.raid;

import java.util.Optional;

public enum RaidBehavior {
    NORMAL(0, "gui.rallyguard.raid.behavior.normal"),
    RALLY_TO_VILLAGE(1, "gui.rallyguard.raid.behavior.village"),
    RALLY_TO_COMMANDER(2, "gui.rallyguard.raid.behavior.commander");

    private final int wireId;
    private final String translationKey;

    RaidBehavior(int wireId, String translationKey) {
        this.wireId = wireId;
        this.translationKey = translationKey;
    }

    public int wireId() {
        return wireId;
    }

    public String translationKey() {
        return translationKey;
    }

    public Optional<RaidResponseAction> automaticAction() {
        return switch (this) {
            case NORMAL -> Optional.empty();
            case RALLY_TO_VILLAGE -> Optional.of(RaidResponseAction.DEFEND_VILLAGE);
            case RALLY_TO_COMMANDER -> Optional.of(RaidResponseAction.RALLY_GUARDS);
        };
    }

    public static Optional<RaidBehavior> fromWireId(int wireId) {
        for (RaidBehavior behavior : values()) {
            if (behavior.wireId == wireId) return Optional.of(behavior);
        }
        return Optional.empty();
    }
}
