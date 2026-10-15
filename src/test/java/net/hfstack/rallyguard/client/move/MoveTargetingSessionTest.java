package net.hfstack.rallyguard.client.move;

import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.api.command.GuardUnitCategory;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveTargetingSessionTest {
    @Test
    void armedSessionCanBeConsumedExactlyOnce() {
        MoveTargetingSession session = new MoveTargetingSession();
        session.arm(GuardCommandScope.unassigned(), GuardUnitCategory.RANGED, 1, 40);

        Optional<MoveTargetingSession.Armed> request = session.consume(41, true, true);

        assertEquals(GuardCommandScope.unassigned(), request.orElseThrow().scope());
        assertEquals(GuardUnitCategory.RANGED, request.orElseThrow().category());
        assertEquals(1, request.orElseThrow().handId());
        assertTrue(session.consume(42, true, true).isEmpty());
    }

    @Test
    void sessionExpiresAtThreeHundredTicks() {
        MoveTargetingSession session = armedAt(10);

        assertEquals(MoveTargetingSession.Update.ACTIVE, session.update(309, true, true));
        assertEquals(MoveTargetingSession.Update.EXPIRED, session.update(310, true, true));
        assertFalse(session.isArmed());
    }

    @Test
    void itemRemovalOrDeathCancelsWithoutProducingARequest() {
        MoveTargetingSession missingLedger = armedAt(0);
        MoveTargetingSession deadPlayer = armedAt(0);

        assertEquals(MoveTargetingSession.Update.CANCELLED, missingLedger.update(1, true, false));
        assertEquals(MoveTargetingSession.Update.CANCELLED, deadPlayer.update(1, false, true));
        assertTrue(missingLedger.consume(1, true, true).isEmpty());
        assertTrue(deadPlayer.consume(1, true, true).isEmpty());
    }

    @Test
    void explicitCancelAndDisconnectResetClearTransientState() {
        MoveTargetingSession cancelled = armedAt(0);
        MoveTargetingSession disconnected = armedAt(0);

        assertTrue(cancelled.cancel());
        disconnected.reset();

        assertFalse(cancelled.isArmed());
        assertFalse(disconnected.isArmed());
        assertFalse(cancelled.cancel());
    }

    private static MoveTargetingSession armedAt(long tick) {
        MoveTargetingSession session = new MoveTargetingSession();
        session.arm(GuardCommandScope.all(), GuardUnitCategory.ALL, 0, tick);
        return session;
    }
}
