package net.hfstack.rallyguard.api.progression;

import dev.sterner.guardvillagers.common.entity.GuardEntity;

public interface GuardProgressionService {
    long awardXp(GuardEntity guard, long proposedXpMilli);

    boolean recordCombatStat(GuardEntity guard, CombatStat stat);

    boolean refreshAttributes(GuardEntity guard);
}
