package net.hfstack.rallyguard.event;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.screen.HireGuardScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public final class InteractGuardHandler {
    private InteractGuardHandler() {
    }

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient || hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (!GuardOwnership.isGuard(entity)) return ActionResult.PASS;

            // Se já tem dono, deixa o GuardVillagers tratar (inventário/seguir/patrulhar).
            if (GuardOwnership.hasOwner(entity)) return ActionResult.PASS;

            // Se NÃO tem dono, abre nossa tela de contratação
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, inv, p) -> new HireGuardScreenHandler(syncId, inv, entity.getId()),
                    Text.translatable("gui.rallyguard.hire.title")
            ));
            return ActionResult.SUCCESS;
        });
    }
}
