package net.hfstack.rallyguard.api.recruitment;

import net.minecraft.util.Identifier;

import java.util.Objects;

/**
 * The personal inventory payment Rally will collect if recruitment succeeds.
 */
public record RecruitmentOffer(Identifier paymentItemId, int cost) {
    public RecruitmentOffer {
        Objects.requireNonNull(paymentItemId, "paymentItemId");
        if (cost < 0) {
            throw new IllegalArgumentException("Recruitment cost cannot be negative");
        }
    }
}
