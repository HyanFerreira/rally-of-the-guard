package net.hfstack.rallyguard.progression;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatContributionTrackerTest {
    private static final UUID VICTIM = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID GUARD = UUID.fromString("00000000-0000-0000-0000-000000000011");

    @Test
    void tracksEffectiveHealthRemovedAndAllDamageInDenominator() {
        CombatContributionTracker tracker = new CombatContributionTracker();
        tracker.beginDamage(VICTIM, 20.0, 1);
        assertEquals(8.0, tracker.completeDamage(VICTIM, 12.0, Optional.of(GUARD), 1, 600));
        tracker.beginDamage(VICTIM, 12.0, 2);
        assertEquals(12.0, tracker.completeDamage(VICTIM, 0.0, Optional.empty(), 2, 600));

        CombatLedgerSnapshot snapshot = tracker.finish(VICTIM, Optional.empty()).orElseThrow();
        assertEquals(20.0, snapshot.totalEffectiveDamage());
        assertEquals(8.0, snapshot.guardDamage().get(GUARD));
        assertEquals(0, tracker.activeLedgerCount());
    }

    @Test
    void doesNotCountOverkillBeyondPreHitHealth() {
        CombatContributionTracker tracker = new CombatContributionTracker();
        tracker.beginDamage(VICTIM, 5.0, 1);

        assertEquals(5.0, tracker.completeDamage(VICTIM, -20.0, Optional.of(GUARD), 1, 600));
    }

    @Test
    void expiresPendingAndActiveCombatState() {
        CombatContributionTracker tracker = new CombatContributionTracker();
        tracker.beginDamage(VICTIM, 20.0, 0);
        assertEquals(0.0, tracker.completeDamage(VICTIM, 10.0, Optional.of(GUARD), 31, 30));

        tracker.beginDamage(VICTIM, 20.0, 40);
        tracker.completeDamage(VICTIM, 10.0, Optional.of(GUARD), 40, 30);
        tracker.cleanup(71, 30);

        assertEquals(0, tracker.activeLedgerCount());
        assertTrue(tracker.finish(VICTIM, Optional.empty()).isEmpty());
    }

    @Test
    void cancelingOnePendingHitPreservesEarlierContribution() {
        CombatContributionTracker tracker = new CombatContributionTracker();
        tracker.beginDamage(VICTIM, 20.0, 1);
        tracker.completeDamage(VICTIM, 15.0, Optional.of(GUARD), 1, 600);
        tracker.beginDamage(VICTIM, 15.0, 2);
        tracker.cancelPending(VICTIM);

        CombatLedgerSnapshot snapshot = tracker.finish(VICTIM, Optional.of(GUARD)).orElseThrow();
        assertEquals(5.0, snapshot.totalEffectiveDamage());
    }
}
