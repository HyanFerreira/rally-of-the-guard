package net.hfstack.rallyguard.network;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.command.GuardBatchCommandResult;
import net.hfstack.rallyguard.api.command.GuardCombatStance;
import net.hfstack.rallyguard.service.GuardCommandTargets;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CombatStanceNetworking {
    private static boolean registered;

    private CombatStanceNetworking() {
    }

    public static synchronized void registerServer() {
        if (registered) return;
        registered = true;
        ServerPlayNetworking.registerGlobalReceiver(SetCombatStanceC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handle(player, payload));
        });
    }

    private static void handle(ServerPlayer player, SetCombatStanceC2SPayload payload) {
        GuardCombatStance stance = CombatStanceRequestPolicy.resolve(
                payload.stanceId(),
                GuardCommandTargets.validScope(player, payload.scope())
        ).orElse(null);
        if (stance == null) {
            player.sendOverlayMessage(Component.translatable("gui.rallyguard.stance.invalid"));
            return;
        }

        GuardBatchCommandResult result = CombatStanceBatchPolicy.execute(
                GuardCommandTargets.loadedScopedGuards(player, payload.scope()),
                GuardEntity::getUUID,
                guard -> RallyGuardApi.guardCommands().setCombatStance(player, guard, stance)
        );
        player.sendOverlayMessage(Component.translatable(
                "gui.rallyguard.squad.batch_result",
                result.succeeded(),
                result.rejected(),
                result.unavailable()
        ));
    }
}
