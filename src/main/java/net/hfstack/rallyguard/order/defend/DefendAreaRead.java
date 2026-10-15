package net.hfstack.rallyguard.order.defend;

import java.util.Objects;
import java.util.Optional;

public sealed interface DefendAreaRead permits DefendAreaRead.Available, DefendAreaRead.Unsupported {
    record Available(Optional<DefendAreaState> state) implements DefendAreaRead {
        public Available {
            state = Objects.requireNonNull(state, "state");
        }
    }

    record Unsupported(int schemaVersion) implements DefendAreaRead {
    }
}
