package net.hfstack.rallyguard.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuardOrderStatusTest {
    @Test
    void existingWireIdsRemainStableAndNewOrdersAppendIds() {
        assertEquals(0, GuardOrderStatus.IDLE);
        assertEquals(1, GuardOrderStatus.FOLLOWING);
        assertEquals(2, GuardOrderStatus.WAITING);
        assertEquals(3, GuardOrderStatus.PATROLLING);
        assertEquals(4, GuardOrderStatus.ROUTING);
        assertEquals(5, GuardOrderStatus.MOVING);
        assertEquals(6, GuardOrderStatus.DEFENDING);
    }

    @Test
    void movingTakesPrecedenceOverEveryLegacyOrderState() {
        assertEquals(GuardOrderStatus.MOVING, GuardOrderStatus.resolve(true, true, true, true, true));
        assertEquals(GuardOrderStatus.ROUTING, GuardOrderStatus.resolve(false, true, true, true, true));
        assertEquals(GuardOrderStatus.PATROLLING, GuardOrderStatus.resolve(false, false, true, true, true));
        assertEquals(GuardOrderStatus.WAITING, GuardOrderStatus.resolve(false, false, false, true, true));
        assertEquals(GuardOrderStatus.FOLLOWING, GuardOrderStatus.resolve(false, false, false, false, true));
        assertEquals(GuardOrderStatus.IDLE, GuardOrderStatus.resolve(false, false, false, false, false));
    }

    @Test
    void defendingTakesPrecedenceOverNativePatrolState() {
        assertEquals(
                GuardOrderStatus.DEFENDING,
                GuardOrderStatus.resolve(true, false, false, true, true, true)
        );
        assertEquals(
                GuardOrderStatus.MOVING,
                GuardOrderStatus.resolve(false, true, true, true, true, true)
        );
    }
}
