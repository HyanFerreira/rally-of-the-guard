package net.hfstack.rallyguard.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hfstack.rallyguard.component.ModComponents;
import net.hfstack.rallyguard.effect.ModEffects;
import net.hfstack.rallyguard.event.RallyFormationTicker;
import net.hfstack.rallyguard.formation.FormationSelectionPolicy;
import net.hfstack.rallyguard.item.ModItems;
import net.hfstack.rallyguard.network.payload.FormationSelectionC2SPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public final class FormationSelectionNetworking {
    private FormationSelectionNetworking() {
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(FormationSelectionC2SPayload.ID, (payload, context) ->
                context.server().execute(() -> handle(context.player(), payload))
        );
    }

    private static void handle(ServerPlayer player, FormationSelectionC2SPayload payload) {
        boolean mainHandHoldsScroll = player.getMainHandItem().is(ModItems.SCROLL_OF_RALLYING);
        boolean offHandHoldsScroll = player.getOffhandItem().is(ModItems.SCROLL_OF_RALLYING);
        FormationSelectionPolicy.resolve(
                payload.handId(),
                payload.formationId(),
                mainHandHoldsScroll,
                offHandHoldsScroll
        ).ifPresent(selection -> apply(player, selection));
    }

    private static void apply(ServerPlayer player, FormationSelectionPolicy.Selection selection) {
        InteractionHand hand = selection.handId() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);
        stack.set(ModComponents.FORMATION, selection.formation().id());

        if (stack.getOrDefault(ModComponents.ACTIVE, false)
                && player.hasEffect(ModEffects.RALLY_COMMANDER)) {
            RallyFormationTicker.updateFormation(player, selection.formation());
        }

        player.sendSystemMessage(Component.translatable(
                "alert.rallyguard.scroll_of_rallying.formation_changed",
                Component.translatable(selection.formation().translationKey())
        ));
        player.getCooldowns().addCooldown(stack, 10);
    }
}
