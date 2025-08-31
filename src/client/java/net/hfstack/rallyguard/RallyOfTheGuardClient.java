package net.hfstack.rallyguard;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.screen.GuardCommandScreen;
import net.hfstack.rallyguard.screen.HireGuardScreen;
import net.hfstack.rallyguard.screen.ModScreenHandlers;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class RallyOfTheGuardClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Tela de contratação
        HandledScreens.register(ModScreenHandlers.HIRE_HANDLER, HireGuardScreen::new);

        // ❗NÃO registrar PayloadTypeRegistry aqui (já foi no common).
        // Apenas receiver S2C da lista de guardas:
        ClientPlayNetworking.registerGlobalReceiver(
                GuardListS2CPayload.ID,
                (payload, context) -> net.hfstack.rallyguard.screen.GuardCommandScreen.openFromPayload(payload)
        );
    }
}
