package net.hfstack.rallyguard.item;

import java.util.List;

import net.hfstack.rallyguard.effect.ModEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ScrollOfRallyingItem extends Item {

    private boolean active = false;  // se o item está brilhando

    public ScrollOfRallyingItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            if (user.isSneaking()) { // segurando SHIFT
                // Verifica cooldown
                if (user.getItemCooldownManager().isCoolingDown(this)) {
                    user.sendMessage(Text.literal("Aguarde antes de usar novamente!"), true);
                    return TypedActionResult.fail(user.getStackInHand(hand));
                }

                boolean hasRallyCommander = user.hasStatusEffect(ModEffects.RALLY_COMMANDER);

                if (!(user instanceof ServerPlayerEntity serverPlayer)) {
                    return TypedActionResult.pass(user.getStackInHand(hand));
                }
                // ServerCommandSource source = serverPlayer.getCommandSource().withSilent();

                if (hasRallyCommander) {
                    user.removeStatusEffect(ModEffects.RALLY_COMMANDER);
                    active = false;  // remove brilho
                    user.sendMessage(Text.translatable("alert.rallyguard.scroll_of_rallying.strength_lost").styled(style -> style.withColor(0xFF0000)), false);

                    // Para remover ação
                    ServerWorld serverWorld = serverPlayer.getServerWorld();
                    List<? extends Entity> nearbyGuards = serverWorld.getEntitiesByType(
                            Registries.ENTITY_TYPE.get(Identifier.of("guardvillagers", "guard")),
                            entity -> entity.getCommandTags().contains("contratado")
                            && entity.squaredDistanceTo(user) <= 100 * 100
                    );

                    for (Entity guard : nearbyGuards) {
                        NbtCompound nbt = new NbtCompound();
                        guard.writeNbt(nbt);

                        nbt.putByte("Patrolling", (byte) 0);
                        nbt.putByte("Following", (byte) 0);

                        guard.readNbt(nbt);
                    }

                } else {
                    user.addStatusEffect(new StatusEffectInstance(ModEffects.RALLY_COMMANDER, Integer.MAX_VALUE, 0, false, false, true));
                    active = true;  // adiciona brilho
                    user.sendMessage(Text.translatable("alert.rallyguard.scroll_of_rallying.strength_gained").styled(style -> style.withColor(0x00FF00)), false);

                    // Para aplicar ação
                    ServerWorld serverWorld = serverPlayer.getServerWorld();
                    List<? extends Entity> nearbyGuards = serverWorld.getEntitiesByType(
                            Registries.ENTITY_TYPE.get(Identifier.of("guardvillagers", "guard")),
                            entity -> entity.getCommandTags().contains("contratado")
                            && entity.squaredDistanceTo(user) <= 100 * 100
                    );

                    for (Entity guard : nearbyGuards) {
                        // Espalhar levemente para simular "spreadplayers"
                        double offsetX = (user.getRandom().nextDouble() - 0.5) * 6; // entre -3 e 3
                        double offsetZ = (user.getRandom().nextDouble() - 0.5) * 6;
                        guard.refreshPositionAndAngles(user.getX() + offsetX, user.getY(), user.getZ() + offsetZ, guard.getYaw(), guard.getPitch());

                        NbtCompound nbt = new NbtCompound();
                        guard.writeNbt(nbt);

                        nbt.putByte("Patrolling", (byte) 0);
                        nbt.putByte("Following", (byte) 1);

                        guard.readNbt(nbt);
                    }
                }

                // no final da ação aplica cooldown
                user.getItemCooldownManager().set(this, 60); // 60 ticks = 3s

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
