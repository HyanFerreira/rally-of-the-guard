package net.hfstack.rallyguard.service;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.eligibility.GuardEligibility;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityContext;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityDecision;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityOperation;
import net.hfstack.rallyguard.api.recruitment.GuardRecruitmentEvents;
import net.hfstack.rallyguard.api.recruitment.GuardRecruitmentService;
import net.hfstack.rallyguard.api.recruitment.RecruitmentContext;
import net.hfstack.rallyguard.api.recruitment.RecruitmentDecision;
import net.hfstack.rallyguard.api.recruitment.RecruitmentOffer;
import net.hfstack.rallyguard.api.recruitment.RecruitmentResult;
import net.hfstack.rallyguard.api.recruitment.RecruitmentTransaction;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DefaultGuardRecruitmentService implements GuardRecruitmentService {
    public static final double MAX_RECRUITMENT_DISTANCE = 8.0;

    @Override
    public RecruitmentResult recruit(ServerPlayer player, GuardEntity guard) {
        Objects.requireNonNull(player, "player");

        RecruitmentResult invalid = validate(player, guard);
        if (invalid != null) {
            return invalid;
        }

        GuardEligibilityDecision eligibility = GuardEligibility.evaluate(
                GuardEligibilityContext.of(player, guard, GuardEligibilityOperation.RECRUIT)
        );
        if (eligibility instanceof GuardEligibilityDecision.Deny denied) {
            return RecruitmentResult.failure(RecruitmentResult.Outcome.DENIED, denied.reason());
        }

        RecruitmentContext context = new RecruitmentContext(
                player,
                guard,
                BuiltInRegistries.ITEM.getKey(RallyConfig.hireItem()),
                RallyConfig.hireCost(),
                player.level(),
                guard.blockPosition()
        );

        RecruitmentDecision decision;
        try {
            decision = GuardRecruitmentEvents.BEFORE.invoker().evaluate(context, context.defaultOffer());
        } catch (RuntimeException exception) {
            RallyOfTheGuard.LOGGER.error("Recruitment policy failed for guard {}", guard.getUUID(), exception);
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.POLICY_ERROR,
                    Component.translatable("gui.rallyguard.hire.policy_error")
            );
        }

        if (decision instanceof RecruitmentDecision.Deny denied) {
            return RecruitmentResult.failure(RecruitmentResult.Outcome.DENIED, denied.reason());
        }

        RecruitmentDecision.Allow allowed = (RecruitmentDecision.Allow) decision;
        RecruitmentOffer offer = allowed.offer();
        if (!BuiltInRegistries.ITEM.containsKey(offer.paymentItemId())) {
            return invalidOffer(offer);
        }
        Item paymentItem = BuiltInRegistries.ITEM.getValue(offer.paymentItemId());
        if (paymentItem == Items.AIR) {
            return invalidOffer(offer);
        }
        RecruitmentResult changedState = validate(player, guard);
        if (changedState != null) {
            return changedState;
        }

        Inventory inventory = player.getInventory();
        if (countPayment(inventory, paymentItem) < offer.cost()) {
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.NOT_ENOUGH_PAYMENT,
                    Component.translatable("gui.rallyguard.hire.not_enough", offer.cost(), paymentItem.getName(new ItemStack(paymentItem)))
            );
        }

        List<RecruitmentTransaction> reservedTransactions = new ArrayList<>();
        try {
            for (RecruitmentTransaction transaction : allowed.transactions()) {
                reservedTransactions.add(transaction);
                Optional<Component> reservationIssue = transaction.reserve();
                if (reservationIssue.isPresent()) {
                    rollbackTransactions(reservedTransactions);
                    return RecruitmentResult.failure(
                            RecruitmentResult.Outcome.DENIED,
                            reservationIssue.get()
                    );
                }
            }
        } catch (RuntimeException exception) {
            rollbackTransactions(reservedTransactions);
            RallyOfTheGuard.LOGGER.error("Recruitment transaction reservation failed for guard {}", guard.getUUID(), exception);
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.POLICY_ERROR,
                    Component.translatable("gui.rallyguard.hire.policy_error")
            );
        }

        removePayment(inventory, paymentItem, offer.cost());
        Component originalCustomName = guard.getCustomName();
        boolean originalCustomNameVisible = guard.isCustomNameVisible();
        try {
            GuardOwnership.setOwner(guard, player);
        } catch (RuntimeException exception) {
            GuardOwnership.clearOwner(guard);
            guard.setCustomName(originalCustomName);
            guard.setCustomNameVisible(originalCustomNameVisible);
            refundPayment(inventory, paymentItem, offer.cost());
            rollbackTransactions(reservedTransactions);
            RallyOfTheGuard.LOGGER.error("Failed to assign guard {} to player {}", guard.getUUID(), player.getUUID(), exception);
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.OWNERSHIP_ERROR,
                    Component.translatable("gui.rallyguard.hire.ownership_error")
            );
        }

        try {
            for (RecruitmentTransaction transaction : reservedTransactions) {
                transaction.commit();
            }
        } catch (RuntimeException exception) {
            GuardOwnership.clearOwner(guard);
            guard.setCustomName(originalCustomName);
            guard.setCustomNameVisible(originalCustomNameVisible);
            refundPayment(inventory, paymentItem, offer.cost());
            rollbackTransactions(reservedTransactions);
            RallyOfTheGuard.LOGGER.error("Recruitment transaction commit failed for guard {}", guard.getUUID(), exception);
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.POLICY_ERROR,
                    Component.translatable("gui.rallyguard.hire.policy_error")
            );
        }

        try {
            GuardRecruitmentEvents.AFTER.invoker().onRecruited(context, offer);
        } catch (RuntimeException exception) {
            RallyOfTheGuard.LOGGER.error("Recruitment AFTER listener failed for guard {}", guard.getUUID(), exception);
        }

        return RecruitmentResult.success(offer);
    }

    private static RecruitmentResult invalidOffer(RecruitmentOffer offer) {
        RallyOfTheGuard.LOGGER.error("Recruitment policy produced invalid payment item {}", offer.paymentItemId());
        return RecruitmentResult.failure(
                RecruitmentResult.Outcome.INVALID_OFFER,
                Component.translatable("gui.rallyguard.hire.invalid_offer")
        );
    }

    private static RecruitmentResult validate(ServerPlayer player, GuardEntity guard) {
        if (guard == null
                || !guard.isAlive()
                || guard.isRemoved()
                || guard.level() != player.level()
                || player.distanceToSqr(guard) > MAX_RECRUITMENT_DISTANCE * MAX_RECRUITMENT_DISTANCE) {
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.INVALID_GUARD,
                    Component.translatable("gui.rallyguard.hire.invalid")
            );
        }
        if (GuardOwnership.hasOwner(guard)) {
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.ALREADY_OWNED,
                    Component.translatable("gui.rallyguard.hire.already_owned")
            );
        }
        return null;
    }

    private static int countPayment(Inventory inventory, Item paymentItem) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(paymentItem)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void removePayment(Inventory inventory, Item paymentItem, int cost) {
        int remaining = cost;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(paymentItem)) {
                int removed = Math.min(remaining, stack.getCount());
                stack.shrink(removed);
                remaining -= removed;
            }
        }
    }

    private static void refundPayment(Inventory inventory, Item paymentItem, int cost) {
        if (cost == 0) {
            return;
        }
        inventory.placeItemBackInInventory(new ItemStack(paymentItem, cost));
    }

    private static void rollbackTransactions(List<RecruitmentTransaction> transactions) {
        for (int index = transactions.size() - 1; index >= 0; index--) {
            try {
                transactions.get(index).rollback();
            } catch (RuntimeException exception) {
                RallyOfTheGuard.LOGGER.error("Recruitment transaction rollback failed", exception);
            }
        }
    }
}
