package net.hfstack.rallyguard.item;

import java.util.ArrayList;
import java.util.List;

import net.hfstack.rallyguard.component.ModComponents;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.effect.ModEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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

    public ScrollOfRallyingItem(Settings settings) {
        super(settings);
    }

    private static boolean isActive(ItemStack stack) {
        return stack.getOrDefault(ModComponents.ACTIVE, false);
    }

    private static void setActive(ItemStack stack, boolean v) {
        stack.set(ModComponents.ACTIVE, v);
    }

    private static NbtCompound read(Entity e) {
        NbtCompound n = new NbtCompound();
        e.writeNbt(n);
        return n;
    }

    private static boolean isPatrolling(Entity e) {
        return read(e).getBoolean("Patrolling");
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(stack, true);
        if (!user.isSneaking()) return TypedActionResult.pass(stack);
        if (!(user instanceof ServerPlayerEntity sp)) return TypedActionResult.pass(stack);

        boolean rallyOn = user.hasStatusEffect(ModEffects.RALLY_COMMANDER);
        EntityType<?> guardType = Registries.ENTITY_TYPE.get(Identifier.of("guardvillagers", "guard"));
        ServerWorld sw = sp.getServerWorld();

        if (rallyOn) {
            // === DESATIVAR RALI === (estilo antigo: aplica a TODOS os seus guardas no raio)
            user.removeStatusEffect(ModEffects.RALLY_COMMANDER);
            setActive(stack, false);
            user.sendMessage(Text.translatable("alert.rallyguard.scroll_of_rallying.strength_lost")
                    .styled(s -> s.withColor(0xFF0000)), false);

            List<? extends Entity> myGuards = sw.getEntitiesByType(
                    guardType,
                    e -> GuardOwnership.isOwnedBy(e, user.getUuid()) && e.squaredDistanceTo(user) <= 100 * 100
            );

            for (Entity g : myGuards) {
                NbtCompound nbt = new NbtCompound();
                g.writeNbt(nbt);
                nbt.putBoolean("rallyguard:in_rally", false);
                nbt.putBoolean("Following", false); // <- só boolean, como no seu código antigo
                g.readNbt(nbt);
            }

        } else {
            // === ATIVAR RALI === (apenas guardas seus que NÃO estão patrulhando)
            user.addStatusEffect(new StatusEffectInstance(
                    ModEffects.RALLY_COMMANDER, Integer.MAX_VALUE, 0, false, false, true));
            setActive(stack, true);
            user.sendMessage(Text.translatable("alert.rallyguard.scroll_of_rallying.strength_gained")
                    .styled(s -> s.withColor(0x00FF00)), false);

            List<? extends Entity> candidates = sw.getEntitiesByType(
                    guardType,
                    e -> GuardOwnership.isOwnedBy(e, user.getUuid()) && e.squaredDistanceTo(user) <= 100 * 100
            );

            List<Entity> joiners = new ArrayList<>();
            for (Entity g : candidates) {
                if (isPatrolling(g)) continue; // não puxa sentinelas
                joiners.add(g);
            }

            int total = joiners.size();
            int i = 0;
            for (Entity g : joiners) {
                double angle = (Math.PI / (total + 1)) * (++i);
                double radius = 3.5;
                double gx = user.getX() + Math.cos(angle) * radius;
                double gz = user.getZ() + Math.sin(angle) * radius;

                g.refreshPositionAndAngles(gx, user.getY(), gz, g.getYaw(), g.getPitch());

                NbtCompound nbt = new NbtCompound();
                g.writeNbt(nbt);
                nbt.putBoolean("rallyguard:in_rally", true);
                nbt.putBoolean("Following", true); // <- só boolean, como no seu código antigo
                g.readNbt(nbt);
            }
        }

        user.getItemCooldownManager().set(this, 60);
        return TypedActionResult.success(stack, false);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return isActive(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext ctx, java.util.List<Text> tip, TooltipType type) {
        tip.add(Text.translatable("tooltip.rallyguard.scroll_of_rallying.tooltip_desc"));
        super.appendTooltip(stack, ctx, tip, type);
    }
}
