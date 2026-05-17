package net.hfstack.rallyguard.contract;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.UUID;

public final class GuardOwnership {
    private GuardOwnership() {
    }

    private static final Identifier GUARD_ID = Identifier.of("guardvillagers", "guard");
    private static final int GOLD = 0xFFD700;

    public static boolean isGuard(Entity e) {
        return e instanceof GuardEntity || (e != null && e.getType() == Registries.ENTITY_TYPE.get(GUARD_ID));
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

    public static void setOwner(Entity guard, ServerPlayerEntity player) {
        if (guard instanceof GuardEntity gv) {
            gv.setOwnerId(player.getUuid());
        }

        applyGoldName(guard);

        String display = guard.getName().getString();
        player.sendMessage(Text.translatable("message.rallyguard.guard_presenting", display), false);
    }

    private static void applyGoldName(Entity guard) {
        if (!(guard instanceof LivingEntity le)) return;

        String base = le.getName().getString();
        Text golden = Text.literal(base).styled(s -> s.withColor(GOLD));
        le.setCustomName(golden);
        le.setCustomNameVisible(true);
    }
}
