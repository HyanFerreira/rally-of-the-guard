package net.hfstack.rallyguard.profile;

import java.util.Objects;

public final class GuardProfileInitializationPolicy {
    private GuardProfileInitializationPolicy() {
    }

    public static boolean shouldInitialize(boolean guardEntity, GuardProfileReadResult current) {
        Objects.requireNonNull(current, "current");
        return guardEntity && current instanceof GuardProfileReadResult.Absent;
    }
}
