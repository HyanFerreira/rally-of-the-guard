package net.hfstack.rallyguard.contract;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
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
        return e != null && e.getType() == Registries.ENTITY_TYPE.get(GUARD_ID);
    }

    public static UUID getOwner(Entity guard) {
        NbtCompound n = new NbtCompound();
        guard.writeNbt(n);
        if (n.containsUuid("Owner")) return n.getUuid("Owner");
        if (n.containsUuid("rallyguard:Owner")) return n.getUuid("rallyguard:Owner");
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
        // Grava o dono no NBT (chave reconhecida pelo GuardVillagers)
        NbtCompound n = new NbtCompound();
        guard.writeNbt(n);
        n.putUuid("Owner", player.getUuid());
        n.putUuid("rallyguard:Owner", player.getUuid()); // redundância para nosso mod
        guard.readNbt(n);

        // Aplica nome dourado (preserva o texto atual do nome)
        applyGoldName(guard);

        // Mensagem de apresentação do guarda (em chat, não overlay)
        String display = guard.getName().getString();
        player.sendMessage(Text.translatable("message.rallyguard.guard_presenting", display), false);
    }

    private static void applyGoldName(Entity guard) {
        if (!(guard instanceof LivingEntity le)) return;

        // Usa o nome atual (customizado ou padrão), só aplicando a cor
        String base = le.getName().getString();
        Text golden = Text.literal(base).styled(s -> s.withColor(GOLD));
        le.setCustomName(golden);
        le.setCustomNameVisible(true);
    }
}
