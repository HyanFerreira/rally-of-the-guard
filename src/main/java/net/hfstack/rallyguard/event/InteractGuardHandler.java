package net.hfstack.rallyguard.event;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.screen.HireGuardScreenHandler;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;

public final class InteractGuardHandler {
    private InteractGuardHandler() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!GuardOwnership.isGuard(entity)) return InteractionResult.PASS;

            // Se já tem dono, deixa o GuardVillagers tratar (inventário/seguir/patrulhar).
            if (GuardOwnership.hasOwner(entity)) return InteractionResult.PASS;

            // Se NÃO tem dono, abre nossa tela de contratação
            if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (syncId, inv, p) -> new HireGuardScreenHandler(syncId, inv, entity.getId()),
                    Component.translatable("gui.rallyguard.hire.title")
            ));
            return InteractionResult.SUCCESS;
        });
    }
}
