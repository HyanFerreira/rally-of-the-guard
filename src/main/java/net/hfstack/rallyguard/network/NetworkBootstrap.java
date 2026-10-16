package net.hfstack.rallyguard.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.hfstack.rallyguard.network.payload.GuardActionC2SPayload;
import net.hfstack.rallyguard.network.payload.GuardAttackTargetC2SPayload;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.network.payload.GuardRouteUpdateC2SPayload;
import net.hfstack.rallyguard.network.payload.OpenGuardCommandC2SPayload;
import net.hfstack.rallyguard.network.payload.CloseMusterRollC2SPayload;
import net.hfstack.rallyguard.network.payload.CloseMusterRollS2CPayload;
import net.hfstack.rallyguard.network.payload.CaptainActionC2SPayload;
import net.hfstack.rallyguard.network.payload.MusterRollSnapshotS2CPayload;
import net.hfstack.rallyguard.network.payload.GuardSquadAssignmentC2SPayload;
import net.hfstack.rallyguard.network.payload.ScopedGuardActionC2SPayload;
import net.hfstack.rallyguard.network.payload.SquadDefinitionC2SPayload;
import net.hfstack.rallyguard.network.payload.SquadOfficerC2SPayload;
import net.hfstack.rallyguard.network.payload.FormationSelectionC2SPayload;

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
        PayloadTypeRegistry.serverboundPlay().register(OpenGuardCommandC2SPayload.ID, OpenGuardCommandC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GuardActionC2SPayload.ID, GuardActionC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GuardRouteUpdateC2SPayload.ID, GuardRouteUpdateC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GuardAttackTargetC2SPayload.ID, GuardAttackTargetC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SquadDefinitionC2SPayload.ID, SquadDefinitionC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GuardSquadAssignmentC2SPayload.ID, GuardSquadAssignmentC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SquadOfficerC2SPayload.ID, SquadOfficerC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ScopedGuardActionC2SPayload.ID, ScopedGuardActionC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CloseMusterRollC2SPayload.ID, CloseMusterRollC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CaptainActionC2SPayload.ID, CaptainActionC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(FormationSelectionC2SPayload.ID, FormationSelectionC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MoveHereC2SPayload.ID, MoveHereC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(DefendAreaC2SPayload.ID, DefendAreaC2SPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SetCombatStanceC2SPayload.ID, SetCombatStanceC2SPayload.CODEC);

        // S2C
        PayloadTypeRegistry.clientboundPlay().register(GuardListS2CPayload.ID, GuardListS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MusterRollSnapshotS2CPayload.ID, MusterRollSnapshotS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CloseMusterRollS2CPayload.ID, CloseMusterRollS2CPayload.CODEC);
    }
}
