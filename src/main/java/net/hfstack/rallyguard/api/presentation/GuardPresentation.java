package net.hfstack.rallyguard.api.presentation;

import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Read-only labels contributed to Rally's guard screens.
 */
public record GuardPresentation(Optional<Component> rank, Optional<Component> settlement) {
    public GuardPresentation {
        rank = Objects.requireNonNull(rank, "rank");
        settlement = Objects.requireNonNull(settlement, "settlement");
    }

    public static GuardPresentation empty() {
        return new GuardPresentation(Optional.empty(), Optional.empty());
    }

    public static GuardPresentation of(Component rank, Component settlement) {
        return new GuardPresentation(Optional.ofNullable(rank), Optional.ofNullable(settlement));
    }

    public GuardPresentation merge(GuardPresentation other) {
        Objects.requireNonNull(other, "other");
        return new GuardPresentation(
                rank.isPresent() ? rank : other.rank,
                settlement.isPresent() ? settlement : other.settlement
        );
    }
}
