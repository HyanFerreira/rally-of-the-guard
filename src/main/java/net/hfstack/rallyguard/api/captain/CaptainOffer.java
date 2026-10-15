package net.hfstack.rallyguard.api.captain;

import net.minecraft.resources.Identifier;

import java.util.Objects;

public record CaptainOffer(Identifier paymentItemId, int cost) {
    public CaptainOffer {
        Objects.requireNonNull(paymentItemId, "paymentItemId");
        if (cost < 0) {
            throw new IllegalArgumentException("Captain promotion cost cannot be negative");
        }
    }
}
