package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public final class RallyFriendlyFireHandler {
    private RallyFriendlyFireHandler() {
    }

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, target, hit) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!GuardOwnership.isGuard(target)) return ActionResult.PASS;
            if (!GuardOwnership.isOwnedBy(target, player.getUuid())) return ActionResult.PASS;
            if (!isFollowing(target)) return ActionResult.PASS;
            return ActionResult.FAIL;
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((LivingEntity victim, net.minecraft.entity.damage.DamageSource source, float amount) -> {
            if (!GuardOwnership.isGuard(victim)) return true;

            PlayerEntity attackerPlayer = null;
            Entity attacker = source.getAttacker();
            Entity origin = source.getSource();

            if (attacker instanceof PlayerEntity p) attackerPlayer = p;
            else if (origin instanceof PlayerEntity p2) attackerPlayer = p2;

            if (attackerPlayer == null) return true;
            if (!GuardOwnership.isOwnedBy(victim, attackerPlayer.getUuid())) return true;

            return !isFollowing(victim);
        });
    }

    private static boolean isFollowing(Entity guard) {
        return guard instanceof GuardEntity gv && gv.isFollowing();
    }
}
