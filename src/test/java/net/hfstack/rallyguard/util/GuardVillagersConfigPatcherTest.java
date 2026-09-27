package net.hfstack.rallyguard.util;

import com.google.gson.JsonParser;
import dev.sterner.guardvillagers.GuardVillagersConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GuardVillagersConfigPatcherTest {
    @TempDir
    Path tempDir;

    @Test
    void missingConfigIsCreatedWithFollowHeroDisabled() throws IOException {
        Path file = tempDir.resolve("guardvillagers.json");

        assertEquals(GuardVillagersConfigPatcher.PatchResult.UPDATED,
                GuardVillagersConfigPatcher.patchFollowHeroConfig(file));
        assertFalse(JsonParser.parseString(Files.readString(file))
                .getAsJsonObject().get("followHero").getAsBoolean());
    }

    @Test
    void enabledFollowHeroIsDisabled() throws IOException {
        Path file = tempDir.resolve("guardvillagers.json");
        Files.writeString(file, "{\"followHero\":true,\"keepMe\":7}");

        assertEquals(GuardVillagersConfigPatcher.PatchResult.UPDATED,
                GuardVillagersConfigPatcher.patchFollowHeroConfig(file));
        var json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        assertFalse(json.get("followHero").getAsBoolean());
        assertEquals(7, json.get("keepMe").getAsInt());
    }

    @Test
    void disabledFollowHeroLeavesConfigUnchanged() throws IOException {
        Path file = tempDir.resolve("guardvillagers.json");
        Files.writeString(file, "{\"followHero\":false}");

        assertEquals(GuardVillagersConfigPatcher.PatchResult.UNCHANGED,
                GuardVillagersConfigPatcher.patchFollowHeroConfig(file));
    }

    @Test
    void alreadyLoadedFollowHeroRequirementIsDisabledImmediately() throws IOException {
        Path file = tempDir.resolve("guardvillagers.json");
        Files.writeString(file, "{\"followHero\":false}");
        boolean originalFollowHero = GuardVillagersConfig.followHero;

        try {
            GuardVillagersConfig.followHero = true;

            assertEquals(GuardVillagersConfigPatcher.PatchResult.UNCHANGED,
                    GuardVillagersConfigPatcher.patchFollowHeroConfig(file));
            assertFalse(GuardVillagersConfig.followHero);
        } finally {
            GuardVillagersConfig.followHero = originalFollowHero;
        }
    }

    @Test
    void malformedJsonReturnsFailedWithoutThrowing() throws IOException {
        Path file = tempDir.resolve("guardvillagers.json");
        Files.writeString(file, "{not-json");

        assertEquals(GuardVillagersConfigPatcher.PatchResult.FAILED,
                GuardVillagersConfigPatcher.patchFollowHeroConfig(file));
    }
}
