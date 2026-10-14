package net.hfstack.rallyguard.progression;

public enum XpCategory {
    COMMON(5),
    DANGEROUS(7),
    RAID(8),
    ELITE(10),
    RAID_ELITE(18),
    BOSS(30);

    private final int baseXp;

    XpCategory(int baseXp) {
        this.baseXp = baseXp;
    }

    public int baseXp() {
        return baseXp;
    }
}
