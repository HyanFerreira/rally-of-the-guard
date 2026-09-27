package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.order.GuardOrders;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;

public final class RallyFriendlyFireHandler {
    private RallyFriendlyFireHandler() {
    }

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, target, hit) -> {
            if (world.isClientSide()) return InteractionResult.PASS;
            if (!RallyConfig.rallyProtectRalliedGuardsFromOwner()) return InteractionResult.PASS;
            if (!GuardOwnership.isGuard(target)) return InteractionResult.PASS;
            if (!GuardOwnership.isOwnedBy(target, player.getUUID())) return InteractionResult.PASS;
            if (!isRallied(target)) return InteractionResult.PASS;
            return InteractionResult.FAIL;
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((LivingEntity victim, net.minecraft.world.damagesource.DamageSource source, float amount) -> {
            if (!RallyConfig.rallyProtectRalliedGuardsFromOwner()) return true;
            if (!GuardOwnership.isGuard(victim)) return true;

            Player attackerPlayer = null;
            Entity attacker = source.getEntity();
            Entity origin = source.getDirectEntity();

            if (attacker instanceof Player p) attackerPlayer = p;
            else if (origin instanceof Player p2) attackerPlayer = p2;

            if (attackerPlayer == null) return true;
            if (!GuardOwnership.isOwnedBy(victim, attackerPlayer.getUUID())) return true;

            return !isRallied(victim);
        });
    }

    private static boolean isRallied(Entity guard) {
        return guard instanceof GuardEntity gv && GuardOrders.isRallied(gv);
    }
}
