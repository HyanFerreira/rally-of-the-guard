package net.hfstack.rallyguard.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.hfstack.rallyguard.RallyOfTheGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Garante que config/guardvillagers.json tenha followHero=false
 * para liberar o botão "Seguir" sem exigir Herói da Vila.
 */
public final class GuardVillagersConfigPatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(RallyOfTheGuard.MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private GuardVillagersConfigPatcher() {
    }

    public static void patchFollowHeroConfig() {
        try {
            Path cfgDir = FabricLoader.getInstance().getConfigDir();
            Path file = cfgDir.resolve("guardvillagers.json");

            JsonObject root;
            if (Files.exists(file)) {
                try (Reader r = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
                    root = JsonParser.parseReader(r).getAsJsonObject();
                }
            } else {
                root = new JsonObject(); // cria novo se não existir
            }

            // seta followHero=false se estiver ausente ou true
            if (!root.has("followHero") || root.get("followHero").getAsBoolean()) {
                root.addProperty("followHero", false);
                try (Writer w = new OutputStreamWriter(Files.newOutputStream(file), StandardCharsets.UTF_8)) {
                    GSON.toJson(root, w);
                }
                LOGGER.info("[{}] Config do GuardVillagers ajustada: followHero=false", RallyOfTheGuard.MOD_ID);
            } else {
                LOGGER.info("[{}] Config do GuardVillagers já está com followHero=false", RallyOfTheGuard.MOD_ID);
            }
        } catch (Exception e) {
            LOGGER.error("[{}] Falha ao ajustar followHero no GuardVillagers", RallyOfTheGuard.MOD_ID, e);
        }
    }
}
