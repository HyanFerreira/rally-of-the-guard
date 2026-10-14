package net.hfstack.rallyguard.squad;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class SquadRegistryCodec {
    private static final String SCHEMA_VERSION = "schema_version";
    private static final String SQUADS = "squads";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String OFFICER = "officer";
    private static final int MAX_STORED_SQUADS = 64;
    private static final int MAX_STORED_NAME_CODE_POINTS = 48;

    private SquadRegistryCodec() {
    }

    public static CompoundTag encode(SquadRegistry registry) {
        CompoundTag root = new CompoundTag();
        root.putInt(SCHEMA_VERSION, registry.schemaVersion());
        ListTag squads = new ListTag();
        for (SquadDefinition definition : registry.squads()) {
            CompoundTag entry = new CompoundTag();
            entry.putString(ID, definition.id().toString());
            entry.putString(NAME, definition.name());
            definition.officerGuardId().ifPresent(value -> entry.putString(OFFICER, value.toString()));
            squads.add(entry);
        }
        root.put(SQUADS, squads);
        return root;
    }

    public static SquadRegistryReadResult decode(CompoundTag root) {
        if (root == null) return new SquadRegistryReadResult.Absent();

        RepairTracker repairs = new RepairTracker();
        int schemaVersion = readSchemaVersion(root, repairs);
        if (schemaVersion > SquadRegistry.CURRENT_SCHEMA_VERSION) {
            return new SquadRegistryReadResult.FutureSchema(schemaVersion);
        }
        if (schemaVersion != SquadRegistry.CURRENT_SCHEMA_VERSION) repairs.mark();

        Tag storedSquads = root.get(SQUADS);
        if (!(storedSquads instanceof ListTag list)) {
            repairs.mark();
            return new SquadRegistryReadResult.Available(SquadRegistry.empty(), true);
        }

        List<SquadDefinition> decoded = new ArrayList<>();
        Set<UUID> seen = new HashSet<>();
        for (Tag value : list) {
            if (decoded.size() >= MAX_STORED_SQUADS) {
                repairs.mark();
                break;
            }
            if (!(value instanceof CompoundTag entry)) {
                repairs.mark();
                continue;
            }

            Optional<UUID> id = parseUuid(entry.getString(ID).orElse(null));
            Optional<String> storedName = entry.getString(NAME);
            if (id.isEmpty() || storedName.isEmpty()) {
                repairs.mark();
                continue;
            }
            String name = storedName.get().trim();
            if (name.isEmpty()) {
                repairs.mark();
                continue;
            }
            String boundedName = boundCodePoints(name, MAX_STORED_NAME_CODE_POINTS);
            if (!boundedName.equals(storedName.get())) repairs.mark();
            if (!seen.add(id.get())) {
                repairs.mark();
                continue;
            }

            Optional<UUID> officer = Optional.empty();
            Optional<String> storedOfficer = entry.getString(OFFICER);
            if (storedOfficer.isPresent()) {
                officer = parseUuid(storedOfficer.get());
                if (officer.isEmpty()) repairs.mark();
            }
            decoded.add(new SquadDefinition(id.get(), boundedName, officer));
        }

        return new SquadRegistryReadResult.Available(
                new SquadRegistry(SquadRegistry.CURRENT_SCHEMA_VERSION, decoded),
                repairs.repaired()
        );
    }

    private static int readSchemaVersion(CompoundTag tag, RepairTracker repairs) {
        Tag value = tag.get(SCHEMA_VERSION);
        if (!(value instanceof NumericTag numeric)) {
            repairs.mark();
            return SquadRegistry.CURRENT_SCHEMA_VERSION;
        }
        long number = numeric.longValue();
        if (value.getId() != Tag.TAG_INT || number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) repairs.mark();
        if (number < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        if (number > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) number;
    }

    private static Optional<UUID> parseUuid(String value) {
        if (value == null) return Optional.empty();
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static String boundCodePoints(String value, int maximum) {
        int count = value.codePointCount(0, value.length());
        if (count <= maximum) return value;
        return value.substring(0, value.offsetByCodePoints(0, maximum));
    }

    private static final class RepairTracker {
        private boolean repaired;
        void mark() { repaired = true; }
        boolean repaired() { return repaired; }
    }
}
