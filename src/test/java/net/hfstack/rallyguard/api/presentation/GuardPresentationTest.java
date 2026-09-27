package net.hfstack.rallyguard.api.presentation;

import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuardPresentationTest {
    @Test
    void mergeKeepsFirstLabelAndFillsMissingFields() {
        GuardPresentation rank = GuardPresentation.of(Text.literal("Captain"), null);
        GuardPresentation settlement = GuardPresentation.of(Text.literal("Ignored"), Text.literal("Oakwatch"));

        GuardPresentation merged = rank.merge(settlement);

        assertEquals("Captain", merged.rank().orElseThrow().getString());
        assertEquals("Oakwatch", merged.settlement().orElseThrow().getString());
    }
}
