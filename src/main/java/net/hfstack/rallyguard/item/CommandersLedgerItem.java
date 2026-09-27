package net.hfstack.rallyguard.item;

import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class CommandersLedgerItem extends Item {
    public CommandersLedgerItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (world.isClientSide()) {
            try {
                Class<?> hooks = Class.forName("net.hfstack.rallyguard.client.ClientHooks");
                Method m = hooks.getMethod("requestOpenGuardPanel");
                m.invoke(null);
            } catch (Throwable ignored) {
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, TooltipDisplay displayComponent,
                              Consumer<Component> textConsumer, TooltipFlag type) {
        textConsumer.accept(Component.translatable("item.rallyguard.commanders_ledger.tooltip"));
        super.appendHoverText(stack, ctx, displayComponent, textConsumer, type);
    }
}
