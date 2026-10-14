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
}
