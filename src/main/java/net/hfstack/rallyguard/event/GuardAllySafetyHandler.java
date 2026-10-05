package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hfstack.rallyguard.combat.GuardCombatState;
import net.hfstack.rallyguard.combat.ProtectedAllyPolicy;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public final class GuardAllySafetyHandler {
    private static final Identifier GUARD_ID = Identifier.fromNamespaceAndPath("guardvillagers", "guard");
    private static final int TICK_INTERVAL = 2;

    private GuardAllySafetyHandler() {
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(world -> {
            if (world.getGameTime() % TICK_INTERVAL != 0) return;

            for (Entity entity : world.getEntities(
                    BuiltInRegistries.ENTITY_TYPE.getValue(GUARD_ID),
                    candidate -> candidate instanceof GuardEntity guard
                            && GuardOwnership.hasOwner(guard)
                            && guard.getTarget() != null
                            && guard.getTarget().isAlive()
            )) {
                if (!(entity instanceof GuardEntity guard)) continue;

                UUID ownerId = GuardOwnership.getOwner(guard);
                LivingEntity target = guard.getTarget();
                boolean protectedTarget = ownerId != null
                        && target != null
                        && ProtectedAllyPolicy.isProtectedAlly(ownerId, target);
                if (shouldDisengage(ownerId != null, protectedTarget)) {
                    GuardCombatState.disengage(guard);
                }
            }
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) -> {
            Entity attacker = source.getEntity();
            if (!(attacker instanceof GuardEntity)) {
                attacker = source.getDirectEntity();
            }
            if (!(attacker instanceof GuardEntity guard)) return true;

            UUID ownerId = GuardOwnership.getOwner(guard);
            boolean protectedVictim = ownerId != null
                    && ProtectedAllyPolicy.isProtectedAlly(ownerId, victim);
            return !shouldCancelDamage(ownerId != null, protectedVictim);
        });
    }

    static boolean shouldDisengage(boolean ownedGuard, boolean protectedTarget) {
        return ownedGuard && protectedTarget;
    }

    static boolean shouldCancelDamage(boolean ownedGuard, boolean protectedVictim) {
        return ownedGuard && protectedVictim;
    }
}
