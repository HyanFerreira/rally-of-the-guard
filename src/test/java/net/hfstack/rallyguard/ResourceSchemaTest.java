package net.hfstack.rallyguard;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceSchemaTest {
    private static final Path RESOURCES = Path.of("src", "main", "resources");

    @Test
    void recipesProduceRegisteredRallyItems() throws IOException {
        assertRecipeResult("scroll_of_rallying", "rallyguard:scroll_of_rallying");
        assertRecipeResult("commanders_ledger", "rallyguard:commanders_ledger");
    }

    @Test
    void itemDefinitionsResolveToExistingModelsAndTextures() throws IOException {
        assertItemResources("scroll_of_rallying", "rallyguard:item/scroll_of_rallying");
        assertItemResources("commanders_ledger", "rallyguard:item/commanders_ledger");
    }

    @Test
    void guardVillagersSpawnEggOverrideIsPackaged() {
        assertTrue(Files.isRegularFile(RESOURCES.resolve(
                "assets/guardvillagers/models/item/guard_spawn_egg.json"
        )));
    }

    private static void assertRecipeResult(String recipeName, String expectedItemId) throws IOException {
        JsonObject recipe = readJson(RESOURCES.resolve(
                "data/rallyguard/recipes/" + recipeName + ".json"
        ));

        assertEquals(expectedItemId, recipe.getAsJsonObject("result").get("id").getAsString());
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
