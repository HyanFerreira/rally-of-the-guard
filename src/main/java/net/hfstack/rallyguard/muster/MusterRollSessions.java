package net.hfstack.rallyguard.muster;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hfstack.rallyguard.item.ModItems;
import net.hfstack.rallyguard.interaction.GuardInteractionLocks;
import net.hfstack.rallyguard.network.payload.CloseMusterRollS2CPayload;
import net.hfstack.rallyguard.network.payload.MusterRollSnapshotS2CPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class MusterRollSessions {
    public static final double MAX_DISTANCE = 8.0;
    private static final int UPDATE_INTERVAL_TICKS = 20;
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private MusterRollSessions() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % UPDATE_INTERVAL_TICKS == 0) tick(server);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SESSIONS.clear());
    }

    public static boolean open(ServerPlayer player, GuardEntity guard) {
        if (!isValid(player, guard)) return false;
        Optional<GuardProfileSnapshot> snapshot = GuardProfileSnapshotFactory.create(player, guard);
        if (snapshot.isEmpty()) return false;
        GuardProfileSnapshot value = snapshot.orElseThrow();
        close(player);
        GuardInteractionLocks.acquire(player, guard);
        SESSIONS.put(player.getUUID(), new Session(guard.getUUID(), value));
        ServerPlayNetworking.send(player, new MusterRollSnapshotS2CPayload(true, value));
        return true;
    }

    public static void close(ServerPlayer player) {
        Session removed = SESSIONS.remove(player.getUUID());
        if (removed != null) {
            GuardInteractionLocks.release(player.getUUID(), removed.guardId());
        }
    }

    private static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Session>> iterator = SESSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Session> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            GuardEntity guard = player == null ? null : findGuard(player, entry.getValue().guardId());
            if (player == null || guard == null || !isValid(player, guard)) {
                if (player != null) ServerPlayNetworking.send(player, new CloseMusterRollS2CPayload());
                GuardInteractionLocks.release(entry.getKey(), entry.getValue().guardId());
                iterator.remove();
                continue;
            }

            Optional<GuardProfileSnapshot> current = GuardProfileSnapshotFactory.create(player, guard);
            if (current.isEmpty()) {
                ServerPlayNetworking.send(player, new CloseMusterRollS2CPayload());
                GuardInteractionLocks.release(entry.getKey(), entry.getValue().guardId());
                iterator.remove();
                continue;
            }
            GuardProfileSnapshot snapshot = current.orElseThrow();
            if (!snapshot.equals(entry.getValue().snapshot())) {
                entry.setValue(new Session(guard.getUUID(), snapshot));
                ServerPlayNetworking.send(player, new MusterRollSnapshotS2CPayload(false, snapshot));
            }
        }
    }

    private static GuardEntity findGuard(ServerPlayer player, UUID guardId) {
        Entity entity = player.level().getEntity(guardId);
        return entity instanceof GuardEntity guard ? guard : null;
    }

    private static boolean isValid(ServerPlayer player, GuardEntity guard) {
        return guard.isAlive()
                && !guard.isRemoved()
                && guard.level() == player.level()
                && player.distanceToSqr(guard) <= MAX_DISTANCE * MAX_DISTANCE
                && holdsMusterRoll(player);
    }

    private static boolean holdsMusterRoll(ServerPlayer player) {
        return player.getMainHandItem().is(ModItems.MUSTER_ROLL)
                || player.getOffhandItem().is(ModItems.MUSTER_ROLL);
    }

    private record Session(UUID guardId, GuardProfileSnapshot snapshot) {
    }
}
