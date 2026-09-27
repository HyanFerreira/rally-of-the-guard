package net.hfstack.rallyguard.item;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.component.ModComponents;
import net.hfstack.rallyguard.api.eligibility.GuardEligibility;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityContext;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityDecision;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityOperation;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.effect.ModEffects;
import net.hfstack.rallyguard.event.RallyFormationTicker;
import net.hfstack.rallyguard.order.GuardOrders;
import net.hfstack.rallyguard.order.RallyFormationSlots;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class ScrollOfRallyingItem extends Item {

    public ScrollOfRallyingItem(Properties settings) {
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

    private static void rallyGuardToPlayer(Entity e, Player user, double x, double y, double z) {
        if (!(e instanceof GuardEntity guard)) return;

        guard.setTarget(null);
        guard.setAggressive(false);
        GuardOrders.setWaiting(guard, false);
        GuardOrders.setRallied(guard, true);
        guard.getNavigation().stop();
        guard.setDeltaMovement(0.0, 0.0, 0.0);

        double teleportMinDistance = RallyConfig.rallyTeleportMinDistance();
        if (RallyConfig.rallyTeleportEnabled()
                && guard.distanceToSqr(user) >= teleportMinDistance * teleportMinDistance) {
            guard.snapTo(x, y, z, guard.getYRot(), guard.getXRot());
        }
        guard.setFollowing(!RallyConfig.rallyFormationEnabled());
        guard.setNoAi(false);
        guard.lookAt(user, 30.0F, 30.0F);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        if (!user.isShiftKeyDown()) return InteractionResult.PASS;
        if (!(user instanceof ServerPlayer sp)) return InteractionResult.PASS;

        boolean rallyOn = user.hasEffect(ModEffects.RALLY_COMMANDER);
        EntityType<?> guardType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("guardvillagers", "guard"));
        ServerLevel sw = sp.level();

        if (rallyOn) {
            user.removeEffect(ModEffects.RALLY_COMMANDER);
            RallyFormationTicker.stopRally(sp);
            setActive(stack, false);
            user.sendSystemMessage(Component.translatable("alert.rallyguard.scroll_of_rallying.strength_lost")
                    .withStyle(s -> s.withColor(0xFF0000)));

            List<? extends Entity> myGuards = sw.getEntities(
                    guardType,
                    e -> GuardOwnership.isOwnedBy(e, user.getUUID())
                            && e.distanceToSqr(user) <= RallyConfig.combatGuardSearchRadius() * RallyConfig.combatGuardSearchRadius()
            );

            for (Entity g : myGuards) {
                setFollowing(g, false);
                GuardOrders.setRallied(g, false);
            }
        } else {
            user.addEffect(new MobEffectInstance(
                    ModEffects.RALLY_COMMANDER, RallyConfig.rallyEffectTimerSeconds() * 20, 0, false, false, true));
            RallyFormationTicker.startRally(sp);
            setActive(stack, true);
            user.sendSystemMessage(Component.translatable("alert.rallyguard.scroll_of_rallying.strength_gained", RallyConfig.rallyRadius())
                    .withStyle(s -> s.withColor(0x00FF00)));

            List<? extends Entity> candidates = sw.getEntities(
                    guardType,
                    e -> GuardOwnership.isOwnedBy(e, user.getUUID())
                            && e.distanceToSqr(user) <= RallyConfig.rallyRadius() * RallyConfig.rallyRadius()
            );

            List<Entity> joiners = new ArrayList<>();
            for (Entity g : candidates) {
                if (!(g instanceof GuardEntity guard) || isPatrolling(g)) continue;
                GuardEligibilityDecision eligibility = GuardEligibility.evaluate(
                        GuardEligibilityContext.of(sp, guard, GuardEligibilityOperation.JOIN_RALLY)
                );
                if (eligibility instanceof GuardEligibilityDecision.Deny) continue;
                joiners.add(g);
            }
            joiners.sort(Comparator.comparingInt(Entity::getId));

            int total = Math.min(joiners.size(), RallyConfig.rallyMaxGuards());
            for (int i = 0; i < total; i++) {
                Entity g = joiners.get(i);
                Vec3 slot = RallyFormationSlots.safeEscortSlot(sw, user, i, user.getYRot(), true);

                rallyGuardToPlayer(g, user, slot.x, slot.y, slot.z);
            }
        }

        user.getCooldowns().addCooldown(stack, 60);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, TooltipDisplay displayComponent,
                              Consumer<Component> textConsumer, TooltipFlag type) {
        textConsumer.accept(Component.translatable("tooltip.rallyguard.scroll_of_rallying.tooltip_desc"));
        super.appendHoverText(stack, ctx, displayComponent, textConsumer, type);
    }
}
