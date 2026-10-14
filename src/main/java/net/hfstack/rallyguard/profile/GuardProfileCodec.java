package net.hfstack.rallyguard.profile;

import net.minecraft.nbt.CompoundTag;

import java.util.Objects;
import java.util.Optional;

public final class GuardProfileCodec {
    private static final String SCHEMA_VERSION = "schema_version";
    private static final String GENERATION_VERSION = "generation_version";
    private static final String ORIGIN = "origin";
    private static final String TOTAL_XP_MILLI = "total_xp_milli";
    private static final String NATURAL_QUALITY = "natural_quality";
    private static final String HEALTH_ROLL = "health_roll";
    private static final String ATTACK_ROLL = "attack_roll";
    private static final String ARMOR_ROLL = "armor_roll";
    private static final String SPEED_ROLL = "speed_roll";
    private static final String KILLS = "kills";
    private static final String ASSISTS = "assists";

    private GuardProfileCodec() {
    }

    public static CompoundTag encode(GuardProfile profile) {
        Objects.requireNonNull(profile, "profile");

        CompoundTag tag = new CompoundTag();
        tag.putInt(SCHEMA_VERSION, profile.schemaVersion());
        tag.putInt(GENERATION_VERSION, profile.generationVersion());
        tag.putString(ORIGIN, profile.origin());
        tag.putLong(TOTAL_XP_MILLI, profile.totalXpMilli());
        tag.putInt(NATURAL_QUALITY, profile.naturalQuality());
        tag.putInt(HEALTH_ROLL, profile.healthRoll());
        tag.putInt(ATTACK_ROLL, profile.attackRoll());
        tag.putInt(ARMOR_ROLL, profile.armorRoll());
        tag.putInt(SPEED_ROLL, profile.speedRoll());
        tag.putLong(KILLS, profile.kills());
        tag.putLong(ASSISTS, profile.assists());
        return tag;
    }

    public static GuardProfileReadResult decode(CompoundTag tag) {
        if (tag == null) return new GuardProfileReadResult.Absent();

        RepairTracker repairs = new RepairTracker();
        int schemaVersion = readInt(tag, SCHEMA_VERSION, GuardProfile.CURRENT_SCHEMA_VERSION, repairs);
        if (schemaVersion > GuardProfile.CURRENT_SCHEMA_VERSION) {
            return new GuardProfileReadResult.FutureSchema(schemaVersion);
        }
        if (schemaVersion != GuardProfile.CURRENT_SCHEMA_VERSION) {
            repairs.mark();
        }

        int generationVersion = readInt(
                tag,
                GENERATION_VERSION,
                GuardProfile.CURRENT_GENERATION_VERSION,
                repairs
        );
        if (generationVersion != GuardProfile.CURRENT_GENERATION_VERSION) {
            repairs.mark();
        }

        GuardProfileOrigin origin = readOrigin(tag, repairs);
        long totalXpMilli = readLong(tag, TOTAL_XP_MILLI, 0, repairs);
        int naturalQuality = readInt(tag, NATURAL_QUALITY, 5_000, repairs);
        int healthRoll = readInt(tag, HEALTH_ROLL, 0, repairs);
        int attackRoll = readInt(tag, ATTACK_ROLL, 0, repairs);
        int armorRoll = readInt(tag, ARMOR_ROLL, 0, repairs);
        int speedRoll = readInt(tag, SPEED_ROLL, 0, repairs);
        long kills = readLong(tag, KILLS, 0, repairs);
        long assists = readLong(tag, ASSISTS, 0, repairs);

        GuardProfile profile = GuardProfile.createV1(
                origin,
                totalXpMilli,
                naturalQuality,
                healthRoll,
                attackRoll,
                armorRoll,
                speedRoll,
                kills,
                assists
        );
        if (profile.totalXpMilli() != totalXpMilli
                || profile.naturalQuality() != naturalQuality
                || profile.healthRoll() != healthRoll
                || profile.attackRoll() != attackRoll
                || profile.armorRoll() != armorRoll
                || profile.speedRoll() != speedRoll
                || profile.kills() != kills
                || profile.assists() != assists) {
            repairs.mark();
        }

        return new GuardProfileReadResult.Available(profile, repairs.repaired());
    }

    private static GuardProfileOrigin readOrigin(CompoundTag tag, RepairTracker repairs) {
        Optional<String> stored = tag.getString(ORIGIN);
        if (stored.isEmpty()) {
            repairs.mark();
            return GuardProfileOrigin.LEGACY;
        }

        String serialized = stored.get();
        GuardProfileOrigin origin = GuardProfileOrigin.fromSerializedName(serialized);
        if (!origin.serializedName().equals(serialized)) {
            repairs.mark();
        }
        return origin;
    }

    private static int readInt(CompoundTag tag, String key, int fallback, RepairTracker repairs) {
        Optional<Integer> stored = tag.getInt(key);
        if (stored.isPresent()) return stored.get();
        repairs.mark();
        return fallback;
    }

    private static long readLong(CompoundTag tag, String key, long fallback, RepairTracker repairs) {
        Optional<Long> stored = tag.getLong(key);
        if (stored.isPresent()) return stored.get();
        repairs.mark();
        return fallback;
    }

    private static final class RepairTracker {
        private boolean repaired;

        void mark() {
            repaired = true;
        }

        boolean repaired() {
            return repaired;
        }
    }
}
