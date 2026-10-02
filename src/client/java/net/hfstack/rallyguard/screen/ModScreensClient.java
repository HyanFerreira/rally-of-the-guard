package net.hfstack.rallyguard.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;

@Environment(EnvType.CLIENT)
public final class ModScreensClient {
    private ModScreensClient() {
    }

    public static void register() {
        MenuScreens.register(ModScreenHandlers.HIRE_HANDLER, HireGuardScreen::new);
    }
}
