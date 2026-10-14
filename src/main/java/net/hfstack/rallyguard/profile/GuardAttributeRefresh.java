package net.hfstack.rallyguard.profile;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.Objects;

public final class GuardAttributeRefresh {
    private GuardAttributeRefresh() {
    }

    public static boolean refresh(GuardEntity guard) {
        Objects.requireNonNull(guard, "guard");
        GuardProfileReadResult current = GuardProfileRepository.instance().read(guard);
        if (!(current instanceof GuardProfileReadResult.Available available)) {
            return false;
        }
        GuardAttributeApplier.apply(
                guard,
                available.profile(),
                GuardAttributeApplicationCause.CONFIGURATION_REFRESH
        );
        return true;
    }

    public static int refreshLoadedGuards(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        int refreshed = 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof GuardEntity guard && refresh(guard)) {
                    refreshed++;
                }
            }
        }
        return refreshed;
    }
}
