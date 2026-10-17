package net.hfstack.rallyguard.raid;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class RaidReportSession {
    private final int guardsDeployed;
    private final long expiresAtTick;
    private int enemiesDefeated;
    private int guardsLost;
    private boolean incomplete;
    private final Map<UUID, KillCredit> kills = new LinkedHashMap<>();

    RaidReportSession(int guardsDeployed, long expiresAtTick) {
        if (guardsDeployed < 0) throw new IllegalArgumentException("guardsDeployed cannot be negative");
        this.guardsDeployed = guardsDeployed;
        this.expiresAtTick = expiresAtTick;
    }

    long expiresAtTick() {
        return expiresAtTick;
    }

    void recordEnemyDefeated() {
        enemiesDefeated++;
    }

    void recordKill(UUID guardId, String guardName) {
        kills.compute(guardId, (ignored, current) -> current == null
                ? new KillCredit(guardName, 1)
                : new KillCredit(current.name(), current.kills() + 1));
    }

    void recordGuardLost() {
        guardsLost++;
    }

    void markIncomplete() {
        incomplete = true;
    }

    RaidReportSummary summary(boolean forcedIncomplete) {
        Optional<KillCredit> mvp = kills.entrySet().stream()
                .sorted(Map.Entry.<UUID, KillCredit>comparingByValue(
                        Comparator.comparingInt(KillCredit::kills).reversed()
                                .thenComparing(KillCredit::name)
                ).thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .findFirst();
        return new RaidReportSummary(
                guardsDeployed,
                enemiesDefeated,
                guardsLost,
                mvp.map(KillCredit::name),
                mvp.map(KillCredit::kills).orElse(0),
                incomplete || forcedIncomplete
        );
    }

    private record KillCredit(String name, int kills) {
    }
}
