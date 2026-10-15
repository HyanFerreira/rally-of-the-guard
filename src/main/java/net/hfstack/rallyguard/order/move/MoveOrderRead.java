package net.hfstack.rallyguard.order.move;

import java.util.Objects;
import java.util.Optional;

public sealed interface MoveOrderRead permits MoveOrderRead.Available, MoveOrderRead.Unsupported {
    record Available(Optional<MoveOrderState> state) implements MoveOrderRead {
        public Available {
            state = Objects.requireNonNull(state, "state");
        }
    }

    record Unsupported(int schemaVersion) implements MoveOrderRead {
    }
}
