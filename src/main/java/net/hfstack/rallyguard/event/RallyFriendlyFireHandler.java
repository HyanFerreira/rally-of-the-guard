package net.hfstack.rallyguard.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;

/**
 * Imuniza guardas do PRÓPRIO jogador enquanto estão no Rali.
 * Cobre:
 * - clique direto (AttackEntityCallback)
 * - QUALQUER dano (ALLOW_DAMAGE): sweep, crítico, projéteis, tridente, etc.
 * <p>
 * Critério: alvo é guarda do jogador E (in_rally==true OU Following==true).
 * (Following é fallback caso algum port não persista o flag in_rally.)
 */
public final class RallyFriendlyFireHandler {
    private RallyFriendlyFireHandler() {
    }

    public static void register() {
        // 1) Clique direto
        AttackEntityCallback.EVENT.register((player, world, hand, target, hit) -> {
            if (world.isClient) return ActionResult.PASS;
            if (!GuardOwnership.isGuard(target)) return ActionResult.PASS;
            if (!GuardOwnership.isOwnedBy(target, player.getUuid())) return ActionResult.PASS;
            if (!isInRallyOrFollowing(target)) return ActionResult.PASS;
            return ActionResult.FAIL;
        });

        // 2) Qualquer dano (inclui sweep/projéteis)
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((LivingEntity victim, net.minecraft.entity.damage.DamageSource source, float amount) -> {
            if (!GuardOwnership.isGuard(victim)) return true;

            // Detecta jogador tanto como "attacker" quanto como "source" (projéteis/tridente)
            PlayerEntity attackerPlayer = null;
            Entity attacker = source.getAttacker(); // normalmente o jogador no corpo-a-corpo
            Entity origin = source.getSource();   // projétil/tridente/etc.

            if (attacker instanceof PlayerEntity p) attackerPlayer = p;
            else if (origin instanceof PlayerEntity p2) attackerPlayer = p2;

            if (attackerPlayer == null) return true;

            if (!GuardOwnership.isOwnedBy(victim, attackerPlayer.getUuid())) return true;
            if (!isInRallyOrFollowing(victim)) return true;

            // Bloqueia o dano em guardas do dono que estão no rali
            return false;
        });
    }

    private static boolean isInRallyOrFollowing(Entity guard) {
        NbtCompound n = new NbtCompound();
        guard.writeNbt(n);
        // flag principal do mod
        if (n.getBoolean("rallyguard:in_rally")) return true;
        // fallback robusto: no rali você seta Following=true; fora, você seta false
        return n.getBoolean("Following");
    }
}
