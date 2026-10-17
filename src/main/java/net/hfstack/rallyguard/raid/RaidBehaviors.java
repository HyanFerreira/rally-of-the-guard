package net.hfstack.rallyguard.raid;

import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.Objects;

public final class RaidBehaviors {
    private RaidBehaviors() {
    }

    public static RaidBehavior get(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        String stored = player.getAttached(RaidBehaviorAttachments.BEHAVIOR);
        if (stored == null) return RaidBehavior.NORMAL;
        try {
            return RaidBehavior.valueOf(stored.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return RaidBehavior.NORMAL;
        }
    }

    public static void set(ServerPlayer player, RaidBehavior behavior) {
        Objects.requireNonNull(player, "player");
        player.setAttached(RaidBehaviorAttachments.BEHAVIOR, Objects.requireNonNull(behavior, "behavior").name());
    }
}
