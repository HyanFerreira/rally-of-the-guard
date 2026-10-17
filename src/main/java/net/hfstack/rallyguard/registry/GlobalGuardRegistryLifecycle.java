package net.hfstack.rallyguard.registry;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.profile.GuardProfileReadResult;
import net.hfstack.rallyguard.profile.GuardProfileRepository;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;

public final class GlobalGuardRegistryLifecycle {
    private static final Identifier GUARD_TYPE = Identifier.fromNamespaceAndPath("guardvillagers", "guard");
    private static final int RECONCILE_INTERVAL = 100;
    private static boolean registered;

    private GlobalGuardRegistryLifecycle() {
    }

    public static synchronized void register() {
        if (registered) return;
        registered = true;

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof GuardEntity guard) update(guard, level.getServer());
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof GuardEntity guard && guard.isAlive()) update(guard, level.getServer());
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof GuardEntity guard && guard.level() instanceof ServerLevel level) {
                remove(guard, level.getServer());
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> reconcile(handler.getPlayer(), server));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % RECONCILE_INTERVAL == 0) reconcileLoaded(server);
        });
    }

    public static void update(GuardEntity guard, MinecraftServer server) {
        UUID ownerId = GuardOwnership.getOwner(guard);
        if (ownerId == null) return;
        ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
        if (owner == null || !(guard.level() instanceof ServerLevel level)) return;
        try {
            GlobalGuardRegistryRepository.instance().upsert(owner, snapshot(owner, guard, level));
        } catch (RuntimeException exception) {
            RallyOfTheGuard.LOGGER.error("Failed to update global registry for guard {}", guard.getUUID(), exception);
        }
    }

    public static void remove(GuardEntity guard, MinecraftServer server) {
        UUID ownerId = GuardOwnership.getOwner(guard);
        if (ownerId == null) return;
        ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
        if (owner != null) GlobalGuardRegistryRepository.instance().remove(owner, guard.getUUID());
    }

    private static GlobalGuardRecord snapshot(ServerPlayer owner, GuardEntity guard, ServerLevel level) {
        int profileLevel = 0;
        String rankId = "";
        boolean captain = false;
        GuardProfileReadResult profile = GuardProfileRepository.instance().read(guard);
        if (profile instanceof GuardProfileReadResult.Available available) {
            profileLevel = available.profile().level();
            rankId = available.profile().rank().name().toLowerCase(java.util.Locale.ROOT);
            captain = available.profile().captain();
        }
        Optional<String> squadName = RallyGuardApi.squads().find(owner, guard).squad().map(squad -> squad.name());
        return new GlobalGuardRecord(
                guard.getUUID(),
                guard.getName().getString(),
                level.dimension().identifier().toString(),
                guard.blockPosition(),
                level.getGameTime(),
                profileLevel,
                rankId,
                captain,
                squadName,
                GuardOrders.statusOf(guard)
        );
    }

    public static void reconcile(ServerPlayer owner, MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : loadedGuards(level)) {
                if (entity instanceof GuardEntity guard && GuardOwnership.isOwnedBy(guard, owner.getUUID())) {
                    update(guard, server);
                }
            }
        }
    }

    private static void reconcileLoaded(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : loadedGuards(level)) {
                if (entity instanceof GuardEntity guard) update(guard, server);
            }
        }
    }

    private static java.util.List<? extends Entity> loadedGuards(ServerLevel level) {
        return level.getEntities(BuiltInRegistries.ENTITY_TYPE.getValue(GUARD_TYPE), Entity::isAlive);
    }
}
