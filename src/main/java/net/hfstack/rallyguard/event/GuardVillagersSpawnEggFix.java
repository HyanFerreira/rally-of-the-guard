package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.GuardVillagers;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

public final class GuardVillagersSpawnEggFix {
    private GuardVillagersSpawnEggFix() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            patch(player.getStackInHand(hand));
            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            patch(stack);
            return TypedActionResult.pass(stack);
        });
    }

    private static void patch(ItemStack stack) {
        if (!stack.isOf(GuardVillagers.GUARD_SPAWN_EGG)) {
            return;
        }

        var entityData = stack.get(DataComponentTypes.ENTITY_DATA);
        Identifier guardId = Identifier.of("guardvillagers", "guard");
        if (entityData != null && guardId.toString().equals(entityData.getNbt().getString("id"))) {
            return;
        }

        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", guardId.toString());
        stack.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(nbt));
    }
}
