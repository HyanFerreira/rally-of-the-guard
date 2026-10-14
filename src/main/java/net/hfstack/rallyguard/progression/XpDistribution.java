package net.hfstack.rallyguard.progression;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class XpDistribution {
    private XpDistribution() {
    }

    public static long scaledBudgetMilli(int baseXp, double multiplier) {
        if (baseXp <= 0 || !Double.isFinite(multiplier) || multiplier <= 0.0) {
            return 0;
        }
        double scaled = baseXp * multiplier * 1_000.0;
        return scaled >= Long.MAX_VALUE ? Long.MAX_VALUE : (long) Math.floor(scaled);
    }

    public static Map<UUID, Long> allocate(
            long budgetMilli,
            double totalEffectiveDamage,
            Map<UUID, Double> guardDamage,
            Optional<UUID> finalGuard,
            XpShares shares
    ) {
        Objects.requireNonNull(guardDamage, "guardDamage");
        Objects.requireNonNull(finalGuard, "finalGuard");
        Objects.requireNonNull(shares, "shares");
        if (budgetMilli <= 0 || !(totalEffectiveDamage > 0.0) || !Double.isFinite(totalEffectiveDamage)) {
            return Map.of();
        }

        boolean guardFinalBlow = finalGuard.filter(guardDamage::containsKey).isPresent();
        double contributionShare = guardFinalBlow ? shares.contribution() : 1.0;
        Map<UUID, Long> awards = new LinkedHashMap<>();
        long awarded = 0;
        for (Map.Entry<UUID, Double> entry : guardDamage.entrySet()) {
            double damage = entry.getValue() == null ? 0.0 : entry.getValue();
            if (!(damage > 0.0) || !Double.isFinite(damage)) continue;
            double exact = budgetMilli * contributionShare * Math.min(damage, totalEffectiveDamage)
                    / totalEffectiveDamage;
            if (guardFinalBlow && finalGuard.orElseThrow().equals(entry.getKey())) {
                exact += budgetMilli * shares.finalBlow();
            }
            long award = Math.max(0, (long) Math.floor(exact));
            award = Math.min(award, budgetMilli - awarded);
            if (award > 0) {
                awards.put(entry.getKey(), award);
                awarded += award;
            }
            if (awarded == budgetMilli) break;
        }
        return Map.copyOf(awards);
    }

    public static boolean qualifiesForAssist(
            double guardDamage,
            double totalEffectiveDamage,
            double minimumDamage,
            double minimumShare
    ) {
        return Double.isFinite(guardDamage)
                && Double.isFinite(totalEffectiveDamage)
                && totalEffectiveDamage > 0.0
                && guardDamage >= Math.max(0.0, minimumDamage)
                && guardDamage / totalEffectiveDamage >= Math.max(0.0, minimumShare);
    }
}
