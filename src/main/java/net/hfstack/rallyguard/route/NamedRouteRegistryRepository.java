package net.hfstack.rallyguard.route;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public final class NamedRouteRegistryRepository {
    private static final NamedRouteRegistryRepository INSTANCE = new NamedRouteRegistryRepository();

    private NamedRouteRegistryRepository() {
    }

    public static NamedRouteRegistryRepository instance() {
        return INSTANCE;
    }

    public NamedRouteRegistryReadResult read(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        CompoundTag stored = player.getAttached(NamedRouteAttachments.REGISTRY);
        return NamedRouteRegistryCodec.decode(stored);
    }

    public NamedRouteRegistryReadResult readAndRepair(ServerPlayer player) {
        NamedRouteRegistryReadResult result = read(player);
        if (result instanceof NamedRouteRegistryReadResult.Absent) {
            NamedRouteRegistry empty = NamedRouteRegistry.empty();
            player.setAttached(NamedRouteAttachments.REGISTRY, NamedRouteRegistryCodec.encode(empty));
            return new NamedRouteRegistryReadResult.Available(empty, false);
        }
        if (NamedRoutePersistencePolicy.shouldCanonicalize(result)) {
            NamedRouteRegistryReadResult.Available available = (NamedRouteRegistryReadResult.Available) result;
            player.setAttached(NamedRouteAttachments.REGISTRY, NamedRouteRegistryCodec.encode(available.registry()));
            return new NamedRouteRegistryReadResult.Available(available.registry(), false);
        }
        return result;
    }

    public boolean save(ServerPlayer player, NamedRouteRegistry registry) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(registry, "registry");
        if (!NamedRoutePersistencePolicy.canWrite(read(player))) return false;
        player.setAttached(NamedRouteAttachments.REGISTRY, NamedRouteRegistryCodec.encode(registry));
        return true;
    }
}
