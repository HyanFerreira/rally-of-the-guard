package net.hfstack.rallyguard.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.network.payload.OpenGuardCommandC2SPayload;

public final class ClientHooks {
    private ClientHooks() {
    }

    /**
     * Pede ao servidor a lista de guardas; ao chegar, o cliente abre a GUI.
     */
    public static void requestOpenGuardPanel() {
        ClientPlayNetworking.send(new OpenGuardCommandC2SPayload());
    }
}
