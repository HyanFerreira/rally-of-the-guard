package net.hfstack.rallyguard.raid;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaidReportSessionTest {
    @Test
    void summaryUsesConfirmedKillsAndTracksObservedFacts() {
        RaidReportSession session = new RaidReportSession(4, 1000);
        UUID alpha = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID bravo = UUID.fromString("00000000-0000-0000-0000-000000000002");
        session.recordEnemyDefeated();
        session.recordEnemyDefeated();
        session.recordKill(alpha, "Alpha");
        session.recordKill(bravo, "Bravo");
        session.recordKill(bravo, "Bravo");
        session.recordGuardLost();

        RaidReportSummary summary = session.summary(false);
        assertEquals(4, summary.guardsDeployed());
        assertEquals(2, summary.enemiesDefeated());
        assertEquals(1, summary.guardsLost());
        assertEquals("Bravo", summary.mvpName().orElseThrow());
        assertEquals(2, summary.mvpKills());
    }

    @Test
    void unloadOrForcedExpiryMarksReportIncomplete() {
        RaidReportSession unloaded = new RaidReportSession(1, 10);
        unloaded.markIncomplete();
        assertTrue(unloaded.summary(false).incomplete());

        RaidReportSession expired = new RaidReportSession(1, 10);
        assertTrue(expired.summary(true).incomplete());
    }
}
