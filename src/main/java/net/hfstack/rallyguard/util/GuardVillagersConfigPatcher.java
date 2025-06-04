package net.hfstack.rallyguard.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class GuardVillagersConfigPatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger("RallyOfTheGuard");

    public static void patchFollowHeroConfig() {
        try {
            File configFile = new File("config/guardvillagers.json");

            if (!configFile.exists()) {
                LOGGER.warn("[RallyOfTheGuard] GuardVillagers config not found.");
                return;
            }

            FileReader reader = new FileReader(configFile);
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            reader.close();

            if (json.has("followHero")) {
                json.addProperty("followHero", false);
                FileWriter writer = new FileWriter(configFile);
                new Gson().toJson(json, writer);
                writer.close();
                LOGGER.info("[RallyOfTheGuard] ✅ Set 'followHero' to false in GuardVillagers config.");
            } else {
                LOGGER.warn("[RallyOfTheGuard] ⚠ 'followHero' not found in GuardVillagers config.");
            }
        } catch (Exception e) {
            LOGGER.error("[RallyOfTheGuard] ❌ Failed to modify GuardVillagers config", e);
        }
    }
}
