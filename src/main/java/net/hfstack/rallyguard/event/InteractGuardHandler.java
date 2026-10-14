package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.recruitment.RecruitmentQuote;
import net.hfstack.rallyguard.api.recruitment.RecruitmentQuoteResult;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.item.ModItems;
import net.hfstack.rallyguard.muster.MusterRollSessions;
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
            if (!GuardOwnership.isGuard(entity)) return InteractionResult.PASS;
            if (player.getItemInHand(hand).is(ModItems.MUSTER_ROLL)) {
                if (world.isClientSide()) return InteractionResult.SUCCESS;
                if (player instanceof ServerPlayer serverPlayer && entity instanceof GuardEntity guard) {
                    if (MusterRollSessions.open(serverPlayer, guard)) return InteractionResult.SUCCESS;
                    serverPlayer.sendOverlayMessage(Component.translatable("gui.rallyguard.muster.invalid"));
                    return InteractionResult.FAIL;
                }
                return InteractionResult.PASS;
            }
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (world.isClientSide()) return InteractionResult.PASS;

            // Se já tem dono, deixa o GuardVillagers tratar (inventário/seguir/patrulhar).
            if (GuardOwnership.hasOwner(entity)) return InteractionResult.PASS;

            // Se NÃO tem dono, abre nossa tela de contratação
            if (!(player instanceof ServerPlayer serverPlayer) || !(entity instanceof GuardEntity guard)) {
                return InteractionResult.PASS;
            }
            RecruitmentQuoteResult quoted = RallyGuardApi.guardRecruitment().quote(serverPlayer, guard);
            if (!quoted.available()) {
                serverPlayer.sendOverlayMessage(quoted.feedback());
                return InteractionResult.FAIL;
            }
            RecruitmentQuote quote = quoted.quote().orElseThrow();
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (syncId, inv, p) -> new HireGuardScreenHandler(syncId, inv, entity.getId(), quote),
                    Component.translatable("gui.rallyguard.hire.title")
            ));
            return InteractionResult.SUCCESS;
        });
    }
}
