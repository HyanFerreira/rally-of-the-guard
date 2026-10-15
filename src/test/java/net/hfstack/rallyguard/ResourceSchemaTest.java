package net.hfstack.rallyguard;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceSchemaTest {
    private static final Path RESOURCES = Path.of("src", "main", "resources");

    @Test
    void recipesProduceRegisteredRallyItems() throws IOException {
        assertRecipeResult("scroll_of_rallying", "rallyguard:scroll_of_rallying");
        assertRecipeResult("commanders_ledger", "rallyguard:commanders_ledger");

        JsonObject muster = readJson(RESOURCES.resolve("data/rallyguard/recipe/muster_roll.json"));
        assertEquals("minecraft:crafting_shapeless", muster.get("type").getAsString());
        assertEquals("rallyguard:muster_roll", muster.getAsJsonObject("result").get("id").getAsString());
        assertEquals(3, muster.getAsJsonArray("ingredients").size());
    }

    @Test
    void itemDefinitionsResolveToExistingModelsAndTextures() throws IOException {
        assertItemResources("scroll_of_rallying", "rallyguard:item/scroll_of_rallying");
        assertItemResources("commanders_ledger", "rallyguard:item/commanders_ledger");
        assertItemResources("muster_roll", "rallyguard:item/muster_roll");
    }

    @Test
    void guardVillagersSpawnEggOverrideIsPackaged() {
        assertTrue(Files.isRegularFile(RESOURCES.resolve(
                "assets/guardvillagers/models/item/guard_spawn_egg.json"
        )));
    }

    @Test
    void commonMixinConfigurationIsDeclaredAndPackaged() throws IOException {
        JsonObject metadata = readJson(RESOURCES.resolve("fabric.mod.json"));
        assertTrue(metadata.getAsJsonArray("mixins").asList().stream()
                .anyMatch(element -> "rallyguard.mixins.json".equals(element.getAsString())));
        JsonObject mixins = readJson(RESOURCES.resolve("rallyguard.mixins.json"));
        assertEquals("net.hfstack.rallyguard.mixin", mixins.get("package").getAsString());
        assertTrue(mixins.getAsJsonArray("mixins").asList().stream()
                .anyMatch(element -> "GuardEntityMixin".equals(element.getAsString())));
    }

    @Test
    void combatProgressionEntityTagsArePackaged() throws IOException {
        Path tags = RESOURCES.resolve("data/rallyguard/tags/entity_type");
        JsonObject targets = readJson(tags.resolve("xp_targets.json"));
        assertEquals(6, targets.getAsJsonArray("values").size());
        assertTrue(targets.getAsJsonArray("values").asList().stream()
                .allMatch(value -> value.getAsString().startsWith("#rallyguard:xp/")));

        assertEquals(0, readJson(tags.resolve("no_xp.json")).getAsJsonArray("values").size());
        for (String category : new String[]{"common", "dangerous", "raid", "elite", "raid_elite", "boss"}) {
            assertTrue(readJson(tags.resolve("xp/" + category + ".json"))
                    .getAsJsonArray("values").size() > 0);
        }
    }

    @Test
    void naturalCaptainLabelsExistInBothSupportedLanguages() throws IOException {
        JsonObject english = readJson(RESOURCES.resolve("assets/rallyguard/lang/en_us.json"));
        JsonObject portuguese = readJson(RESOURCES.resolve("assets/rallyguard/lang/pt_br.json"));

        assertEquals("Captain", english.get("gui.rallyguard.muster.captain").getAsString());
        assertEquals("Capitão", portuguese.get("gui.rallyguard.muster.captain").getAsString());
        assertTrue(english.has("rallyguard.midnightconfig.captainSpawnEnabled"));
        assertTrue(portuguese.has("rallyguard.midnightconfig.captainSpawnEnabled"));
        assertTrue(english.has("rallyguard.midnightconfig.captainHireCostBonus"));
        assertTrue(portuguese.has("rallyguard.midnightconfig.captainHireCostBonus"));
    }

    @Test
    void captainPromotionLabelsFeedbackAndConfigurationExistInBothLanguages() throws IOException {
        JsonObject english = readJson(RESOURCES.resolve("assets/rallyguard/lang/en_us.json"));
        JsonObject portuguese = readJson(RESOURCES.resolve("assets/rallyguard/lang/pt_br.json"));
        Set<String> keys = Set.of(
                "rallyguard.midnightconfig.captainPromotionEnabled",
                "rallyguard.midnightconfig.captainPromotionEnabled.tooltip",
                "rallyguard.midnightconfig.captainPromotionMinimumLevel",
                "rallyguard.midnightconfig.captainPromotionMinimumLevel.tooltip",
                "rallyguard.midnightconfig.captainPromotionCost",
                "rallyguard.midnightconfig.captainPromotionCost.tooltip",
                "gui.rallyguard.captain.promote_button",
                "gui.rallyguard.captain.demote_button",
                "gui.rallyguard.captain.promoted",
                "gui.rallyguard.captain.demoted",
                "gui.rallyguard.captain.promotion_disabled",
                "gui.rallyguard.captain.unavailable",
                "gui.rallyguard.captain.not_owner",
                "gui.rallyguard.captain.unsupported_profile",
                "gui.rallyguard.captain.already_captain",
                "gui.rallyguard.captain.not_captain",
                "gui.rallyguard.captain.level_too_low",
                "gui.rallyguard.captain.insufficient_payment",
                "gui.rallyguard.captain.active_officer",
                "gui.rallyguard.captain.persistence_error",
                "gui.rallyguard.captain.denied"
        );
        for (String key : keys) {
            assertTrue(english.has(key), () -> "Missing English Captain key: " + key);
            assertTrue(portuguese.has(key), () -> "Missing Portuguese Captain key: " + key);
        }
    }

    @Test
    void captainPromotionReleaseDocumentationTargetsApiFiveAndVersionOneFour() throws IOException {
        String api = Files.readString(Path.of("docs", "API.md"));
        String readme = Files.readString(Path.of("README.md"));
        String changelog = Files.readString(Path.of("CHANGELOG.md"));
        String roadmap = Files.readString(Path.of("ROADMAP.md"));
        Path smokePath = Path.of("docs", "testing", "26.1-captain-promotion-smoke-test.md");

        assertTrue(api.contains("API contract version: `5`"));
        assertTrue(api.contains("RallyGuardApi.captains()"));
        assertTrue(readme.contains("Level 7"));
        assertTrue(readme.contains("4 unidades"));
        assertTrue(changelog.contains("## Unreleased — planned 1.4.0"));
        assertTrue(roadmap.contains("- [x] **Captain Promotion & Demotion**"));
        assertTrue(Files.isRegularFile(smokePath));
        if (Files.isRegularFile(smokePath)) {
            String smoke = Files.readString(smokePath);
            assertTrue(smoke.contains("Level 7"));
            assertTrue(smoke.contains("4"));
            assertTrue(smoke.contains("Officer"));
        }

        for (Path releaseMetadata : new Path[]{
                Path.of("gradle.properties"),
                Path.of("CHANGELOG.md"),
                Path.of("README.md"),
                Path.of("docs", "API.md"),
                Path.of("ROADMAP.md")
        }) {
            assertFalse(Files.readString(releaseMetadata).contains("1.5.0"),
                    () -> "Unexpected 1.5.0 reference in " + releaseMetadata);
        }
    }

    @Test
    void additionalFormationResourcesAndReleaseDocumentationAreComplete() throws IOException {
        JsonObject english = readJson(RESOURCES.resolve("assets/rallyguard/lang/en_us.json"));
        JsonObject portuguese = readJson(RESOURCES.resolve("assets/rallyguard/lang/pt_br.json"));
        Set<String> keys = Set.of(
                "tooltip.rallyguard.scroll_of_rallying.formation",
                "alert.rallyguard.scroll_of_rallying.formation_changed",
                "rallyguard.midnightconfig.formationCircleRadius",
                "rallyguard.midnightconfig.formationCircleRadius.tooltip",
                "rallyguard.midnightconfig.formationHeadingMargin",
                "rallyguard.midnightconfig.formationHeadingMargin.tooltip",
                "formation.rallyguard.escort",
                "formation.rallyguard.column",
                "formation.rallyguard.line",
                "formation.rallyguard.wedge",
                "formation.rallyguard.square",
                "formation.rallyguard.circle",
                "formation.rallyguard.shield_wall"
        );
        for (String key : keys) {
            assertTrue(english.has(key), () -> "Missing English formation key: " + key);
            assertTrue(portuguese.has(key), () -> "Missing Portuguese formation key: " + key);
        }

        String readme = Files.readString(Path.of("README.md"));
        String changelog = Files.readString(Path.of("CHANGELOG.md"));
        String roadmap = Files.readString(Path.of("ROADMAP.md"));
        Path smokePath = Path.of("docs", "testing", "26.1-additional-formations-smoke-test.md");

        assertTrue(readme.contains("Muralha de Escudos"));
        assertTrue(changelog.contains("Shield Wall"));
        assertTrue(roadmap.contains("- [x] **Additional formations**"));
        assertTrue(Files.isRegularFile(smokePath));
    }

    @Test
    void squadLabelsAndConfigurationExistInBothSupportedLanguages() throws IOException {
        JsonObject english = readJson(RESOURCES.resolve("assets/rallyguard/lang/en_us.json"));
        JsonObject portuguese = readJson(RESOURCES.resolve("assets/rallyguard/lang/pt_br.json"));
        Set<String> keys = Set.of(
                "rallyguard.midnightconfig.category.squads",
                "rallyguard.midnightconfig.squadsEnabled",
                "rallyguard.midnightconfig.maxSquadsPerPlayer",
                "rallyguard.midnightconfig.maxMembersPerSquad",
                "rallyguard.midnightconfig.maxSquadNameLength",
                "gui.rallyguard.squad.manage",
                "gui.rallyguard.squad.manage_title",
                "gui.rallyguard.command.squad",
                "gui.rallyguard.squad.scope_all",
                "gui.rallyguard.squad.scope_unassigned",
                "gui.rallyguard.squad.create",
                "gui.rallyguard.squad.rename",
                "gui.rallyguard.squad.delete",
                "gui.rallyguard.squad.assign",
                "gui.rallyguard.squad.unassign",
                "gui.rallyguard.squad.make_officer",
                "gui.rallyguard.squad.clear_officer",
                "gui.rallyguard.squad.batch_result",
                "gui.rallyguard.squad.officer_not_captain"
        );
        for (String key : keys) {
            assertTrue(english.has(key), () -> "Missing English Squad key: " + key);
            assertTrue(portuguese.has(key), () -> "Missing Portuguese Squad key: " + key);
        }
    }

    private static void assertRecipeResult(String recipeName, String expectedItemId) throws IOException {
        JsonObject recipe = readJson(RESOURCES.resolve(
                "data/rallyguard/recipe/" + recipeName + ".json"
        ));

        assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString());
        assertEquals("equipment", recipe.get("category").getAsString());
        assertEquals(expectedItemId, recipe.getAsJsonObject("result").get("id").getAsString());
        for (Map.Entry<String, com.google.gson.JsonElement> ingredient : recipe.getAsJsonObject("key").entrySet()) {
            assertTrue(ingredient.getValue().isJsonPrimitive(),
                    () -> "Ingredient " + ingredient.getKey() + " must use the 26.1 string syntax");
        }
    }

    private static void assertItemResources(String itemName, String expectedModelId) throws IOException {
        JsonObject definition = readJson(RESOURCES.resolve(
                "assets/rallyguard/items/" + itemName + ".json"
        ));
        String modelId = definition.getAsJsonObject("model").get("model").getAsString();
        assertEquals(expectedModelId, modelId);

        Path modelPath = resourcePath("models", modelId, ".json");
        assertTrue(Files.isRegularFile(modelPath), () -> "Missing model: " + modelPath);

        JsonObject model = readJson(modelPath);
        String textureId = model.getAsJsonObject("textures").get("layer0").getAsString();
        Path texturePath = resourcePath("textures", textureId, ".png");
        assertTrue(Files.isRegularFile(texturePath), () -> "Missing texture: " + texturePath);
    }

    private static Path resourcePath(String resourceType, String identifier, String extension) {
        String[] parts = identifier.split(":", 2);
        assertEquals(2, parts.length, "Resource identifier must include a namespace");
        return RESOURCES.resolve("assets")
                .resolve(parts[0])
                .resolve(resourceType)
                .resolve(parts[1] + extension);
    }

    private static JsonObject readJson(Path path) throws IOException {
        assertTrue(Files.isRegularFile(path), () -> "Missing JSON resource: " + path);
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
