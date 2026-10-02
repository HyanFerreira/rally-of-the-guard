package net.hfstack.rallyguard.contract;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public final class GuardOwnership {
    private GuardOwnership() {
    }

    private static final Identifier GUARD_ID = Identifier.fromNamespaceAndPath("guardvillagers", "guard");
    private static final int GOLD = 0xFFD700;

    public static boolean isGuard(Entity e) {
        return e instanceof GuardEntity || (e != null && e.getType() == BuiltInRegistries.ENTITY_TYPE.getValue(GUARD_ID));
    }

    public static UUID getOwner(Entity guard) {
        if (guard instanceof GuardEntity gv) return gv.getOwnerId();
        return null;
    }

    public static boolean hasOwner(Entity guard) {
        return getOwner(guard) != null;
    }

    public static boolean isOwnedBy(Entity guard, UUID player) {
        UUID owner = getOwner(guard);
        return owner != null && owner.equals(player);
    }

    public static void setOwner(Entity guard, ServerPlayer player) {
        if (guard instanceof GuardEntity gv) {
            gv.setOwnerId(player.getUUID());
        }

        applyGoldName(guard);

        String display = guard.getName().getString();
        player.sendSystemMessage(Component.translatable("message.rallyguard.guard_presenting", display));
    }

    public static void clearOwner(Entity guard) {
        if (guard instanceof GuardEntity gv) {
            gv.setOwnerId(null);
        }
    }

    private static void applyGoldName(Entity guard) {
        if (!(guard instanceof LivingEntity le)) return;

        String base = le.getName().getString();
        Component golden = Component.literal(base).withStyle(s -> s.withColor(GOLD));
        le.setCustomName(golden);
        le.setCustomNameVisible(true);
    }
}
