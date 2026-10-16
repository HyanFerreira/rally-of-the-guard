package net.hfstack.rallyguard.route;

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

public final class NamedRouteRegistryCodec {
    private static final String SCHEMA_VERSION = "schema_version";
    private static final String ROUTES = "routes";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String DIMENSION = "dimension";
    private static final String WAIT_SECONDS = "wait_seconds";
    private static final String POINTS = "points";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";
    private static final int MAX_STORED_ROUTES = 64;
    private static final int MAX_STORED_NAME_CODE_POINTS = 48;
    private static final int MAX_STORED_POINTS = 10;
    private static final int MAX_WAIT_SECONDS = 600;

    private NamedRouteRegistryCodec() {
    }

    public static CompoundTag encode(NamedRouteRegistry registry) {
        CompoundTag root = new CompoundTag();
        root.putInt(SCHEMA_VERSION, registry.schemaVersion());
        ListTag routes = new ListTag();
        for (NamedRouteDefinition definition : registry.routes()) {
            CompoundTag entry = new CompoundTag();
            entry.putString(ID, definition.id().toString());
            entry.putString(NAME, definition.name());
            entry.putString(DIMENSION, definition.dimensionId());
            entry.putInt(WAIT_SECONDS, definition.waitSeconds());
            ListTag points = new ListTag();
            for (BlockPos point : definition.points()) {
                CompoundTag storedPoint = new CompoundTag();
                storedPoint.putInt(X, point.getX());
                storedPoint.putInt(Y, point.getY());
                storedPoint.putInt(Z, point.getZ());
                points.add(storedPoint);
            }
            entry.put(POINTS, points);
            routes.add(entry);
        }
        root.put(ROUTES, routes);
        return root;
    }

    public static NamedRouteRegistryReadResult decode(CompoundTag root) {
        if (root == null) return new NamedRouteRegistryReadResult.Absent();
        RepairTracker repairs = new RepairTracker();
        int schemaVersion = readInt(root.get(SCHEMA_VERSION), NamedRouteRegistry.CURRENT_SCHEMA_VERSION, repairs);
        if (schemaVersion > NamedRouteRegistry.CURRENT_SCHEMA_VERSION) {
            return new NamedRouteRegistryReadResult.FutureSchema(schemaVersion);
        }
        if (schemaVersion != NamedRouteRegistry.CURRENT_SCHEMA_VERSION) repairs.mark();

        Tag storedRoutes = root.get(ROUTES);
        if (!(storedRoutes instanceof ListTag list)) {
            repairs.mark();
            return new NamedRouteRegistryReadResult.Available(NamedRouteRegistry.empty(), true);
        }

        List<NamedRouteDefinition> decoded = new ArrayList<>();
        Set<UUID> seen = new HashSet<>();
        for (Tag value : list) {
            if (decoded.size() >= MAX_STORED_ROUTES) {
                repairs.mark();
                break;
            }
            if (!(value instanceof CompoundTag entry)) {
                repairs.mark();
                continue;
            }
            Optional<UUID> id = parseUuid(entry.getString(ID).orElse(null));
            Optional<String> storedName = entry.getString(NAME);
            Optional<String> storedDimension = entry.getString(DIMENSION);
            if (id.isEmpty() || storedName.isEmpty() || storedDimension.isEmpty() || !seen.add(id.get())) {
                repairs.mark();
                continue;
            }
            String name = boundCodePoints(storedName.get().trim(), MAX_STORED_NAME_CODE_POINTS);
            Identifier dimension = Identifier.tryParse(storedDimension.get());
            if (name.isEmpty() || dimension == null) {
                repairs.mark();
                continue;
            }
            if (!name.equals(storedName.get()) || !dimension.toString().equals(storedDimension.get())) repairs.mark();
            int waitSeconds = readInt(entry.get(WAIT_SECONDS), 0, repairs);
            int boundedWait = Math.max(0, Math.min(MAX_WAIT_SECONDS, waitSeconds));
            if (boundedWait != waitSeconds) repairs.mark();
            List<BlockPos> points = decodePoints(entry.get(POINTS), repairs);
            if (points.size() < 2) {
                repairs.mark();
                continue;
            }
            decoded.add(new NamedRouteDefinition(id.get(), name, dimension.toString(), boundedWait, points));
        }
        return new NamedRouteRegistryReadResult.Available(
                new NamedRouteRegistry(NamedRouteRegistry.CURRENT_SCHEMA_VERSION, decoded), repairs.repaired()
        );
    }

    private static List<BlockPos> decodePoints(Tag stored, RepairTracker repairs) {
        if (!(stored instanceof ListTag list)) {
            repairs.mark();
            return List.of();
        }
        List<BlockPos> points = new ArrayList<>();
        for (Tag value : list) {
            if (points.size() >= MAX_STORED_POINTS) {
                repairs.mark();
                break;
            }
            if (!(value instanceof CompoundTag point)
                    || !(point.get(X) instanceof NumericTag x)
                    || !(point.get(Y) instanceof NumericTag y)
                    || !(point.get(Z) instanceof NumericTag z)) {
                repairs.mark();
                continue;
            }
            points.add(new BlockPos(x.intValue(), y.intValue(), z.intValue()));
        }
        return points;
    }

    private static int readInt(Tag value, int fallback, RepairTracker repairs) {
        if (!(value instanceof NumericTag numeric)) {
            repairs.mark();
            return fallback;
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
        if (value.codePointCount(0, value.length()) <= maximum) return value;
        return value.substring(0, value.offsetByCodePoints(0, maximum));
    }

    private static final class RepairTracker {
        private boolean repaired;
        void mark() { repaired = true; }
        boolean repaired() { return repaired; }
    }
}
