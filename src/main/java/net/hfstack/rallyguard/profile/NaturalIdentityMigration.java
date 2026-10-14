package net.hfstack.rallyguard.profile;

public final class NaturalIdentityMigration {
    private NaturalIdentityMigration() {
    }

    public static boolean isProfileCorePlaceholder(GuardProfile profile) {
        return profile.generationVersion() == GuardProfile.PROFILE_CORE_PLACEHOLDER_GENERATION_VERSION
                && profile.originType() == GuardProfileOrigin.LEGACY
                && profile.totalXpMilli() == 0
                && profile.naturalQuality() == 5_000
                && profile.healthRoll() == 0
                && profile.attackRoll() == 0
                && profile.armorRoll() == 0
                && profile.speedRoll() == 0
                && profile.kills() == 0
                && profile.assists() == 0;
    }
}
