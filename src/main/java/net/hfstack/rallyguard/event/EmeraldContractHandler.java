package net.hfstack.rallyguard.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class EmeraldContractHandler {

    private static final EntityType<?> GUARD_TYPE = Registries.ENTITY_TYPE.get(Identifier.of("guardvillagers", "guard"));

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((Entity entity, ServerWorld world) -> {
            if (!(entity instanceof ItemEntity emerald)) return;
            if (emerald.getStack().getItem() != Items.EMERALD) return;

            // Aguarda 1 tick para garantir que o item "caiu no chão"
            world.getServer().execute(() -> processEmerald(emerald, world));
        });
    }

    private static void processEmerald(ItemEntity emerald, ServerWorld world) {
        if (emerald.isRemoved()) return;

        List<LivingEntity> nearbyGuards = world.getEntitiesByClass(LivingEntity.class,
                emerald.getBoundingBox().expand(1.5),
                entity -> entity.getType() == GUARD_TYPE && !entity.getCommandTags().contains("contratado"));

        if (nearbyGuards.isEmpty()) return;

        LivingEntity guard = nearbyGuards.get(0);
        PlayerEntity player = world.getClosestPlayer(emerald, 5);
        if (player == null) return;

        guard.addCommandTag("contratado");

        NbtCompound nbt = guard.writeNbt(new NbtCompound());
        nbt.putUuid("Owner", player.getUuid());
        guard.readNbt(nbt);

        guard.setCustomName(Text.literal(guard.getName().getString())
                .styled(style -> style.withColor(0xFFD700)));
        guard.setCustomNameVisible(true);

        player.sendMessage(Text.literal("O Guarda " + guard.getName().getString() + " aceitou seu contrato!"), false);
        emerald.discard();
    }
}
