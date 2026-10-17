package net.hfstack.rallyguard.raid;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.command.GuardBatchCommandResult;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.network.MoveHereBatchPolicy;
import net.hfstack.rallyguard.order.defend.DefendAreaBoundaryPolicy;
import net.hfstack.rallyguard.order.move.MoveOrderSlotResolver;
import net.hfstack.rallyguard.service.GuardBatchCommandExecutor;
import net.hfstack.rallyguard.service.GuardCommandTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.raid.Raid;

import java.util.List;

public final class RaidDefenseActions {
    public static final int DEFENSE_RADIUS = Raid.VILLAGE_RADIUS_BUFFER;

    private RaidDefenseActions() {
    }

    public static GuardBatchCommandResult execute(
            RaidResponseAction action,
            ServerPlayer player,
            Raid raid
    ) {
        List<GuardEntity> guards = GuardCommandTargets.loadedScopedGuards(player, GuardCommandScope.all());
        return switch (action) {
            case RALLY_GUARDS -> rally(player, guards);
            case DEFEND_VILLAGE -> defend(player, player.level(), raid.getCenter(), guards);
            case IGNORE -> new GuardBatchCommandResult(0, 0, 0);
        };
    }

    private static GuardBatchCommandResult rally(ServerPlayer player, List<GuardEntity> guards) {
        return GuardBatchCommandExecutor.executeCandidates(
                guards,
                guard -> RallyGuardApi.guardCommands().summon(player, guard)
        );
    }

    private static GuardBatchCommandResult defend(
            ServerPlayer player,
            ServerLevel level,
            BlockPos center,
            List<GuardEntity> guards
    ) {
        return MoveHereBatchPolicy.execute(
                guards,
                GuardEntity::getUUID,
                guard -> true,
                (guard, slotIndex) -> MoveOrderSlotResolver.resolve(level, center, slotIndex)
                        .filter(post -> DefendAreaBoundaryPolicy.withinRadius(
                                center,
                                post.getX() + 0.5,
                                post.getY() + 0.5,
                                post.getZ() + 0.5,
                                DEFENSE_RADIUS
                        )),
                (guard, post) -> RallyGuardApi.guardCommands().defendArea(
                        player, guard, center, post, DEFENSE_RADIUS
                )
        );
    }
}
