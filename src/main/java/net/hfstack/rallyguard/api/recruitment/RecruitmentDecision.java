package net.hfstack.rallyguard.api.recruitment;

import net.minecraft.text.Text;

import java.util.Objects;

/**
 * Result returned by a recruitment policy listener.
 */
public sealed interface RecruitmentDecision {
    record Allow(RecruitmentOffer offer) implements RecruitmentDecision {
        public Allow {
            Objects.requireNonNull(offer, "offer");
        }
    }

    record Deny(Text reason) implements RecruitmentDecision {
        public Deny {
            Objects.requireNonNull(reason, "reason");
        }
    }

    static RecruitmentDecision allow(RecruitmentOffer offer) {
        return new Allow(offer);
    }

    static RecruitmentDecision deny(Text reason) {
        return new Deny(reason);
    }
}
