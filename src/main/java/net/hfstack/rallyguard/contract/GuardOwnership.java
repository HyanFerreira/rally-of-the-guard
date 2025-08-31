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

    public static boolean isGuard(Entity e) {
        return e != null && e.getType() == Registries.ENTITY_TYPE.get(GUARD_ID);
    }

    public static UUID getOwner(Entity guard) {
        NbtCompound nbt = new NbtCompound();
        guard.writeNbt(nbt);
        if (nbt.containsUuid("Owner")) return nbt.getUuid("Owner");
        if (nbt.containsUuid("rallyguard:Owner")) return nbt.getUuid("rallyguard:Owner");
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
        NbtCompound nbt = new NbtCompound();
        guard.writeNbt(nbt);
        nbt.putUuid("Owner", player.getUuid());            // chave reconhecida pelo GuardVillagers
        nbt.putUuid("rallyguard:Owner", player.getUuid()); // redundância, se quiser consultar
        guard.readNbt(nbt);

        String name = (guard instanceof LivingEntity le && le.hasCustomName())
                ? le.getCustomName().getString() : "Guarda";
        player.sendMessage(Text.literal("O " + name + " agora é seu contratado."), false);
    }
}
