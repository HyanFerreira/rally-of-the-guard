package net.hfstack.rallyguard.formation;

import java.util.Arrays;
import java.util.Optional;

public enum RallyFormation {
    ESCORT("escort"),
    COLUMN("column"),
    LINE("line"),
    WEDGE("wedge"),
    SQUARE("square"),
    CIRCLE("circle"),
    SHIELD_WALL("shield_wall");

    private final String id;

    RallyFormation(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return "formation.rallyguard." + id;
    }

    public RallyFormation next() {
        RallyFormation[] formations = values();
        return formations[(ordinal() + 1) % formations.length];
    }

    public static RallyFormation fromId(String id) {
        return findById(id).orElse(ESCORT);
    }

    public static Optional<RallyFormation> findById(String id) {
        if (id == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(formation -> formation.id.equals(id))
                .findFirst();
    }
}
