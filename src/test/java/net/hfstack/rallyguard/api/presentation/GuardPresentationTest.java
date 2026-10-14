package net.hfstack.rallyguard.api.presentation;

import net.hfstack.rallyguard.profile.GuardProfile;
import net.hfstack.rallyguard.profile.GuardProfileOrigin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

import java.util.Optional;

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

    @Test
    void profileRankIsUsedAsFallback() {
        GuardProfile profile = GuardProfile.createV2(
                GuardProfileOrigin.NATURAL,
                340_000,
                5_000,
                0,
                0,
                0,
                0,
                0,
                0,
                false
        );

        GuardPresentation resolved = GuardPresentationRegistry.withProfileFallback(
                GuardPresentation.empty(),
                Optional.of(profile)
        );

        TranslatableContents contents = (TranslatableContents) resolved.rank()
                .orElseThrow()
                .getContents();
        assertEquals("rank.rallyguard.veteran", contents.getKey());
    }

    @Test
    void externalRankOverridesProfileFallback() {
        GuardPresentation resolved = GuardPresentationRegistry.withProfileFallback(
                GuardPresentation.of(Component.literal("Captain"), null),
                Optional.of(GuardProfile.legacyNeutral())
        );

        assertEquals("Captain", resolved.rank().orElseThrow().getString());
    }
}
