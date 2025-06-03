package net.hfstack.rallyguard;

import net.fabricmc.api.ClientModInitializer;
import net.hfstack.rallyguard.event.EmeraldContractHandler;

public class RallyOfTheGuardClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EmeraldContractHandler.register();
	}
}