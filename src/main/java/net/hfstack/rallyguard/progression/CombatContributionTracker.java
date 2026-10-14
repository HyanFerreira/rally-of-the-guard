package net.hfstack.rallyguard.progression;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CombatContributionTracker {
    private final Map<UUID, PendingDamage> pending = new HashMap<>();
    private final Map<UUID, VictimLedger> ledgers = new HashMap<>();

    public void beginDamage(UUID victimId, double healthBefore, long gameTick) {
        pending.put(victimId, new PendingDamage(Math.max(0.0, healthBefore), gameTick));
    }

    public double completeDamage(
            UUID victimId,
            double healthAfter,
            Optional<UUID> guardId,
            long gameTick,
            long timeoutTicks
    ) {
        PendingDamage before = pending.remove(victimId);
        if (before == null || gameTick - before.gameTick() > timeoutTicks) {
            return 0.0;
        }
        double effectiveDamage = Math.max(0.0, before.healthBefore() - Math.max(0.0, healthAfter));
        if (!(effectiveDamage > 0.0) || !Double.isFinite(effectiveDamage)) {
            return 0.0;
        }

        VictimLedger ledger = ledgers.get(victimId);
        if (ledger == null || gameTick - ledger.lastDamageTick > timeoutTicks) {
            ledger = new VictimLedger();
            ledgers.put(victimId, ledger);
        }
        ledger.totalEffectiveDamage += effectiveDamage;
        ledger.lastDamageTick = gameTick;
        if (guardId.isPresent()) {
            ledger.guardDamage.merge(guardId.orElseThrow(), effectiveDamage, Double::sum);
        }
        return effectiveDamage;
    }

    public void cancelPending(UUID victimId) {
        pending.remove(victimId);
    }

    public Optional<CombatLedgerSnapshot> finish(UUID victimId, Optional<UUID> finalGuard) {
        pending.remove(victimId);
        VictimLedger ledger = ledgers.remove(victimId);
        if (ledger == null || !(ledger.totalEffectiveDamage > 0.0)) {
            return Optional.empty();
        }
        return Optional.of(new CombatLedgerSnapshot(
                ledger.totalEffectiveDamage,
                ledger.guardDamage,
                finalGuard
        ));
    }

    public void remove(UUID victimId) {
        pending.remove(victimId);
        ledgers.remove(victimId);
    }

    public void cleanup(long gameTick, long timeoutTicks) {
        pending.entrySet().removeIf(entry -> gameTick - entry.getValue().gameTick() > timeoutTicks);
        Iterator<Map.Entry<UUID, VictimLedger>> iterator = ledgers.entrySet().iterator();
        while (iterator.hasNext()) {
            if (gameTick - iterator.next().getValue().lastDamageTick > timeoutTicks) {
                iterator.remove();
            }
        }
    }

    public void clear() {
        pending.clear();
        ledgers.clear();
    }

    int activeLedgerCount() {
        return ledgers.size();
    }

    private record PendingDamage(double healthBefore, long gameTick) {
    }

    private static final class VictimLedger {
        private double totalEffectiveDamage;
        private long lastDamageTick;
        private final Map<UUID, Double> guardDamage = new LinkedHashMap<>();
    }
}
