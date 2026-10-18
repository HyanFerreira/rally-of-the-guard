package net.hfstack.rallyguard.event;

public final class RallyCleanupPolicy {
    private RallyCleanupPolicy() {
    }

    public static boolean shouldRelease(boolean ownedByCommander, boolean rallied) {
        return ownedByCommander && rallied;
    }
}
