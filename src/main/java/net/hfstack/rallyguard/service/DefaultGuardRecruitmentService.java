package net.hfstack.rallyguard.service;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.eligibility.GuardEligibility;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityContext;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityDecision;
import net.hfstack.rallyguard.api.eligibility.GuardEligibilityOperation;
import net.hfstack.rallyguard.api.recruitment.GuardRecruitmentEvents;
import net.hfstack.rallyguard.api.recruitment.GuardRecruitmentService;
import net.hfstack.rallyguard.api.recruitment.RecruitmentContext;
import net.hfstack.rallyguard.api.recruitment.RecruitmentDecision;
import net.hfstack.rallyguard.api.recruitment.RecruitmentOffer;
import net.hfstack.rallyguard.api.recruitment.RecruitmentQuote;
import net.hfstack.rallyguard.api.recruitment.RecruitmentQuoteResult;
import net.hfstack.rallyguard.api.recruitment.RecruitmentResult;
import net.hfstack.rallyguard.api.recruitment.RecruitmentTransaction;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.economy.InventoryPayment;
import net.hfstack.rallyguard.profile.GuardProfileReadResult;
import net.hfstack.rallyguard.profile.GuardProfileRepository;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DefaultGuardRecruitmentService implements GuardRecruitmentService {
    public static final double MAX_RECRUITMENT_DISTANCE = 8.0;

    @Override
    public RecruitmentQuoteResult quote(ServerPlayer player, GuardEntity guard) {
        Objects.requireNonNull(player, "player");
        Preparation preparation = prepare(player, guard);
        if (preparation.failure() != null) return quoteFailure(preparation.failure());
        return RecruitmentQuoteResult.available(createQuote(guard, preparation.prepared().offer()));
    }

    @Override
    public RecruitmentResult recruit(ServerPlayer player, GuardEntity guard) {
        Objects.requireNonNull(player, "player");
        Preparation preparation = prepare(player, guard);
        if (preparation.failure() != null) return preparation.failure();
        RecruitmentQuote currentQuote = createQuote(guard, preparation.prepared().offer());
        return recruitPrepared(player, guard, currentQuote, preparation);
    }

    @Override
    public RecruitmentResult recruit(ServerPlayer player, GuardEntity guard, RecruitmentQuote expectedQuote) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(expectedQuote, "expectedQuote");

        Preparation preparation = prepare(player, guard);
        return recruitPrepared(player, guard, expectedQuote, preparation);
    }

    private static RecruitmentResult recruitPrepared(
            ServerPlayer player,
            GuardEntity guard,
            RecruitmentQuote expectedQuote,
            Preparation preparation
    ) {
        if (preparation.failure() != null) return preparation.failure();
        PreparedRecruitment prepared = preparation.prepared();
        RecruitmentQuote currentQuote = createQuote(guard, prepared.offer());
        if (!currentQuote.equals(expectedQuote)) {
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.OFFER_CHANGED,
                    Component.translatable("gui.rallyguard.hire.offer_changed")
            );
        }

        RecruitmentResult changedState = validate(player, guard);
        if (changedState != null) return changedState;

        Inventory inventory = player.getInventory();
        if (InventoryPayment.count(inventory, prepared.paymentItem()) < prepared.offer().cost()) {
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.NOT_ENOUGH_PAYMENT,
                    Component.translatable(
                            "gui.rallyguard.hire.not_enough",
                            prepared.offer().cost(),
                            prepared.paymentItem().getName(new ItemStack(prepared.paymentItem()))
                    )
            );
        }

        List<RecruitmentTransaction> reservedTransactions = new ArrayList<>();
        try {
            for (RecruitmentTransaction transaction : prepared.transactions()) {
                reservedTransactions.add(transaction);
                Optional<Component> reservationIssue = transaction.reserve();
                if (reservationIssue.isPresent()) {
                    rollbackTransactions(reservedTransactions);
                    return RecruitmentResult.failure(RecruitmentResult.Outcome.DENIED, reservationIssue.get());
                }
            }
        } catch (RuntimeException exception) {
            rollbackTransactions(reservedTransactions);
            RallyOfTheGuard.LOGGER.error("Recruitment transaction reservation failed for guard {}", guard.getUUID(), exception);
            return policyError();
        }

        InventoryPayment.remove(inventory, prepared.paymentItem(), prepared.offer().cost());
        Component originalCustomName = guard.getCustomName();
        boolean originalCustomNameVisible = guard.isCustomNameVisible();
        try {
            GuardOwnership.setOwner(guard, player);
        } catch (RuntimeException exception) {
            restoreAfterFailure(guard, originalCustomName, originalCustomNameVisible);
            InventoryPayment.refund(inventory, prepared.paymentItem(), prepared.offer().cost());
            rollbackTransactions(reservedTransactions);
            RallyOfTheGuard.LOGGER.error("Failed to assign guard {} to player {}", guard.getUUID(), player.getUUID(), exception);
            return RecruitmentResult.failure(
                    RecruitmentResult.Outcome.OWNERSHIP_ERROR,
                    Component.translatable("gui.rallyguard.hire.ownership_error")
            );
        }

        try {
            for (RecruitmentTransaction transaction : reservedTransactions) transaction.commit();
        } catch (RuntimeException exception) {
            restoreAfterFailure(guard, originalCustomName, originalCustomNameVisible);
            InventoryPayment.refund(inventory, prepared.paymentItem(), prepared.offer().cost());
            rollbackTransactions(reservedTransactions);
            RallyOfTheGuard.LOGGER.error("Recruitment transaction commit failed for guard {}", guard.getUUID(), exception);
            return policyError();
        }

        try {
            GuardRecruitmentEvents.AFTER.invoker().onRecruited(prepared.context(), prepared.offer());
        } catch (RuntimeException exception) {
            RallyOfTheGuard.LOGGER.error("Recruitment AFTER listener failed for guard {}", guard.getUUID(), exception);
        }
        return RecruitmentResult.success(prepared.offer());
    }

    private static Preparation prepare(ServerPlayer player, GuardEntity guard) {
        RecruitmentResult invalid = validate(player, guard);
        if (invalid != null) return Preparation.failure(invalid);

        GuardEligibilityDecision eligibility = GuardEligibility.evaluate(
                GuardEligibilityContext.of(player, guard, GuardEligibilityOperation.RECRUIT)
        );
        if (eligibility instanceof GuardEligibilityDecision.Deny denied) {
            return Preparation.failure(RecruitmentResult.failure(RecruitmentResult.Outcome.DENIED, denied.reason()));
        }

        RecruitmentOffer defaultOffer = RallyGuardApi.hiringValues().calculate(guard);
        RecruitmentContext context = new RecruitmentContext(
                player,
                guard,
                defaultOffer.paymentItemId(),
                defaultOffer.cost(),
                player.level(),
                guard.blockPosition()
        );
        RecruitmentDecision decision;
        try {
            decision = GuardRecruitmentEvents.BEFORE.invoker().evaluate(context, defaultOffer);
        } catch (RuntimeException exception) {
            RallyOfTheGuard.LOGGER.error("Recruitment policy failed for guard {}", guard.getUUID(), exception);
            return Preparation.failure(policyError());
        }
        if (decision instanceof RecruitmentDecision.Deny denied) {
            return Preparation.failure(RecruitmentResult.failure(RecruitmentResult.Outcome.DENIED, denied.reason()));
        }

        RecruitmentDecision.Allow allowed = (RecruitmentDecision.Allow) decision;
        RecruitmentOffer offer = allowed.offer();
        if (!BuiltInRegistries.ITEM.containsKey(offer.paymentItemId())) {
            return Preparation.failure(invalidOffer(offer));
        }
        Item paymentItem = BuiltInRegistries.ITEM.getValue(offer.paymentItemId());
        if (paymentItem == Items.AIR) return Preparation.failure(invalidOffer(offer));
        return Preparation.success(new PreparedRecruitment(context, offer, paymentItem, allowed.transactions()));
    }

    private static RecruitmentQuote createQuote(GuardEntity guard, RecruitmentOffer offer) {
        GuardProfileReadResult profile = GuardProfileRepository.instance().read(guard);
        long xp = profile instanceof GuardProfileReadResult.Available available
                ? available.profile().totalXpMilli()
                : -1;
        return RecruitmentQuote.create(guard.getUUID(), xp, offer);
    }

    private static RecruitmentQuoteResult quoteFailure(RecruitmentResult failure) {
        RecruitmentQuoteResult.Outcome outcome = switch (failure.outcome()) {
            case INVALID_GUARD -> RecruitmentQuoteResult.Outcome.INVALID_GUARD;
            case ALREADY_OWNED -> RecruitmentQuoteResult.Outcome.ALREADY_OWNED;
            case DENIED -> RecruitmentQuoteResult.Outcome.DENIED;
            case INVALID_OFFER -> RecruitmentQuoteResult.Outcome.INVALID_OFFER;
            default -> RecruitmentQuoteResult.Outcome.POLICY_ERROR;
        };
        return RecruitmentQuoteResult.unavailable(outcome, failure.feedback());
    }

    private static RecruitmentResult invalidOffer(RecruitmentOffer offer) {
        RallyOfTheGuard.LOGGER.error("Recruitment policy produced invalid payment item {}", offer.paymentItemId());
        return RecruitmentResult.failure(
                RecruitmentResult.Outcome.INVALID_OFFER,
                Component.translatable("gui.rallyguard.hire.invalid_offer")
        );
    }

    private static RecruitmentResult policyError() {
        return RecruitmentResult.failure(
                RecruitmentResult.Outcome.POLICY_ERROR,
                Component.translatable("gui.rallyguard.hire.policy_error")
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

    private static void restoreAfterFailure(
            GuardEntity guard,
            Component originalCustomName,
            boolean originalCustomNameVisible
    ) {
        GuardOwnership.clearOwner(guard);
        guard.setCustomName(originalCustomName);
        guard.setCustomNameVisible(originalCustomNameVisible);
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

    private record PreparedRecruitment(
            RecruitmentContext context,
            RecruitmentOffer offer,
            Item paymentItem,
            List<RecruitmentTransaction> transactions
    ) {
        private PreparedRecruitment {
            transactions = List.copyOf(transactions);
        }
    }

    private record Preparation(PreparedRecruitment prepared, RecruitmentResult failure) {
        private static Preparation success(PreparedRecruitment prepared) {
            return new Preparation(prepared, null);
        }

        private static Preparation failure(RecruitmentResult failure) {
            return new Preparation(null, failure);
        }
    }
}
