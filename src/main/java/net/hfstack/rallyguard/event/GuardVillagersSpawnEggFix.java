package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.GuardVillagers;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;

public final class GuardVillagersSpawnEggFix {
    private GuardVillagersSpawnEggFix() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            patch(player.getStackInHand(hand));
            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            patch(player.getStackInHand(hand));
            return ActionResult.PASS;
        });
    }

    private static void patch(ItemStack stack) {
        if (!stack.isOf(GuardVillagers.GUARD_SPAWN_EGG)) {
            return;
        }

        var entityData = stack.get(DataComponentTypes.ENTITY_DATA);
        if (entityData != null && entityData.getType() == GuardVillagers.GUARD_VILLAGER) {
            return;
        }

        stack.set(
                DataComponentTypes.ENTITY_DATA,
                TypedEntityData.create((EntityType<?>) GuardVillagers.GUARD_VILLAGER, new NbtCompound())
        );
    }
}
