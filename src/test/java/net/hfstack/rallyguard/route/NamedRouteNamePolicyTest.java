package net.hfstack.rallyguard.route;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamedRouteNamePolicyTest {
    private static final UUID ROUTE_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");

    @Test
    void normalizationTrimsAndCountsUnicodeCodePoints() {
        assertEquals(Optional.of("Muralhas 🛡"), NamedRouteNamePolicy.normalize("  Muralhas 🛡  ", 10));
        assertTrue(NamedRouteNamePolicy.normalize("🛡🛡", 1).isEmpty());
        assertTrue(NamedRouteNamePolicy.normalize("   ", 10).isEmpty());
    }

    @Test
    void duplicateComparisonIsCaseInsensitiveAndCanExcludeEditedRoute() {
        NamedRouteRegistry registry = new NamedRouteRegistry(1, List.of(new NamedRouteDefinition(
                ROUTE_ID, "Village Walls", "minecraft:overworld", 30,
                List.of(BlockPos.ZERO, BlockPos.ZERO.above())
        )));

        assertTrue(NamedRouteNamePolicy.isDuplicate(registry, "village walls", Optional.empty()));
        assertFalse(NamedRouteNamePolicy.isDuplicate(registry, "Village Walls", Optional.of(ROUTE_ID)));
    }
}
