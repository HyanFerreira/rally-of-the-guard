package net.hfstack.rallyguard.raid;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class RaidReportTracker {
    private static boolean registered;

    private RaidReportTracker() {
    }

    public static synchronized void register() {
        if (registered) return;
        registered = true;
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof GuardEntity guard) RaidReports.recordGuardLost(guard);
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof GuardEntity guard) RaidDefenseOrders.releaseIfFinished(guard, level);
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof GuardEntity guard && guard.isAlive()) RaidReports.recordGuardUnload(guard);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> RaidReports.clear());
    }
}
