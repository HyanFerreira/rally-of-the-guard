package net.hfstack.rallyguard.api.profile;

/**
 * Immutable, read-only view of a guard's persisted profile data.
 */
public interface GuardProfileView {
    int schemaVersion();

    int generationVersion();

    String origin();

    long totalXpMilli();

    int naturalQuality();

    int healthRoll();

    int attackRoll();

    int armorRoll();

    int speedRoll();

    long kills();

    long assists();

    default boolean captain() {
        return false;
    }

    default int level() {
        return net.hfstack.rallyguard.profile.GuardLevelModel.level(totalXpMilli());
    }

    default GuardRank rank() {
        return net.hfstack.rallyguard.profile.GuardLevelModel.rank(level());
    }

    default int qualityStars() {
        return Math.min(5, naturalQuality() / 2_000 + 1);
    }

    default long xpIntoLevelMilli() {
        return net.hfstack.rallyguard.profile.GuardLevelModel.xpIntoLevelMilli(totalXpMilli());
    }

    default long xpForNextLevelMilli() {
        return net.hfstack.rallyguard.profile.GuardLevelModel.xpForNextLevelMilli(level());
    }
}
