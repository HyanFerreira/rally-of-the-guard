package net.hfstack.rallyguard.service;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.recruitment.GuardRecruitmentEvents;
import net.hfstack.rallyguard.api.recruitment.GuardRecruitmentService;
import net.hfstack.rallyguard.api.recruitment.RecruitmentContext;
import net.hfstack.rallyguard.api.recruitment.RecruitmentDecision;
import net.hfstack.rallyguard.api.recruitment.RecruitmentOffer;
import net.hfstack.rallyguard.api.recruitment.RecruitmentResult;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Objects;

public final class DefaultGuardRecruitmentService implements GuardRecruitmentService {
    public static final double MAX_RECRUITMENT_DISTANCE = 8.0;

    @Override
    public RecruitmentResult recruit(ServerPlayerEntity player, GuardEntity guard) {
        Objects.requireNonNull(player, "player");

        RecruitmentResult invalid = validate(player, guard);
        if (invalid != null) {
            return invalid;
        }

        RecruitmentContext context = new RecruitmentContext(
                player,
                guard,
                Registries.ITEM.getId(RallyConfig.hireItem()),
                RallyConfig.hireCost(),
                player.getEntityWorld(),
                guard.getBlockPos()
        );

        RecruitmentDecision decision;
        try {
            decision = GuardRecruitmentEvents.BEFORE.invoker().evaluate(context, context.defaultOffer());
        } catch (RuntimeException exception) {
            RallyOfTheGuard.LOGGER.error("Recruitment policy failed for guard {}", guard.getUuid(), exception);
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.POLICY_ERROR,
                    Text.translatable("gui.rallyguard.hire.policy_error")
            );
        }

        if (decision instanceof RecruitmentDecision.Deny denied) {
            return RecruitmentResult.failure(RecruitmentResult.Outcome.DENIED, denied.reason());
        }

        RecruitmentOffer offer = ((RecruitmentDecision.Allow) decision).offer();
        if (!Registries.ITEM.containsId(offer.paymentItemId())) {
            return invalidOffer(offer);
        }
        Item paymentItem = Registries.ITEM.get(offer.paymentItemId());
        if (paymentItem == Items.AIR) {
            return invalidOffer(offer);
        }
        RecruitmentResult changedState = validate(player, guard);
        if (changedState != null) {
            return changedState;
        }

        PlayerInventory inventory = player.getInventory();
        if (countPayment(inventory, paymentItem) < offer.cost()) {
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.NOT_ENOUGH_PAYMENT,
                    Text.translatable("gui.rallyguard.hire.not_enough", offer.cost(), paymentItem.getName())
            );
        }

        removePayment(inventory, paymentItem, offer.cost());
        Text originalCustomName = guard.getCustomName();
        boolean originalCustomNameVisible = guard.isCustomNameVisible();
        try {
            GuardOwnership.setOwner(guard, player);
        } catch (RuntimeException exception) {
            GuardOwnership.clearOwner(guard);
            guard.setCustomName(originalCustomName);
            guard.setCustomNameVisible(originalCustomNameVisible);
            refundPayment(inventory, paymentItem, offer.cost());
            RallyOfTheGuard.LOGGER.error("Failed to assign guard {} to player {}", guard.getUuid(), player.getUuid(), exception);
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.OWNERSHIP_ERROR,
                    Text.translatable("gui.rallyguard.hire.ownership_error")
            );
        }

        try {
            GuardRecruitmentEvents.AFTER.invoker().onRecruited(context, offer);
        } catch (RuntimeException exception) {
            RallyOfTheGuard.LOGGER.error("Recruitment AFTER listener failed for guard {}", guard.getUuid(), exception);
        }

        return RecruitmentResult.success(offer);
    }

    private static RecruitmentResult invalidOffer(RecruitmentOffer offer) {
        RallyOfTheGuard.LOGGER.error("Recruitment policy produced invalid payment item {}", offer.paymentItemId());
        return RecruitmentResult.failure(
                RecruitmentResult.Outcome.INVALID_OFFER,
                Text.translatable("gui.rallyguard.hire.invalid_offer")
        );
    }

    private static RecruitmentResult validate(ServerPlayerEntity player, GuardEntity guard) {
        if (guard == null
                || !guard.isAlive()
                || guard.isRemoved()
                || guard.getEntityWorld() != player.getEntityWorld()
                || player.squaredDistanceTo(guard) > MAX_RECRUITMENT_DISTANCE * MAX_RECRUITMENT_DISTANCE) {
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.INVALID_GUARD,
                    Text.translatable("gui.rallyguard.hire.invalid")
            );
        }
        if (GuardOwnership.hasOwner(guard)) {
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.ALREADY_OWNED,
                    Text.translatable("gui.rallyguard.hire.already_owned")
            );
        }
        return null;
    }

    private static int countPayment(PlayerInventory inventory, Item paymentItem) {
        int total = 0;
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.isOf(paymentItem)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void removePayment(PlayerInventory inventory, Item paymentItem, int cost) {
        int remaining = cost;
        for (int slot = 0; slot < inventory.size() && remaining > 0; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.isOf(paymentItem)) {
                int removed = Math.min(remaining, stack.getCount());
                stack.decrement(removed);
                remaining -= removed;
            }
        }
    }

    private static void refundPayment(PlayerInventory inventory, Item paymentItem, int cost) {
        if (cost == 0) {
            return;
        }
        inventory.offerOrDrop(new ItemStack(paymentItem, cost));
    }
}
