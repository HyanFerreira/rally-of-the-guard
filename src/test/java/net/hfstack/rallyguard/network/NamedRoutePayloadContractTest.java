package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.NamedRouteAssignmentC2SPayload;
import net.hfstack.rallyguard.network.payload.NamedRouteMutationC2SPayload;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NamedRoutePayloadContractTest {
    private static final UUID ROUTE_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID GUARD_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");

    @Test
    void ledgerSnapshotCarriesReusableRoutesAndGuardAssociation() {
        GuardListS2CPayload.Point first = new GuardListS2CPayload.Point(1, 64, 1);
        GuardListS2CPayload.Point second = new GuardListS2CPayload.Point(8, 64, 8);
        GuardListS2CPayload.NamedRouteEntry route = new GuardListS2CPayload.NamedRouteEntry(
                ROUTE_ID, "Village Walls", "minecraft:overworld", 30, List.of(first, second)
        );
        GuardListS2CPayload.Entry guard = new GuardListS2CPayload.Entry(
                7, GUARD_ID, "Aldric", true, 4, true, 30, List.of(first, second),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(ROUTE_ID), false
        );
        GuardListS2CPayload payload = new GuardListS2CPayload(
                true, List.of(), List.of(route), List.of(guard)
        );

        assertEquals(ROUTE_ID, payload.namedRoutes().getFirst().id());
        assertEquals(Optional.of(ROUTE_ID), payload.entries().getFirst().namedRouteId());
    }

    @Test
    void mutationAndAssignmentPayloadsRetainExplicitIntent() {
        NamedRouteMutationC2SPayload mutation = new NamedRouteMutationC2SPayload(
                NetworkConstants.NAMED_ROUTE_UPDATE, Optional.of(ROUTE_ID), "Village Walls", 30,
                List.of(new NamedRouteMutationC2SPayload.Point(1, 64, 1),
                        new NamedRouteMutationC2SPayload.Point(8, 64, 8))
        );
        NamedRouteAssignmentC2SPayload assignment = new NamedRouteAssignmentC2SPayload(7, Optional.of(ROUTE_ID));

        assertEquals(NetworkConstants.NAMED_ROUTE_UPDATE, mutation.action());
        assertEquals(Optional.of(ROUTE_ID), mutation.routeId());
        assertEquals(Optional.of(ROUTE_ID), assignment.routeId());
    }
}
