package net.hfstack.rallyguard.api.profile;

public enum GuardRank {
    RECRUIT("rank.rallyguard.recruit"),
    GUARDSMAN("rank.rallyguard.guardsman"),
    SOLDIER("rank.rallyguard.soldier"),
    VETERAN("rank.rallyguard.veteran"),
    ELITE("rank.rallyguard.elite");

    private final String translationKey;

    GuardRank(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }
}
