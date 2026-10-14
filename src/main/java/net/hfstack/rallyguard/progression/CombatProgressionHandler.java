package net.hfstack.rallyguard.progression;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.api.progression.CombatStat;
import net.hfstack.rallyguard.profile.GuardProfileReadResult;
import net.hfstack.rallyguard.profile.GuardProfileRepository;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CombatProgressionHandler {
    private static final int CLEANUP_INTERVAL_TICKS = 20;
    private static final CombatContributionTracker TRACKER = new CombatContributionTracker();

    private CombatProgressionHandler() {
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) -> {
            if (!RallyConfig.progressionEnabled() || !XpTargetPolicy.isEligible(victim)) {
                TRACKER.remove(victim.getUUID());
                return true;
            }
            if (amount <= 0.0F) {
                TRACKER.cancelPending(victim.getUUID());
                return true;
            }
            TRACKER.beginDamage(victim.getUUID(), victim.getHealth(), serverTick(victim));
            return true;
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((victim, source, baseDamageTaken, damageTaken, blocked) -> {
            if (!RallyConfig.progressionEnabled() || !XpTargetPolicy.isEligible(victim)) {
                TRACKER.remove(victim.getUUID());
                return;
            }
            TRACKER.completeDamage(
                    victim.getUUID(),
                    victim.getHealth(),
                    profiledGuard(source).map(Entity::getUUID),
                    serverTick(victim),
                    RallyConfig.combatTimeoutTicks()
            );
        });

        ServerLivingEntityEvents.AFTER_DEATH.register(CombatProgressionHandler::afterDeath);
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> TRACKER.remove(entity.getUUID()));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % CLEANUP_INTERVAL_TICKS == 0) {
                TRACKER.cleanup(server.getTickCount(), RallyConfig.combatTimeoutTicks());
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> TRACKER.clear());
    }

    private static void afterDeath(LivingEntity victim, DamageSource source) {
        try {
            if (!RallyConfig.progressionEnabled()) {
                TRACKER.remove(victim.getUUID());
                return;
            }
            Optional<XpCategory> category = XpTargetPolicy.category(victim);
            if (category.isEmpty()) {
                TRACKER.remove(victim.getUUID());
                return;
            }

            Optional<UUID> finalGuard = profiledGuard(source).map(Entity::getUUID);
            long tick = serverTick(victim);
            TRACKER.completeDamage(
                    victim.getUUID(),
                    0.0,
                    finalGuard,
                    tick,
                    RallyConfig.combatTimeoutTicks()
            );
            Optional<CombatLedgerSnapshot> finished = TRACKER.finish(victim.getUUID(), finalGuard);
            finished.ifPresent(snapshot -> award(victim, category.orElseThrow(), snapshot));
        } catch (RuntimeException exception) {
            TRACKER.remove(victim.getUUID());
            RallyOfTheGuard.LOGGER.error("Failed to process combat progression for {}", victim.getUUID(), exception);
        }
    }

    private static void award(LivingEntity victim, XpCategory category, CombatLedgerSnapshot snapshot) {
        if (!(victim.level() instanceof ServerLevel level)) return;

        Map<UUID, GuardEntity> loadedGuards = new LinkedHashMap<>();
        Map<UUID, Double> eligibleDamage = new LinkedHashMap<>();
        snapshot.guardDamage().forEach((guardId, damage) -> {
            Entity entity = level.getEntityInAnyDimension(guardId);
            if (entity instanceof GuardEntity guard && guard.isAlive() && hasProfile(guard)) {
                loadedGuards.put(guardId, guard);
                eligibleDamage.put(guardId, damage);
            }
        });
        if (eligibleDamage.isEmpty()) return;

        Optional<UUID> eligibleFinalGuard = snapshot.finalGuard().filter(eligibleDamage::containsKey);
        long budget = XpDistribution.scaledBudgetMilli(category.baseXp(), RallyConfig.xpMultiplier());
        XpShares shares = XpShares.normalize(
                RallyConfig.damageContributionShare(),
                RallyConfig.finalBlowShare()
        );
        Map<UUID, Long> awards = XpDistribution.allocate(
                budget,
                snapshot.totalEffectiveDamage(),
                eligibleDamage,
                eligibleFinalGuard,
                shares
        );

        eligibleDamage.forEach((guardId, damage) -> {
            boolean kill = eligibleFinalGuard.filter(guardId::equals).isPresent();
            boolean assist = !kill && XpDistribution.qualifiesForAssist(
                    damage,
                    snapshot.totalEffectiveDamage(),
                    RallyConfig.assistMinimumDamage(),
                    RallyConfig.assistMinimumShare()
            );
            long xp = awards.getOrDefault(guardId, 0L);
            GuardEntity guard = loadedGuards.get(guardId);
            if (xp > 0) GuardProgressionService.instance().awardXp(guard, xp);
            if (kill) GuardProgressionService.instance().recordCombatStat(guard, CombatStat.KILL);
            if (assist) GuardProgressionService.instance().recordCombatStat(guard, CombatStat.ASSIST);
        });
    }

    private static Optional<GuardEntity> profiledGuard(DamageSource source) {
        return GuardDamageResolver.resolve(source).filter(CombatProgressionHandler::hasProfile);
    }

    private static boolean hasProfile(GuardEntity guard) {
        return GuardProfileRepository.instance().read(guard) instanceof GuardProfileReadResult.Available;
    }

    private static long serverTick(LivingEntity entity) {
        return entity.level() instanceof ServerLevel level ? level.getServer().getTickCount() : 0L;
    }
}
