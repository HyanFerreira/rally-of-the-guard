package net.hfstack.rallyguard.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.hfstack.rallyguard.network.payload.OpenGuardCommandC2SPayload;
import net.hfstack.rallyguard.formation.RallyFormation;
import net.hfstack.rallyguard.screen.FormationSelectionScreen;
import net.minecraft.client.Minecraft;

public final class ClientHooks {
    private ClientHooks() {
    }

    /**
     * Pede ao servidor a lista de guardas; ao chegar, o cliente abre a GUI.
     */
    public static void requestOpenGuardPanel() {
        ClientPlayNetworking.send(new OpenGuardCommandC2SPayload());
    }

    public static void openFormationHub(int handId, String formationId) {
        Minecraft.getInstance().setScreen(new FormationSelectionScreen(
                handId,
                RallyFormation.fromId(formationId)
        ));
    }
}
