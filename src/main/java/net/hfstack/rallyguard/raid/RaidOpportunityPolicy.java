package net.hfstack.rallyguard.raid;

public final class RaidOpportunityPolicy {
    public static final double MAX_DISTANCE = 128.0;

    private RaidOpportunityPolicy() {
    }

    public static boolean eligible(boolean raidActive, boolean spectator, double distanceSquared,
                                   int loadedOwnedGuards, boolean alreadySeen) {
        return raidActive
                && !spectator
                && Double.isFinite(distanceSquared)
                && distanceSquared <= MAX_DISTANCE * MAX_DISTANCE
                && loadedOwnedGuards > 0
                && !alreadySeen;
    }
}
