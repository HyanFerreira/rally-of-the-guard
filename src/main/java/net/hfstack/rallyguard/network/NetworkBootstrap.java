package net.hfstack.rallyguard.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.hfstack.rallyguard.network.payload.GuardActionC2SPayload;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.OpenGuardCommandC2SPayload;

public final class NetworkBootstrap {
    private NetworkBootstrap() {
    }

    private static boolean DONE = false;

    /**
     * Chame uma única vez no ModInitializer (lado comum).
     */
    public static void registerTypesOnce() {
        if (DONE) return;
        DONE = true;

        // C2S
        PayloadTypeRegistry.playC2S().register(OpenGuardCommandC2SPayload.ID, OpenGuardCommandC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GuardActionC2SPayload.ID, GuardActionC2SPayload.CODEC);

        // S2C
        PayloadTypeRegistry.playS2C().register(GuardListS2CPayload.ID, GuardListS2CPayload.CODEC);
    }
}
