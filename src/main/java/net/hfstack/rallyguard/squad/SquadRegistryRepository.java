package net.hfstack.rallyguard.squad;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public final class SquadRegistryRepository {
    private static final SquadRegistryRepository INSTANCE = new SquadRegistryRepository();

    private SquadRegistryRepository() {
    }

    public static SquadRegistryRepository instance() {
        return INSTANCE;
    }

    public SquadRegistryReadResult read(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        CompoundTag stored = player.getAttached(SquadAttachments.REGISTRY);
        return SquadRegistryCodec.decode(stored);
    }

    public SquadRegistryReadResult readAndRepair(ServerPlayer player) {
        SquadRegistryReadResult result = read(player);
        if (result instanceof SquadRegistryReadResult.Absent) {
            SquadRegistry empty = SquadRegistry.empty();
            player.setAttached(SquadAttachments.REGISTRY, SquadRegistryCodec.encode(empty));
            return new SquadRegistryReadResult.Available(empty, false);
        }
        if (SquadPersistencePolicy.shouldCanonicalize(result)) {
            SquadRegistryReadResult.Available available = (SquadRegistryReadResult.Available) result;
            player.setAttached(SquadAttachments.REGISTRY, SquadRegistryCodec.encode(available.registry()));
            return new SquadRegistryReadResult.Available(available.registry(), false);
        }
        return result;
    }

    public boolean save(ServerPlayer player, SquadRegistry registry) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(registry, "registry");
        if (!SquadPersistencePolicy.canWrite(read(player))) return false;
        player.setAttached(SquadAttachments.REGISTRY, SquadRegistryCodec.encode(registry));
        return true;
    }
}
