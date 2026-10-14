package net.hfstack.rallyguard.progression;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.progression.CombatStat;
import net.hfstack.rallyguard.api.progression.CombatStatContext;
import net.hfstack.rallyguard.api.progression.GuardProgressionEvents;
import net.hfstack.rallyguard.api.progression.LevelChangedContext;
import net.hfstack.rallyguard.api.progression.XpAwardContext;
import net.hfstack.rallyguard.api.progression.XpAwardDecision;
import net.hfstack.rallyguard.api.progression.XpAwardedContext;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.profile.GuardAttributeApplicationCause;
import net.hfstack.rallyguard.profile.GuardAttributeApplier;
import net.hfstack.rallyguard.profile.GuardLevelModel;
import net.hfstack.rallyguard.profile.GuardProfile;
import net.hfstack.rallyguard.profile.GuardProfileReadResult;
import net.hfstack.rallyguard.profile.GuardProfileRepository;

import java.util.Objects;

public final class GuardProgressionService implements net.hfstack.rallyguard.api.progression.GuardProgressionService {
    private static final GuardProgressionService INSTANCE =
            new GuardProgressionService(GuardProfileRepository.instance());

    private final GuardProfileRepository repository;

    private GuardProgressionService(GuardProfileRepository repository) {
        this.repository = repository;
    }

    public static GuardProgressionService instance() {
        return INSTANCE;
    }

    @Override
    public long awardXp(GuardEntity guard, long proposedXpMilli) {
        Objects.requireNonNull(guard, "guard");
        if (!RallyConfig.progressionEnabled()) return 0;
        GuardProfileReadResult current = repository.read(guard);
        if (!(current instanceof GuardProfileReadResult.Available available)) return 0;

        XpAwardDecision decision = GuardProgressionEvents.BEFORE_XP_AWARDED.invoker().evaluate(
                new XpAwardContext(guard, available.profile()),
                Math.max(0, proposedXpMilli)
        );
        if (!(decision instanceof XpAwardDecision.Allow allowed) || allowed.xpMilli() <= 0) return 0;

        ProgressionUpdate update = apply(guard, allowed.xpMilli(), false, false);
        if (!update.saved() || update.awardedXpMilli() <= 0) return 0;
        GuardProfile updated = availableProfile(guard);
        if (updated == null) return update.awardedXpMilli();

        GuardProgressionEvents.AFTER_XP_AWARDED.invoker().onAwarded(
                new XpAwardedContext(guard, updated, update.awardedXpMilli())
        );
        if (update.currentLevel() != update.previousLevel()) {
            GuardProgressionEvents.LEVEL_CHANGED.invoker().onLevelChanged(
                    new LevelChangedContext(guard, updated, update.previousLevel(), update.currentLevel())
            );
        }
        return update.awardedXpMilli();
    }

    @Override
    public boolean recordCombatStat(GuardEntity guard, CombatStat stat) {
        Objects.requireNonNull(guard, "guard");
        Objects.requireNonNull(stat, "stat");
        if (!RallyConfig.progressionEnabled()) return false;
        ProgressionUpdate update = apply(
                guard,
                0,
                stat == CombatStat.KILL,
                stat == CombatStat.ASSIST
        );
        boolean recorded = stat == CombatStat.KILL ? update.killRecorded() : update.assistRecorded();
        if (!update.saved() || !recorded) return false;
        GuardProfile updated = availableProfile(guard);
        if (updated != null) {
            GuardProgressionEvents.COMBAT_STAT_RECORDED.invoker().onRecorded(
                    new CombatStatContext(guard, updated, stat)
            );
        }
        return updated != null;
    }

    @Override
    public boolean refreshAttributes(GuardEntity guard) {
        Objects.requireNonNull(guard, "guard");
        GuardProfile profile = availableProfile(guard);
        if (profile == null) return false;
        GuardAttributeApplier.apply(guard, profile, GuardAttributeApplicationCause.CONFIGURATION_REFRESH);
        return true;
    }

    private ProgressionUpdate apply(
            GuardEntity guard,
            long xpAwardMilli,
            boolean recordKill,
            boolean recordAssist
    ) {
        Objects.requireNonNull(guard, "guard");
        GuardProfileReadResult current = repository.read(guard);
        if (!(current instanceof GuardProfileReadResult.Available available)) {
            return ProgressionUpdate.unavailable();
        }
        GuardProfile previous = available.profile();
        int previousLevel = GuardLevelModel.level(previous.totalXpMilli());
        GuardProfile updated = previous.withProgress(xpAwardMilli, recordKill, recordAssist);
        if (updated.equals(previous)) {
            return new ProgressionUpdate(true, previousLevel, previousLevel, 0, false, false);
        }
        if (!repository.save(guard, updated)) {
            return ProgressionUpdate.unavailable();
        }
        int currentLevel = GuardLevelModel.level(updated.totalXpMilli());
        if (currentLevel != previousLevel) {
            GuardAttributeApplier.apply(guard, updated, GuardAttributeApplicationCause.LEVEL_UP);
        }
        return new ProgressionUpdate(
                true,
                previousLevel,
                currentLevel,
                Math.max(0, updated.totalXpMilli() - previous.totalXpMilli()),
                recordKill,
                recordAssist
        );
    }

    private GuardProfile availableProfile(GuardEntity guard) {
        GuardProfileReadResult result = repository.read(guard);
        return result instanceof GuardProfileReadResult.Available available ? available.profile() : null;
    }
}
