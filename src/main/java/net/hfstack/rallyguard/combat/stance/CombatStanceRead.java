package net.hfstack.rallyguard.combat.stance;

import java.util.Objects;

public sealed interface CombatStanceRead permits CombatStanceRead.Available, CombatStanceRead.Unsupported {
    record Available(CombatStanceState state) implements CombatStanceRead {
        public Available {
            state = Objects.requireNonNull(state, "state");
        }
    }

    record Unsupported(int schemaVersion) implements CombatStanceRead {
    }
}
