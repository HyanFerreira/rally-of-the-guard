package net.hfstack.rallyguard.interaction;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class GuardInteractionLocks {
    private static final InteractionLeaseRegistry<UUID, UUID> LEASES = new InteractionLeaseRegistry<>();
    private static final Map<UUID, GuardState> GUARDS = new HashMap<>();

    private GuardInteractionLocks() {
    }

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> releaseAll(handler.getPlayer().getUUID()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> clear());
    }

    public static void acquire(ServerPlayer player, GuardEntity guard) {
        UUID guardId = guard.getUUID();
        if (LEASES.acquire(guardId, player.getUUID())) {
            GUARDS.put(guardId, new GuardState(guard, guard.interacting));
        }
        guard.interacting = true;
        guard.getNavigation().stop();
    }

    public static void release(UUID playerId, UUID guardId) {
        if (LEASES.release(guardId, playerId)) restore(guardId);
    }

    public static void releaseAll(UUID playerId) {
        Set<UUID> releasedGuards = LEASES.releaseAll(playerId);
        releasedGuards.forEach(GuardInteractionLocks::restore);
    }

    private static void clear() {
        Set<UUID> heldGuards = LEASES.clear();
        heldGuards.forEach(GuardInteractionLocks::restore);
        GUARDS.clear();
    }

    private static void restore(UUID guardId) {
        GuardState state = GUARDS.remove(guardId);
        if (state != null) state.guard().interacting = state.previouslyInteracting();
    }

    private record GuardState(GuardEntity guard, boolean previouslyInteracting) {
    }
}
