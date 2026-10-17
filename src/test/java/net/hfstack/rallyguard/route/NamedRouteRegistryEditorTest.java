package net.hfstack.rallyguard.route;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class NamedRouteRegistryEditorTest {
    private static final UUID ROUTE_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final List<BlockPos> POINTS = List.of(new BlockPos(1, 64, 1), new BlockPos(8, 64, 8));

    @Test
    void createNormalizesNameAndPreservesOrderedDefinition() {
        NamedRouteRegistryEditor.Success result = success(NamedRouteRegistryEditor.create(
                NamedRouteRegistry.empty(), ROUTE_ID, "  Village Walls  ", "minecraft:overworld",
                30, POINTS, List.of("  Warehouse  ", "North Gate"), 32, 32, 5
        ));

        assertEquals("Village Walls", result.affected().name());
        assertEquals(POINTS, result.affected().points());
        assertEquals(List.of("Warehouse", "North Gate"), result.affected().pointLabels());
        assertEquals(List.of(result.affected()), result.registry().routes());
    }

    @Test
    void duplicateNamesAreRejectedCaseInsensitively() {
        NamedRouteRegistry registry = success(NamedRouteRegistryEditor.create(
                NamedRouteRegistry.empty(), ROUTE_ID, "Village Walls", "minecraft:overworld",
                30, POINTS, 32, 32, 5
        )).registry();

        NamedRouteRegistryEditor.Failure failure = failure(NamedRouteRegistryEditor.create(
                registry, UUID.randomUUID(), "village walls", "minecraft:overworld",
                30, POINTS, 32, 32, 5
        ));

        assertEquals(NamedRouteRegistryEditor.FailureReason.DUPLICATE_NAME, failure.reason());
    }

    @Test
    void invalidDimensionPointCountAndWaitAreRejected() {
        assertEquals(NamedRouteRegistryEditor.FailureReason.INVALID_ROUTE, failure(NamedRouteRegistryEditor.create(
                NamedRouteRegistry.empty(), ROUTE_ID, "Route", "invalid dimension",
                30, POINTS, 32, 32, 5
        )).reason());
        assertEquals(NamedRouteRegistryEditor.FailureReason.INVALID_ROUTE, failure(NamedRouteRegistryEditor.create(
                NamedRouteRegistry.empty(), ROUTE_ID, "Route", "minecraft:overworld",
                -1, List.of(BlockPos.ZERO), 32, 32, 5
        )).reason());
    }

    @Test
    void updateKeepsIdentityAndDeleteReturnsRemovedDefinition() {
        NamedRouteRegistry created = success(NamedRouteRegistryEditor.create(
                NamedRouteRegistry.empty(), ROUTE_ID, "Old", "minecraft:overworld",
                30, POINTS, 32, 32, 5
        )).registry();
        NamedRouteRegistryEditor.Success updated = success(NamedRouteRegistryEditor.update(
                created, ROUTE_ID, "New", "minecraft:the_nether", 5,
                List.of(BlockPos.ZERO, BlockPos.ZERO.above()), 32, 5
        ));

        assertEquals(ROUTE_ID, updated.affected().id());
        assertEquals("New", updated.affected().name());
        assertEquals("minecraft:the_nether", updated.affected().dimensionId());
        assertEquals(ROUTE_ID, success(NamedRouteRegistryEditor.delete(updated.registry(), ROUTE_ID)).affected().id());
    }

    private static NamedRouteRegistryEditor.Success success(NamedRouteRegistryEditor.EditResult result) {
        return assertInstanceOf(NamedRouteRegistryEditor.Success.class, result);
    }

    private static NamedRouteRegistryEditor.Failure failure(NamedRouteRegistryEditor.EditResult result) {
        return assertInstanceOf(NamedRouteRegistryEditor.Failure.class, result);
    }
}
