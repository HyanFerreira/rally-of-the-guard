package net.hfstack.rallyguard.order.move;

import java.util.Objects;

public final class MoveOrderPersistencePolicy {
    private MoveOrderPersistencePolicy() {
    }

    public static boolean canReplace(MoveOrderRead current) {
        return !(Objects.requireNonNull(current, "current") instanceof MoveOrderRead.Unsupported);
    }

    public static boolean canExplicitlyClear(MoveOrderRead current) {
        Objects.requireNonNull(current, "current");
        return true;
    }
}
