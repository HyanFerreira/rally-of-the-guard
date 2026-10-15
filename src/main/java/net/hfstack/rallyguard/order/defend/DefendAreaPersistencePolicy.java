package net.hfstack.rallyguard.order.defend;

import java.util.Objects;

public final class DefendAreaPersistencePolicy {
    private DefendAreaPersistencePolicy() {
    }

    public static boolean canReplace(DefendAreaRead current) {
        return !(Objects.requireNonNull(current, "current") instanceof DefendAreaRead.Unsupported);
    }

    public static boolean canExplicitlyClear(DefendAreaRead current) {
        Objects.requireNonNull(current, "current");
        return true;
    }
}
