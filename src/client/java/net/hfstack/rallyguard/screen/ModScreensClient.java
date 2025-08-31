package net.hfstack.rallyguard.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

@Environment(EnvType.CLIENT)
public final class ModScreensClient {
    private ModScreensClient() {
    }

    public static void register() {
        HandledScreens.register(ModScreenHandlers.HIRE_HANDLER, HireGuardScreen::new);
    }
}
