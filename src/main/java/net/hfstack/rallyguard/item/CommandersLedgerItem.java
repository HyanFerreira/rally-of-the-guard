package net.hfstack.rallyguard.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Livro de Comando — abre o painel de controle dos guardas.
 * Usa reflexão para chamar um helper do CLIENTE sem quebrar no servidor.
 */
public class CommandersLedgerItem extends Item {
    public CommandersLedgerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) {
            try {
                Class<?> hooks = Class.forName("net.hfstack.rallyguard.client.ClientHooks");
                Method m = hooks.getMethod("requestOpenGuardPanel");
                m.invoke(null);
            } catch (Throwable t) {
                // opcional: logar em dev, mas não crashar
                // System.out.println("[rallyguard] Falha abrindo painel: " + t);
            }
            return TypedActionResult.success(stack, true);
        }
        return TypedActionResult.pass(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext ctx, List<Text> tips, TooltipType type) {
        tips.add(Text.translatable("item.rallyguard.commanders_ledger.tooltip"));
        super.appendTooltip(stack, ctx, tips, type);
    }
}
