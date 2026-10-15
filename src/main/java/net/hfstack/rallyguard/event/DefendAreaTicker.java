package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hfstack.rallyguard.combat.GuardCombatState;
import net.hfstack.rallyguard.config.RallyConfig;
import net.hfstack.rallyguard.order.defend.DefendAreaBoundaryPolicy;
import net.hfstack.rallyguard.order.defend.DefendAreaState;
import net.hfstack.rallyguard.order.defend.DefendAreas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public final class DefendAreaTicker {
    private static final Identifier GUARD_ID = Identifier.fromNamespaceAndPath("guardvillagers", "guard");
    private static final int TICK_INTERVAL = 10;

    private DefendAreaTicker() {
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(world -> {
            if (world.getGameTime() % TICK_INTERVAL == 0) tickWorld(world);
        });
    }

    private static void tickWorld(ServerLevel world) {
        List<? extends Entity> guards = world.getEntities(
                BuiltInRegistries.ENTITY_TYPE.getValue(GUARD_ID),
                entity -> entity instanceof GuardEntity && DefendAreas.active(entity).isPresent()
        );
        for (Entity entity : guards) {
            if (entity instanceof GuardEntity guard) tickGuard(guard);
        }
    }

    private static void tickGuard(GuardEntity guard) {
        DefendAreaState order = DefendAreas.active(guard).orElse(null);
        if (order == null) return;

        BlockPos post = order.post();
        guard.setFollowing(false);
        guard.setPatrolling(true);
        if (!post.equals(guard.getPatrolPos())) guard.setPatrolPos(post);

        LivingEntity target = guard.getTarget();
        boolean targetAllowed = target != null && target.isAlive()
                && DefendAreaBoundaryPolicy.withinRadius(
                order.center(), target.getX(), target.getY(), target.getZ(), order.radius()
        );
        boolean beyondReturn = DefendAreaBoundaryPolicy.beyondReturnBoundary(
                order.center(), guard.getX(), guard.getY(), guard.getZ(), order.radius()
        );

        switch (DefendAreaBoundaryPolicy.decide(target != null, targetAllowed, beyondReturn)) {
            case PATROL, KEEP_COMBAT -> {
                // Native Guard Villagers AI retains control inside the defended territory.
            }
            case DISENGAGE -> GuardCombatState.disengage(guard);
            case RETURN_TO_POST -> returnToPost(guard, post);
            case DISENGAGE_AND_RETURN -> {
                GuardCombatState.disengage(guard);
                returnToPost(guard, post);
            }
        }
    }

    private static void returnToPost(GuardEntity guard, BlockPos post) {
        guard.getNavigation().moveTo(
                post.getX() + 0.5,
                post.getY(),
                post.getZ() + 0.5,
                RallyConfig.routeMoveSpeed()
        );
    }
}
