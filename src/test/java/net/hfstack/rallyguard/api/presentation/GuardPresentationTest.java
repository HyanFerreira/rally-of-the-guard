package net.hfstack.rallyguard.api.presentation;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuardPresentationTest {
    @Test
    void mergeKeepsFirstLabelAndFillsMissingFields() {
        GuardPresentation rank = GuardPresentation.of(Component.literal("Captain"), null);
        GuardPresentation settlement = GuardPresentation.of(Component.literal("Ignored"), Component.literal("Oakwatch"));

        GuardPresentation merged = rank.merge(settlement);

        assertEquals("Captain", merged.rank().orElseThrow().getString());
        assertEquals("Oakwatch", merged.settlement().orElseThrow().getString());
    }
}
