package net.hfstack.rallyguard.order;

import net.hfstack.rallyguard.config.RallyConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

class GuardRoutesTest {
    private static final String PREFIX = "rallyguard:route:v1:";
    private static final String PREFIX_V2 = "rallyguard:route:v2:";

    @Test
    void malformedNumericFieldsReturnEmptyInactiveRoute() {
        GuardRouteState route = GuardRoutes.parse(PREFIX + "1:not-a-number:20:0:1,2,3;4,5,6");

        assertFalse(route.active());
        assertTrue(route.points().isEmpty());
    }

    @Test
    void negativeCountersAreClampedToZero() {
        GuardRouteState route = GuardRoutes.parse(PREFIX + "1:-3:-20:-7:1,2,3;4,5,6");

        assertEquals(0, route.currentIndex());
        assertEquals(0, route.waitTicks());
        assertEquals(0, route.dwellTicks());
    }

    @Test
    void indexBeyondPointListResetsToZero() {
        GuardRouteState route = GuardRoutes.parse(PREFIX + "1:9:20:0:1,2,3;4,5,6");

        assertEquals(0, route.currentIndex());
        assertEquals(2, route.points().size());
    }

    @Test
    void pointsBeyondConfiguredMaximumAreDiscarded() {
        StringBuilder points = new StringBuilder();
        for (int i = 0; i < RallyConfig.routeMaxPoints() + 2; i++) {
            if (i > 0) points.append(';');
            points.append(i).append(",64,").append(i);
        }

        GuardRouteState route = GuardRoutes.parse(PREFIX + "1:0:20:0:" + points);

        assertEquals(RallyConfig.routeMaxPoints(), route.points().size());
    }

    @Test
    void versionOneRoutesMigrateWithoutInventingNamedAssociation() {
        GuardRouteState route = GuardRoutes.parse(PREFIX + "1:0:20:0:1,2,3;4,5,6");

        assertTrue(route.active());
        assertTrue(route.namedRouteId().isEmpty());
    }

    @Test
    void versionTwoPreservesNamedRouteAssociation() {
        UUID routeId = UUID.fromString("30000000-0000-0000-0000-000000000001");

        GuardRouteState route = GuardRoutes.parse(
                PREFIX_V2 + "1:1:40:5:" + routeId + ":1,2,3;4,5,6"
        );

        assertEquals(routeId, route.namedRouteId().orElseThrow());
        assertEquals(1, route.currentIndex());
        assertEquals(5, route.dwellTicks());
    }

    @Test
    void malformedVersionTwoAssociationFailsClosed() {
        GuardRouteState route = GuardRoutes.parse(PREFIX_V2 + "1:0:20:0:not-a-uuid:1,2,3;4,5,6");

        assertFalse(route.active());
        assertTrue(route.points().isEmpty());
        assertTrue(route.namedRouteId().isEmpty());
    }
}
