package net.hfstack.rallyguard.captain;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.RallyOfTheGuard;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.captain.CaptainAction;
import net.hfstack.rallyguard.api.captain.CaptainActionView;
import net.hfstack.rallyguard.api.captain.CaptainContext;
import net.hfstack.rallyguard.api.captain.CaptainDecision;
import net.hfstack.rallyguard.api.captain.CaptainEvents;
import net.hfstack.rallyguard.api.captain.CaptainMutationResult;
import net.hfstack.rallyguard.api.captain.CaptainOffer;
import net.hfstack.rallyguard.api.captain.CaptainService;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.economy.InventoryPayment;
import net.hfstack.rallyguard.profile.GuardProfile;
import net.hfstack.rallyguard.profile.GuardProfileReadResult;
import net.hfstack.rallyguard.profile.GuardProfileRepository;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;

import java.util.Objects;
import java.util.Optional;

public final class DefaultCaptainService implements CaptainService {
    private final GuardProfileRepository profiles = GuardProfileRepository.instance();

    @Override
    public CaptainActionView inspect(ServerPlayer owner, GuardEntity guard) {
        Objects.requireNonNull(owner, "owner");
        GuardProfileReadResult profileState = guard == null
                ? new GuardProfileReadResult.Absent()
                : profiles.read(guard);
        boolean available = available(owner, guard);
        boolean owned = available && GuardOwnership.isOwnedBy(guard, owner.getUUID());
        boolean captain = profileState instanceof GuardProfileReadResult.Available profile
                && profile.profile().captain();
        CaptainAction action = available && owned && profileState instanceof GuardProfileReadResult.Available
                ? (captain ? CaptainAction.DEMOTE : CaptainAction.PROMOTE)
                : CaptainAction.NONE;

        CaptainMutationResult.Outcome outcome;
        Optional<CaptainOffer> offer = Optional.empty();
        if (!available) {
            outcome = CaptainMutationResult.Outcome.NOT_AVAILABLE;
        } else if (!owned) {
            outcome = CaptainMutationResult.Outcome.NOT_OWNER;
        } else if (!(profileState instanceof GuardProfileReadResult.Available)) {
            outcome = profileState instanceof GuardProfileReadResult.FutureSchema
                    ? CaptainMutationResult.Outcome.UNSUPPORTED_PROFILE
                    : CaptainMutationResult.Outcome.NOT_AVAILABLE;
        } else {
            State state = prepare(owner, guard, action);
            outcome = CaptainMutationCoordinator.validationOutcome(state.request());
            offer = action == CaptainAction.PROMOTE ? state.request().offer() : Optional.empty();
        }

        boolean allowed = outcome == CaptainMutationResult.Outcome.SUCCESS;
        return new CaptainActionView(
                captain,
                action,
                allowed,
                allowed ? Optional.empty() : Optional.of(outcome),
                RallyConfig.captainPromotionMinimumLevel(),
                offer
        );
    }

    @Override
    public CaptainMutationResult promote(ServerPlayer owner, GuardEntity guard) {
        return mutate(owner, guard, CaptainAction.PROMOTE);
    }

    @Override
    public CaptainMutationResult demote(ServerPlayer owner, GuardEntity guard) {
        return mutate(owner, guard, CaptainAction.DEMOTE);
    }

    private CaptainMutationResult mutate(ServerPlayer owner, GuardEntity guard, CaptainAction action) {
        Objects.requireNonNull(owner, "owner");
        State initial = prepare(owner, guard, action);
        CaptainMutationResult.Outcome initialOutcome = CaptainMutationCoordinator.validationOutcome(initial.request());
        if (initialOutcome != CaptainMutationResult.Outcome.SUCCESS) {
            return CaptainMutationCoordinator.rejected(action, initialOutcome);
        }

        CaptainContext initialContext = context(owner, guard, initial.request());
        CaptainDecision decision;
        try {
            decision = CaptainEvents.BEFORE.invoker().evaluate(initialContext);
        } catch (RuntimeException exception) {
            RallyOfTheGuard.LOGGER.error("Captain policy failed for guard {}", guard.getUUID(), exception);
            return CaptainMutationCoordinator.rejected(action, CaptainMutationResult.Outcome.DENIED);
        }
        if (decision instanceof CaptainDecision.Deny denied) {
            return new CaptainMutationResult(
                    CaptainMutationResult.Outcome.DENIED,
                    denied.reason(),
                    action,
                    null
            );
        }

        State current = prepare(owner, guard, action);
        Inventory inventory = owner.getInventory();
        CaptainMutationResult result = CaptainMutationCoordinator.execute(
                current.request(),
                new CaptainMutationCoordinator.PaymentPort() {
                    @Override
                    public boolean canAfford(CaptainOffer offer) {
                        return InventoryPayment.count(inventory, current.paymentItem()) >= offer.cost();
                    }

                    @Override
                    public void remove(CaptainOffer offer) {
                        InventoryPayment.remove(inventory, current.paymentItem(), offer.cost());
                    }

                    @Override
                    public void refund(CaptainOffer offer) {
                        InventoryPayment.refund(inventory, current.paymentItem(), offer.cost());
                    }
                },
                profile -> profiles.save(guard, profile)
        );
        if (result.succeeded()) {
            CaptainEvents.AFTER.invoker().onChanged(context(owner, guard, current.request()), result);
        }
        return result;
    }

    private State prepare(ServerPlayer owner, GuardEntity guard, CaptainAction action) {
        boolean available = available(owner, guard);
        boolean owned = available && GuardOwnership.isOwnedBy(guard, owner.getUUID());
        GuardProfileReadResult profile = guard == null
                ? new GuardProfileReadResult.Absent()
                : profiles.read(guard);
        boolean officer = action == CaptainAction.DEMOTE
                && available
                && owned
                && RallyGuardApi.squads().find(owner, guard).officer();
        Item paymentItem = RallyConfig.hireItem();
        Optional<CaptainOffer> offer = action == CaptainAction.PROMOTE
                ? Optional.of(new CaptainOffer(
                        BuiltInRegistries.ITEM.getKey(paymentItem),
                        RallyConfig.captainPromotionCost()
                ))
                : Optional.empty();
        return new State(
                new CaptainMutationCoordinator.Request(
                        action,
                        RallyConfig.captainPromotionEnabled(),
                        available,
                        owned,
                        profile,
                        RallyConfig.captainPromotionMinimumLevel(),
                        officer,
                        offer
                ),
                paymentItem
        );
    }

    private static boolean available(ServerPlayer owner, GuardEntity guard) {
        return guard != null
                && guard.isAlive()
                && !guard.isRemoved()
                && guard.level() == owner.level();
    }

    private static CaptainContext context(
            ServerPlayer owner,
            GuardEntity guard,
            CaptainMutationCoordinator.Request request
    ) {
        GuardProfile profile = ((GuardProfileReadResult.Available) request.profile()).profile();
        return new CaptainContext(owner, guard, request.action(), profile, request.offer());
    }

    private record State(CaptainMutationCoordinator.Request request, Item paymentItem) {
    }
}
