package net.hfstack.rallyguard.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hfstack.rallyguard.muster.MusterRollSessions;
import net.hfstack.rallyguard.muster.MusterCaptainActionHandler;
import net.hfstack.rallyguard.network.payload.CaptainActionC2SPayload;
import net.hfstack.rallyguard.network.payload.CloseMusterRollC2SPayload;

public final class MusterRollNetworking {
    private MusterRollNetworking() {
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(CloseMusterRollC2SPayload.ID, (payload, context) ->
                context.server().execute(() -> MusterRollSessions.close(context.player()))
        );
        ServerPlayNetworking.registerGlobalReceiver(CaptainActionC2SPayload.ID, (payload, context) ->
                context.server().execute(() ->
                        MusterCaptainActionHandler.handle(context.player(), payload.action()))
        );
    }
}
