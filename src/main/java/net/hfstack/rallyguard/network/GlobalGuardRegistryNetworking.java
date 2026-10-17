package net.hfstack.rallyguard.network;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.network.payload.GlobalGuardRegistryS2CPayload;
import net.hfstack.rallyguard.network.payload.OpenGlobalGuardRegistryC2SPayload;
import net.hfstack.rallyguard.registry.GlobalGuardRecord;
import net.hfstack.rallyguard.registry.GlobalGuardRegistry;
import net.hfstack.rallyguard.registry.GlobalGuardRegistryRead;
import net.hfstack.rallyguard.registry.GlobalGuardRegistryRepository;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Comparator;
import java.util.List;

public final class GlobalGuardRegistryNetworking {
    private static final int PAGE_SIZE = 8;
    private static boolean registered;

    private GlobalGuardRegistryNetworking() {
    }

    public static synchronized void registerServer() {
        if (registered) return;
        registered = true;
        ServerPlayNetworking.registerGlobalReceiver(OpenGlobalGuardRegistryC2SPayload.ID, (payload, context) ->
                context.server().execute(() -> sendPage(context.player(), payload.page()))
        );
    }

    private static void sendPage(ServerPlayer player, int requestedPage) {
        GlobalGuardRegistryRead result = GlobalGuardRegistryRepository.instance().readAndRepair(player);
        GlobalGuardRegistry registry = result instanceof GlobalGuardRegistryRead.Available available
                ? available.registry() : GlobalGuardRegistry.empty();
        List<GlobalGuardRecord> sorted = registry.guards().stream()
                .sorted(Comparator.comparing(GlobalGuardRecord::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        int totalPages = Math.max(1, (sorted.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.max(0, Math.min(requestedPage, totalPages - 1));
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, sorted.size());
        List<GlobalGuardRegistryS2CPayload.Entry> entries = sorted.subList(start, end).stream()
                .map(record -> snapshot(player, record))
                .toList();
        ServerPlayNetworking.send(player, new GlobalGuardRegistryS2CPayload(
                page, totalPages, sorted.size(), entries
        ));
    }

    private static GlobalGuardRegistryS2CPayload.Entry snapshot(ServerPlayer player, GlobalGuardRecord record) {
        Entity loadedEntity = null;
        for (ServerLevel level : player.level().getServer().getAllLevels()) {
            Entity candidate = level.getEntity(record.guardId());
            if (candidate != null) {
                loadedEntity = candidate;
                break;
            }
        }
        boolean loaded = loadedEntity instanceof GuardEntity guard
                && GuardOwnership.isOwnedBy(guard, player.getUUID());
        int distance = -1;
        if (record.dimensionId().equals(player.level().dimension().identifier().toString())) {
            distance = (int)Math.round(Math.sqrt(player.blockPosition().distSqr(record.lastPosition())));
        }
        return new GlobalGuardRegistryS2CPayload.Entry(
                record.guardId(), record.name(), record.dimensionId(),
                record.lastPosition().getX(), record.lastPosition().getY(), record.lastPosition().getZ(),
                record.lastSeenGameTime(), record.level(), record.rankId(), record.captain(),
                record.squadName().orElse(""), record.orderStatus(), loaded, distance
        );
    }
}
