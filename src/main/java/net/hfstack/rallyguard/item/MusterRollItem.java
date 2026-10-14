package net.hfstack.rallyguard.item;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.muster.MusterRollSessions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public final class MusterRollItem extends Item {
    public MusterRollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity target,
            InteractionHand hand
    ) {
        if (!(target instanceof GuardEntity guard)) return InteractionResult.PASS;
        if (player.level().isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        if (!MusterRollSessions.open(serverPlayer, guard)) {
            serverPlayer.sendOverlayMessage(Component.translatable("gui.rallyguard.muster.invalid"));
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> textConsumer,
            TooltipFlag flag
    ) {
        textConsumer.accept(Component.translatable("item.rallyguard.muster_roll.tooltip"));
        super.appendHoverText(stack, context, display, textConsumer, flag);
    }
}
