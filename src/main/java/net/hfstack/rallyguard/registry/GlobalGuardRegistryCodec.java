package net.hfstack.rallyguard.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class GlobalGuardRegistryCodec {
    public static final int MAX_ENTRIES = 2048;
    private static final int MAX_NAME_CODE_POINTS = 64;
    private static final int MAX_SQUAD_CODE_POINTS = 48;

    private GlobalGuardRegistryCodec() {
    }

    public static CompoundTag encode(GlobalGuardRegistry registry) {
        CompoundTag root = new CompoundTag();
        root.putInt("schema_version", registry.schemaVersion());
        ListTag guards = new ListTag();
        registry.guards().stream().limit(MAX_ENTRIES).forEach(record -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", record.guardId().toString());
            entry.putString("name", record.name());
            entry.putString("dimension", record.dimensionId());
            entry.putInt("x", record.lastPosition().getX());
            entry.putInt("y", record.lastPosition().getY());
            entry.putInt("z", record.lastPosition().getZ());
            entry.putLong("last_seen", record.lastSeenGameTime());
            entry.putInt("level", record.level());
            entry.putString("rank", record.rankId());
            entry.putBoolean("captain", record.captain());
            record.squadName().ifPresent(name -> entry.putString("squad", name));
            entry.putInt("order", record.orderStatus());
            guards.add(entry);
        });
        root.put("guards", guards);
        return root;
    }

    public static GlobalGuardRegistryRead decode(CompoundTag root) {
        if (root == null) return new GlobalGuardRegistryRead.Absent();
        Integer schema = strictInt(root.get("schema_version"));
        if (schema != null && schema > GlobalGuardRegistry.SCHEMA_VERSION) {
            return new GlobalGuardRegistryRead.FutureSchema(schema);
        }
        Repair repair = new Repair(schema == null || schema != GlobalGuardRegistry.SCHEMA_VERSION);
        if (!(root.get("guards") instanceof ListTag list)) {
            return new GlobalGuardRegistryRead.Available(GlobalGuardRegistry.empty(), true);
        }
        List<GlobalGuardRecord> records = new ArrayList<>();
        Set<UUID> seen = new HashSet<>();
        for (Tag value : list) {
            if (records.size() >= MAX_ENTRIES) {
                repair.mark();
                break;
            }
            if (!(value instanceof CompoundTag entry)) {
                repair.mark();
                continue;
            }
            Optional<UUID> id = entry.getString("id").flatMap(GlobalGuardRegistryCodec::parseUuid);
            String name = bounded(entry.getString("name").orElse(""), MAX_NAME_CODE_POINTS);
            String dimension = entry.getString("dimension").orElse("");
            Integer x = strictInt(entry.get("x"));
            Integer y = strictInt(entry.get("y"));
            Integer z = strictInt(entry.get("z"));
            if (id.isEmpty() || !seen.add(id.get()) || name.isBlank() || Identifier.tryParse(dimension) == null
                    || x == null || y == null || z == null) {
                repair.mark();
                continue;
            }
            long lastSeen = entry.getLong("last_seen").orElse(0L);
            Integer storedLevel = strictInt(entry.get("level"));
            int level = Math.max(0, storedLevel == null ? 0 : storedLevel);
            String rank = bounded(entry.getString("rank").orElse(""), 32);
            Optional<String> squad = entry.getString("squad")
                    .map(valueName -> bounded(valueName, MAX_SQUAD_CODE_POINTS)).filter(valueName -> !valueName.isBlank());
            Integer storedOrder = strictInt(entry.get("order"));
            int order = Math.max(0, storedOrder == null ? 0 : storedOrder);
            GlobalGuardRecord record = new GlobalGuardRecord(
                    id.get(), name, dimension, new BlockPos(x, y, z), lastSeen, level, rank,
                    entry.getBoolean("captain").orElse(false), squad, order
            );
            if (!record.name().equals(entry.getString("name").orElse(""))) repair.mark();
            records.add(record);
        }
        return new GlobalGuardRegistryRead.Available(
                new GlobalGuardRegistry(GlobalGuardRegistry.SCHEMA_VERSION, records), repair.value
        );
    }

    private static Integer strictInt(Tag value) {
        return value instanceof NumericTag number && value.getId() == Tag.TAG_INT ? number.intValue() : null;
    }

    private static Optional<UUID> parseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static String bounded(String value, int max) {
        String normalized = value.trim();
        return normalized.codePointCount(0, normalized.length()) <= max
                ? normalized : normalized.substring(0, normalized.offsetByCodePoints(0, max));
    }

    private static final class Repair {
        private boolean value;
        private Repair(boolean value) { this.value = value; }
        private void mark() { value = true; }
    }
}
