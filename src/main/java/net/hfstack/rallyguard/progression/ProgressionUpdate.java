package net.hfstack.rallyguard.progression;

record ProgressionUpdate(
        boolean saved,
        int previousLevel,
        int currentLevel,
        long awardedXpMilli,
        boolean killRecorded,
        boolean assistRecorded
) {
    public static ProgressionUpdate unavailable() {
        return new ProgressionUpdate(false, 0, 0, 0, false, false);
    }
}
