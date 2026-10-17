package net.hfstack.rallyguard.raid;

import java.util.Optional;

public record RaidReportSummary(
        int guardsDeployed,
        int enemiesDefeated,
        int guardsLost,
        Optional<String> mvpName,
        int mvpKills,
        boolean incomplete
) {
    public RaidReportSummary {
        if (guardsDeployed < 0 || enemiesDefeated < 0 || guardsLost < 0 || mvpKills < 0) {
            throw new IllegalArgumentException("Raid report counts cannot be negative");
        }
        mvpName = mvpName == null ? Optional.empty() : mvpName;
        if (mvpName.isEmpty() && mvpKills != 0) throw new IllegalArgumentException("MVP kills require a name");
    }
}
