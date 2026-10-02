package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.GuardVillagers;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;

public final class GuardVillagersSpawnEggFix {
    private GuardVillagersSpawnEggFix() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            patch(player.getItemInHand(hand));
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            patch(player.getItemInHand(hand));
            return InteractionResult.PASS;
        });
    }

    private static void patch(ItemStack stack) {
        if (!stack.is(GuardVillagers.GUARD_SPAWN_EGG)) {
            return;
        }

        var entityData = stack.get(DataComponents.ENTITY_DATA);
        if (entityData != null && entityData.type() == GuardVillagers.GUARD_VILLAGER) {
            return;
        }

        stack.set(
                DataComponents.ENTITY_DATA,
                TypedEntityData.of((EntityType<?>) GuardVillagers.GUARD_VILLAGER, new CompoundTag())
        );
    }
}
