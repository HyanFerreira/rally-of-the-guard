package net.hfstack.rallyguard.client.move;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.api.command.GuardUnitCategory;
import net.hfstack.rallyguard.item.ModItems;
import net.hfstack.rallyguard.network.MoveHereC2SPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

public final class MoveTargetingClient {
    private static final MoveTargetingSession SESSION = new MoveTargetingSession();
    private static long clientTick;

    private MoveTargetingClient() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(MoveTargetingClient::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> SESSION.reset());
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!world.isClientSide() || !SESSION.isArmed()) return InteractionResult.PASS;
            MoveTargetingSession.Armed armed = SESSION.consume(
                    clientTick,
                    player.isAlive(),
                    player.getItemInHand(handById(SESSION.current().orElseThrow().handId())).is(ModItems.COMMANDERS_LEDGER)
            ).orElse(null);
            if (armed == null) return InteractionResult.PASS;

            ClientPlayNetworking.send(new MoveHereC2SPayload(
                    armed.scope(),
                    armed.category().wireId(),
                    hit.getBlockPos(),
                    hit.getDirection().get3DDataValue()
            ));
            return InteractionResult.FAIL;
        });
    }

    public static void arm(GuardCommandScope scope, GuardUnitCategory category) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        int handId;
        if (client.player.getMainHandItem().is(ModItems.COMMANDERS_LEDGER)) handId = 0;
        else if (client.player.getOffhandItem().is(ModItems.COMMANDERS_LEDGER)) handId = 1;
        else {
            overlay(client, "gui.rallyguard.move.cancelled");
            return;
        }
        SESSION.arm(scope, category, handId, clientTick);
        overlay(client, "gui.rallyguard.move.target_prompt");
    }

    private static void tick(Minecraft client) {
        clientTick++;
        if (!SESSION.isArmed()) return;
        if (client.player == null) {
            SESSION.reset();
            return;
        }
        if (client.screen != null) {
            if (SESSION.cancel()) {
                overlay(client, "gui.rallyguard.move.cancelled");
            }
            return;
        }

        MoveTargetingSession.Armed armed = SESSION.current().orElseThrow();
        boolean ledgerHeld = client.player.getItemInHand(handById(armed.handId())).is(ModItems.COMMANDERS_LEDGER);
        MoveTargetingSession.Update update = SESSION.update(clientTick, client.player.isAlive(), ledgerHeld);
        if (update == MoveTargetingSession.Update.EXPIRED) {
            overlay(client, "gui.rallyguard.move.expired");
        } else if (update == MoveTargetingSession.Update.CANCELLED) {
            overlay(client, "gui.rallyguard.move.cancelled");
        }
    }

    private static void overlay(Minecraft client, String translationKey) {
        client.gui.setOverlayMessage(Component.translatable(translationKey), false);
    }

    private static InteractionHand handById(int handId) {
        return handId == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }
}
