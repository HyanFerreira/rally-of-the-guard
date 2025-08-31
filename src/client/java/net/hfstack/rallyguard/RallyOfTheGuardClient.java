package net.hfstack.rallyguard;

import net.fabricmc.api.ClientModInitializer;
import net.hfstack.rallyguard.screen.ModScreensClient;

public class RallyOfTheGuardClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModScreensClient.register();
    }
}
