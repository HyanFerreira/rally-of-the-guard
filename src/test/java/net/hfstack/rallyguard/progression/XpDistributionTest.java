package net.hfstack.rallyguard.progression;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XpDistributionTest {
    private static final UUID GUARD_A = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID GUARD_B = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void appliesContributionAndFinalBlowSharesInMilliXp() {
        Map<UUID, Long> awards = XpDistribution.allocate(
                5_000,
                20.0,
                damage(15.0, 5.0),
                Optional.of(GUARD_B),
                new XpShares(0.80, 0.20)
        );

        assertEquals(3_000, awards.get(GUARD_A));
        assertEquals(2_000, awards.get(GUARD_B));
        assertEquals(5_000, awards.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    void returnsFinalBlowShareToContributionWhenNonGuardKills() {
        Map<UUID, Long> awards = XpDistribution.allocate(
                5_000,
                20.0,
                damage(15.0, 5.0),
                Optional.empty(),
                new XpShares(0.80, 0.20)
        );

        assertEquals(3_750, awards.get(GUARD_A));
        assertEquals(1_250, awards.get(GUARD_B));
    }

    @Test
    void environmentalDamageRemainsInTheDenominatorAndAwardsNeverExceedBudget() {
        Map<UUID, Long> awards = XpDistribution.allocate(
                5_000,
                30.0,
                damage(10.0, 5.0),
                Optional.of(GUARD_B),
                new XpShares(0.80, 0.20)
        );

        assertEquals(1_333, awards.get(GUARD_A));
        assertEquals(1_666, awards.get(GUARD_B));
        assertTrue(awards.values().stream().mapToLong(Long::longValue).sum() <= 5_000);
    }

    @Test
    void assistRequiresBothDamageAndShareThresholds() {
        assertTrue(XpDistribution.qualifiesForAssist(1.0, 20.0, 1.0, 0.05));
        assertFalse(XpDistribution.qualifiesForAssist(0.99, 10.0, 1.0, 0.05));
        assertFalse(XpDistribution.qualifiesForAssist(1.0, 21.0, 1.0, 0.05));
    }

    @Test
    void multiplierIsAppliedBeforeDistribution() {
        assertEquals(7_500, XpDistribution.scaledBudgetMilli(5, 1.5));
        assertEquals(0, XpDistribution.scaledBudgetMilli(5, Double.NaN));
        assertEquals(0, XpDistribution.scaledBudgetMilli(0, 1.0));
    }

    private static Map<UUID, Double> damage(double first, double second) {
        Map<UUID, Double> damage = new LinkedHashMap<>();
        damage.put(GUARD_A, first);
        damage.put(GUARD_B, second);
        return damage;
    }
}
