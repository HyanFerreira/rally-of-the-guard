package net.hfstack.rallyguard.api.recruitment;

import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.List;

/**
 * Result returned by a recruitment policy listener.
 */
public sealed interface RecruitmentDecision {
    record Allow(RecruitmentOffer offer, List<RecruitmentTransaction> transactions) implements RecruitmentDecision {
        public Allow {
            Objects.requireNonNull(offer, "offer");
            transactions = List.copyOf(Objects.requireNonNull(transactions, "transactions"));
        }

        public Allow(RecruitmentOffer offer) {
            this(offer, List.of());
        }
    }

    record Deny(Component reason) implements RecruitmentDecision {
        public Deny {
            Objects.requireNonNull(reason, "reason");
        }
    }

    static RecruitmentDecision allow(RecruitmentOffer offer) {
        return new Allow(offer);
    }

    static RecruitmentDecision transactional(RecruitmentOffer offer, RecruitmentTransaction transaction) {
        return new Allow(offer, List.of(Objects.requireNonNull(transaction, "transaction")));
    }

    static RecruitmentDecision deny(Component reason) {
        return new Deny(reason);
    }
}
