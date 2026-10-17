package net.hfstack.rallyguard.raid;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.api.RallyGuardApi;
import net.hfstack.rallyguard.api.command.GuardBatchCommandResult;
import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.api.command.GuardCommandResult;
import net.hfstack.rallyguard.network.MoveHereBatchPolicy;
import net.hfstack.rallyguard.order.GuardFollowAssistPolicy;
import net.hfstack.rallyguard.order.defend.DefendAreaBoundaryPolicy;
import net.hfstack.rallyguard.order.defend.DefendAreas;
import net.hfstack.rallyguard.order.move.MoveOrderSlotResolver;
import net.hfstack.rallyguard.service.GuardBatchCommandExecutor;
import net.hfstack.rallyguard.service.GuardCommandTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.raid.Raid;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

public final class RaidDefenseActions {
    public static final int DEFENSE_RADIUS = Raid.VILLAGE_RADIUS_BUFFER;
    static final int POST_CANDIDATE_LIMIT = 256;

    private RaidDefenseActions() {
    }

    public static GuardBatchCommandResult execute(
            RaidResponseAction action,
            ServerPlayer player,
            Raid raid,
            RaidOpportunityKey key
    ) {
        List<GuardEntity> guards = GuardCommandTargets.loadedScopedGuards(player, GuardCommandScope.all());
        return switch (action) {
            case RALLY_GUARDS -> rally(player, guards);
            case DEFEND_VILLAGE -> defend(player, player.level(), raid.getCenter(), key.raidId(), guards);
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
            int raidId,
            List<GuardEntity> guards
    ) {
        Set<BlockPos> reserved = new HashSet<>();
        return MoveHereBatchPolicy.execute(
                guards,
                GuardEntity::getUUID,
                guard -> true,
                (guard, slotIndex) -> RaidDefensePostSearch.firstAvailable(
                        POST_CANDIDATE_LIMIT,
                        candidateIndex -> MoveOrderSlotResolver.resolve(level, center, candidateIndex, guard),
                        post -> DefendAreaBoundaryPolicy.withinRadius(
                                center, post.getX() + 0.5, post.getY() + 0.5, post.getZ() + 0.5, DEFENSE_RADIUS
                        ),
                        reserved
                ),
                (guard, post) -> beginRaidDefense(player, guard, center, post, raidId)
        );
    }

    private static GuardCommandResult beginRaidDefense(
            ServerPlayer player,
            GuardEntity guard,
            BlockPos center,
            BlockPos post,
            int raidId
    ) {
        GuardCommandResult result = RallyGuardApi.guardCommands().defendArea(
                player, guard, center, post, DEFENSE_RADIUS
        );
        if (!result.succeeded()) return result;

        DefendAreas.setFromRaid(guard, center, post, DEFENSE_RADIUS, raidId);
        guard.getNavigation().moveTo(
                post.getX() + 0.5,
                post.getY(),
                post.getZ() + 0.5,
                GuardFollowAssistPolicy.CATCH_UP_SPEED
        );
        return result;
    }
}
