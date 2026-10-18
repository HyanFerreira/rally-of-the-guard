package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.hfstack.rallyguard.component.ModComponents;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.effect.ModEffects;
import net.hfstack.rallyguard.formation.RallyFormation;
import net.hfstack.rallyguard.item.ModItems;
import net.hfstack.rallyguard.order.GuardOrders;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class RallyLifecycle {
    private static final Identifier GUARD_ID = Identifier.fromNamespaceAndPath("guardvillagers", "guard");
    private static final int MAX_TRACKED_COMMANDERS = 1024;
    private static final Map<UUID, String> ACTIVE_COMMANDERS = new LinkedHashMap<>();
    private static boolean registered;

    private RallyLifecycle() {
    }

    public static synchronized void register() {
        if (registered) return;
        registered = true;

        ServerTickEvents.END_SERVER_TICK.register(RallyLifecycle::reconcile);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (!ACTIVE_COMMANDERS.containsKey(player.getUUID())) {
                cleanup(server, player.getUUID(), player);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            cleanup(server, player.getUUID(), player);
        });
    }

    public static void start(ServerPlayer player, RallyFormation formation, ItemStack initiatingScroll) {
        MinecraftServer server = player.level().getServer();
        UUID commanderId = player.getUUID();
        if (ACTIVE_COMMANDERS.containsKey(commanderId)) {
            cleanup(server, commanderId, player);
        } else if (ACTIVE_COMMANDERS.size() >= MAX_TRACKED_COMMANDERS) {
            UUID oldest = ACTIVE_COMMANDERS.keySet().iterator().next();
            stop(server, oldest);
        }

        ACTIVE_COMMANDERS.put(commanderId, dimensionId(player));
        player.addEffect(new MobEffectInstance(
                ModEffects.RALLY_COMMANDER,
                RallyConfig.rallyEffectTimerSeconds() * 20,
                0,
                false,
                false,
                true
        ));
        RallyFormationTicker.startRally(player, formation);
        initiatingScroll.set(ModComponents.ACTIVE, true);
    }

    public static boolean isActive(UUID commanderId) {
        return ACTIVE_COMMANDERS.containsKey(commanderId);
    }

    public static void stop(MinecraftServer server, UUID commanderId) {
        cleanup(server, commanderId, server.getPlayerList().getPlayer(commanderId));
    }

    public static void reconcile(MinecraftServer server) {
        for (Map.Entry<UUID, String> entry : new ArrayList<>(ACTIVE_COMMANDERS.entrySet())) {
            UUID commanderId = entry.getKey();
            ServerPlayer player = server.getPlayerList().getPlayer(commanderId);
            boolean invalid = player == null
                    || !player.isAlive()
                    || !player.hasEffect(ModEffects.RALLY_COMMANDER)
                    || !entry.getValue().equals(dimensionId(player))
                    || !RallyFormationTicker.hasSession(commanderId);
            if (invalid) cleanup(server, commanderId, player);
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!ACTIVE_COMMANDERS.containsKey(player.getUUID())) {
                clearActiveScrolls(player);
            }
        }
    }

    private static void cleanup(MinecraftServer server, UUID commanderId, ServerPlayer player) {
        ACTIVE_COMMANDERS.remove(commanderId);
        RallyFormationTicker.stopRally(commanderId);
        if (player != null) {
            player.removeEffect(ModEffects.RALLY_COMMANDER);
            clearActiveScrolls(player);
        }

        EntityType<?> guardType = BuiltInRegistries.ENTITY_TYPE.getValue(GUARD_ID);
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getEntities(guardType, Entity::isAlive)) {
                if (!(entity instanceof GuardEntity guard)) continue;
                if (!RallyCleanupPolicy.shouldRelease(
                        GuardOwnership.isOwnedBy(guard, commanderId),
                        GuardOrders.isRallied(guard)
                )) continue;
                guard.setFollowing(false);
                GuardOrders.setRallied(guard, false);
            }
        }
    }

    private static void clearActiveScrolls(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.SCROLL_OF_RALLYING) && stack.getOrDefault(ModComponents.ACTIVE, false)) {
                stack.set(ModComponents.ACTIVE, false);
            }
        }
    }

    private static String dimensionId(ServerPlayer player) {
        return player.level().dimension().identifier().toString();
    }
}
