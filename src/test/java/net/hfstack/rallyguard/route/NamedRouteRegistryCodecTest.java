package net.hfstack.rallyguard.route;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamedRouteRegistryCodecTest {
    private static final UUID WALLS_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");

    @Test
    void roundTripPreservesDimensionWaitAndOrderedPoints() {
        NamedRouteRegistry original = new NamedRouteRegistry(NamedRouteRegistry.CURRENT_SCHEMA_VERSION,
                List.of(new NamedRouteDefinition(
                WALLS_ID, "Village Walls", "minecraft:overworld", 30,
                List.of(new BlockPos(1, 64, 2), new BlockPos(8, 65, 9)),
                List.of("Warehouse", "North Gate")
        )));

        NamedRouteRegistryReadResult.Available result = available(NamedRouteRegistryCodec.decode(
                NamedRouteRegistryCodec.encode(original)
        ));

        assertEquals(original, result.registry());
        assertFalse(result.repaired());
    }

    @Test
    void malformedRoutesAreDiscardedAndKnownDamageIsRepaired() {
        CompoundTag root = new CompoundTag();
        root.putInt("schema_version", 1);
        ListTag routes = new ListTag();
        CompoundTag valid = definition(WALLS_ID, "  Village Walls  ", "minecraft:overworld", 999);
        routes.add(valid);
        routes.add(definition(WALLS_ID, "Duplicate", "minecraft:overworld", 10));
        routes.add(definition(UUID.randomUUID(), "Broken", "not a dimension", 10));
        root.put("routes", routes);

        NamedRouteRegistryReadResult.Available result = available(NamedRouteRegistryCodec.decode(root));

        assertEquals(1, result.registry().routes().size());
        assertEquals("Village Walls", result.registry().routes().getFirst().name());
        assertEquals(600, result.registry().routes().getFirst().waitSeconds());
        assertTrue(result.repaired());
    }

    @Test
    void routeWithFewerThanTwoValidPointsIsDiscarded() {
        CompoundTag root = new CompoundTag();
        root.putInt("schema_version", 1);
        ListTag routes = new ListTag();
        CompoundTag route = definition(WALLS_ID, "Short", "minecraft:overworld", 5);
        ListTag points = new ListTag();
        points.add(point(1, 2, 3));
        route.put("points", points);
        routes.add(route);
        root.put("routes", routes);

        NamedRouteRegistryReadResult.Available result = available(NamedRouteRegistryCodec.decode(root));

        assertTrue(result.registry().routes().isEmpty());
        assertTrue(result.repaired());
    }

    @Test
    void futureSchemaRemainsReadOnly() {
        CompoundTag root = new CompoundTag();
        root.putInt("schema_version", NamedRouteRegistry.CURRENT_SCHEMA_VERSION + 1);
        root.putString("future", "keep");
        CompoundTag before = root.copy();

        NamedRouteRegistryReadResult.FutureSchema result = assertInstanceOf(
                NamedRouteRegistryReadResult.FutureSchema.class, NamedRouteRegistryCodec.decode(root)
        );

        assertEquals(NamedRouteRegistry.CURRENT_SCHEMA_VERSION + 1, result.schemaVersion());
        assertEquals(before, root);
    }

    @Test
    void versionOneRoutesMigrateWithOptionalLabelsEmpty() {
        CompoundTag root = new CompoundTag();
        root.putInt("schema_version", 1);
        ListTag routes = new ListTag();
        routes.add(definition(WALLS_ID, "Legacy", "minecraft:overworld", 10));
        root.put("routes", routes);

        NamedRouteRegistryReadResult.Available result = available(NamedRouteRegistryCodec.decode(root));

        assertEquals(NamedRouteRegistry.CURRENT_SCHEMA_VERSION, result.registry().schemaVersion());
        assertEquals(List.of("", ""), result.registry().routes().getFirst().pointLabels());
        assertTrue(result.repaired());
    }

    @Test
    void nullTagIsAbsent() {
        assertInstanceOf(NamedRouteRegistryReadResult.Absent.class, NamedRouteRegistryCodec.decode(null));
    }

    private static CompoundTag definition(UUID id, String name, String dimension, int waitSeconds) {
        CompoundTag route = new CompoundTag();
        route.putString("id", id.toString());
        route.putString("name", name);
        route.putString("dimension", dimension);
        route.putInt("wait_seconds", waitSeconds);
        ListTag points = new ListTag();
        points.add(point(1, 64, 2));
        points.add(point(8, 65, 9));
        route.put("points", points);
        return route;
    }

    private static CompoundTag point(int x, int y, int z) {
        CompoundTag point = new CompoundTag();
        point.putInt("x", x);
        point.putInt("y", y);
        point.putInt("z", z);
        return point;
    }

    private static NamedRouteRegistryReadResult.Available available(NamedRouteRegistryReadResult result) {
        return assertInstanceOf(NamedRouteRegistryReadResult.Available.class, result);
    }
}
