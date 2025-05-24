package net.hfstack.rallyguard.item;

import net.hfstack.rallyguard.effect.ModEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;

public class ScrollOfRallyingItem extends Item {

    private boolean active = false;  // se o item está brilhando

    public ScrollOfRallyingItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            if (user.isSneaking()) { // segurando SHIFT
                boolean hasRallyCommander = user.hasStatusEffect(ModEffects.RALLY_COMMANDER);

                if (!(user instanceof ServerPlayerEntity serverPlayer)) {
                    return TypedActionResult.pass(user.getStackInHand(hand));
                }
                ServerCommandSource source = serverPlayer.getCommandSource().withSilent();

                if (hasRallyCommander) {
                    user.removeStatusEffect(ModEffects.RALLY_COMMANDER);
                    active = false;  // remove brilho
                    user.sendMessage(Text.translatable("alert.rallyguard.scroll_of_rallying.strength_lost").styled(style -> style.withColor(0xFF0000)), false);

                    // Executa comando para remover uma ação
                    try {
                        source.getServer().getCommandManager().executeWithPrefix(source,
                                "execute as @e[type=guardvillagers:guard,distance=..100] at @s run data merge entity @s {Patrolling:0b,Following:0b}");
                    } catch (Exception e) {
                        user.sendMessage(Text.literal("Erro ao executar comandos para remover ação."), false);
                        e.printStackTrace();
                    }

                } else {
                    user.addStatusEffect(new StatusEffectInstance(ModEffects.RALLY_COMMANDER, Integer.MAX_VALUE, 0, false, false, true));
                    active = true;  // adiciona brilho
                    user.sendMessage(Text.translatable("alert.rallyguard.scroll_of_rallying.strength_gained").styled(style -> style.withColor(0x00FF00)), false);

                    // Executa comando para aplicar uma ação
                    try {
                        String spreadCommand = String.format(Locale.US,
                                "spreadplayers %.1f %.1f 3 5 false @e[type=guardvillagers:guard,distance=..50]",
                                user.getX(), user.getZ()
                        );
                        source.getServer().getCommandManager().executeWithPrefix(source, spreadCommand);
                        source.getServer().getCommandManager().executeWithPrefix(source,
                                "execute as @e[type=guardvillagers:guard,distance=..50] at @s run data merge entity @s {Patrolling:0b,Following:1b}");
                    } catch (Exception e) {
                        user.sendMessage(Text.literal("Erro ao executar comandos para aplicar ação."), false);
                        e.printStackTrace();
                    }
                }

                return TypedActionResult.success(user.getStackInHand(hand), false);
            } else {
                return TypedActionResult.pass(user.getStackInHand(hand));
            }
        }

        return TypedActionResult.success(user.getStackInHand(hand), true);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return active;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.rallyguard.scroll_of_rallying.tooltip_desc").styled(style -> style.withColor(0xFFFFFF)));

        super.appendTooltip(stack, context, tooltip, type);
    }
}
