package net.hfstack.rallyguard.profile;

import java.util.Objects;

public final class GuardProfilePersistencePolicy {
    private GuardProfilePersistencePolicy() {
    }

    public static boolean canWrite(GuardProfileReadResult current) {
        Objects.requireNonNull(current, "current");
        return !(current instanceof GuardProfileReadResult.FutureSchema);
    }

    public static boolean shouldCanonicalize(GuardProfileReadResult current) {
        Objects.requireNonNull(current, "current");
        return current instanceof GuardProfileReadResult.Available available && available.repaired();
    }
}
