package net.hfstack.rallyguard.item;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class CommandersLedgerItem extends Item {
    public CommandersLedgerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) {
            try {
                Class<?> hooks = Class.forName("net.hfstack.rallyguard.client.ClientHooks");
                Method m = hooks.getMethod("requestOpenGuardPanel");
                m.invoke(null);
            } catch (Throwable ignored) {
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext ctx, TooltipDisplayComponent displayComponent,
                              Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(Text.translatable("item.rallyguard.commanders_ledger.tooltip"));
        super.appendTooltip(stack, ctx, displayComponent, textConsumer, type);
    }
}
