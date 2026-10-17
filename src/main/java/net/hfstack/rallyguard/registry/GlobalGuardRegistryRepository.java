package net.hfstack.rallyguard.registry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public final class GlobalGuardRegistryRepository {
    private static final GlobalGuardRegistryRepository INSTANCE = new GlobalGuardRegistryRepository();

    private GlobalGuardRegistryRepository() {
    }

    public static GlobalGuardRegistryRepository instance() {
        return INSTANCE;
    }

    public GlobalGuardRegistryRead read(ServerPlayer player) {
        CompoundTag stored = Objects.requireNonNull(player, "player")
                .getAttached(GlobalGuardRegistryAttachments.REGISTRY);
        return GlobalGuardRegistryCodec.decode(stored);
    }

    public GlobalGuardRegistryRead readAndRepair(ServerPlayer player) {
        GlobalGuardRegistryRead result = read(player);
        if (result instanceof GlobalGuardRegistryRead.FutureSchema) return result;
        if (result instanceof GlobalGuardRegistryRead.Available available && !available.repaired()) return result;
        GlobalGuardRegistry registry = result instanceof GlobalGuardRegistryRead.Available available
                ? available.registry() : GlobalGuardRegistry.empty();
        player.setAttached(GlobalGuardRegistryAttachments.REGISTRY, GlobalGuardRegistryCodec.encode(registry));
        return new GlobalGuardRegistryRead.Available(registry, false);
    }

    public boolean save(ServerPlayer player, GlobalGuardRegistry registry) {
        if (read(player) instanceof GlobalGuardRegistryRead.FutureSchema) return false;
        player.setAttached(GlobalGuardRegistryAttachments.REGISTRY, GlobalGuardRegistryCodec.encode(registry));
        return true;
    }

    public boolean upsert(ServerPlayer player, GlobalGuardRecord record) {
        GlobalGuardRegistryRead current = readAndRepair(player);
        if (!(current instanceof GlobalGuardRegistryRead.Available available)) return false;
        return save(player, GlobalGuardRegistryEditor.upsert(available.registry(), record));
    }

    public boolean remove(ServerPlayer player, java.util.UUID guardId) {
        GlobalGuardRegistryRead current = readAndRepair(player);
        if (!(current instanceof GlobalGuardRegistryRead.Available available)) return false;
        GlobalGuardRegistry updated = GlobalGuardRegistryEditor.remove(available.registry(), guardId);
        return updated == available.registry() || save(player, updated);
    }
}
