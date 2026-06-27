package net.hfstack.rallyguard.item;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.component.ModComponents;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.effect.ModEffects;
import net.hfstack.rallyguard.event.RallyFormationTicker;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.RallyFormationSlots;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

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

    private static boolean isPatrolling(Entity e) {
        return e instanceof GuardEntity guard && guard.isPatrolling();
    }

    private static void setFollowing(Entity e, boolean following) {
        if (e instanceof GuardEntity guard) {
            guard.setFollowing(following);
            if (!following) {
                GuardOrders.setRallied(guard, false);
            }
        }
    }

    private static void rallyGuardToPlayer(Entity e, PlayerEntity user, double x, double y, double z) {
        if (!(e instanceof GuardEntity guard)) return;

        guard.setTarget(null);
        guard.setAttacking(false);
        GuardOrders.setWaiting(guard, false);
        GuardOrders.setRallied(guard, true);
        guard.getNavigation().stop();
        guard.setVelocity(0.0, 0.0, 0.0);

        double teleportMinDistance = RallyConfig.rallyTeleportMinDistance();
        if (RallyConfig.rallyTeleportEnabled()
                && guard.squaredDistanceTo(user) >= teleportMinDistance * teleportMinDistance) {
            guard.refreshPositionAndAngles(x, y, z, guard.getYaw(), guard.getPitch());
        }
        guard.setFollowing(!RallyConfig.rallyFormationEnabled());
        guard.setAiDisabled(false);
        guard.lookAtEntity(user, 30.0F, 30.0F);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) return ActionResult.SUCCESS;
        if (!user.isSneaking()) return ActionResult.PASS;
        if (!(user instanceof ServerPlayerEntity sp)) return ActionResult.PASS;

        boolean rallyOn = user.hasStatusEffect(ModEffects.RALLY_COMMANDER);
        EntityType<?> guardType = Registries.ENTITY_TYPE.get(Identifier.of("guardvillagers", "guard"));
        ServerWorld sw = sp.getEntityWorld();

        if (rallyOn) {
            user.removeStatusEffect(ModEffects.RALLY_COMMANDER);
            RallyFormationTicker.stopRally(sp);
            setActive(stack, false);
            user.sendMessage(Text.translatable("alert.rallyguard.scroll_of_rallying.strength_lost")
                    .styled(s -> s.withColor(0xFF0000)), false);

            List<? extends Entity> myGuards = sw.getEntitiesByType(
                    guardType,
                    e -> GuardOwnership.isOwnedBy(e, user.getUuid())
                            && e.squaredDistanceTo(user) <= RallyConfig.combatGuardSearchRadius() * RallyConfig.combatGuardSearchRadius()
            );

            for (Entity g : myGuards) {
                setFollowing(g, false);
                GuardOrders.setRallied(g, false);
            }
        } else {
            user.addStatusEffect(new StatusEffectInstance(
                    ModEffects.RALLY_COMMANDER, RallyConfig.rallyEffectTimerSeconds() * 20, 0, false, false, true));
            RallyFormationTicker.startRally(sp);
            setActive(stack, true);
            user.sendMessage(Text.translatable("alert.rallyguard.scroll_of_rallying.strength_gained", RallyConfig.rallyRadius())
                    .styled(s -> s.withColor(0x00FF00)), false);

            List<? extends Entity> candidates = sw.getEntitiesByType(
                    guardType,
                    e -> GuardOwnership.isOwnedBy(e, user.getUuid())
                            && e.squaredDistanceTo(user) <= RallyConfig.rallyRadius() * RallyConfig.rallyRadius()
            );

            List<Entity> joiners = new ArrayList<>();
            for (Entity g : candidates) {
                if (isPatrolling(g)) continue;
                joiners.add(g);
            }
            joiners.sort(Comparator.comparingInt(Entity::getId));

            int total = Math.min(joiners.size(), RallyConfig.rallyMaxGuards());
            for (int i = 0; i < total; i++) {
                Entity g = joiners.get(i);
                Vec3d slot = RallyFormationSlots.safeEscortSlot(sw, user, i, user.getYaw(), true);

                rallyGuardToPlayer(g, user, slot.x, slot.y, slot.z);
            }
        }

        user.getItemCooldownManager().set(stack, 60);
        return ActionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return isActive(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext ctx, TooltipDisplayComponent displayComponent,
                              Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(Text.translatable("tooltip.rallyguard.scroll_of_rallying.tooltip_desc"));
        super.appendTooltip(stack, ctx, displayComponent, textConsumer, type);
    }
}
