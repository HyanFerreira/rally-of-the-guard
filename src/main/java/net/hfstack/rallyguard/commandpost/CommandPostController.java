package net.hfstack.rallyguard.commandpost;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.contract.GuardOwnership;
import net.hfstack.rallyguard.formation.FormationLayouts;
import net.hfstack.rallyguard.formation.FormationOffset;
import net.hfstack.rallyguard.order.defend.DefendAreaState;
import net.hfstack.rallyguard.order.defend.DefendAreas;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;

public final class CommandPostController {
    private static final int TICK_INTERVAL = 20;
    private static final double COLUMN_SPACING = 2.0;
    private static final double ROW_SPACING = 3.0;

    private CommandPostController() {
    }

    public static void tick(Level level, BlockPos pos, BlockState ignored, CommandPostBlockEntity post) {
        if (!(level instanceof ServerLevel serverLevel)
                || level.getGameTime() % TICK_INTERVAL != 0
                || post.hasFutureSchema()) return;
        reconcile(serverLevel, pos, post.state());
    }

    public static void reconcile(ServerLevel level, BlockPos center, CommandPostState state) {
        UUID ownerId = state.ownerId().orElse(null);
        if (ownerId == null) return;
        List<UUID> assignments = state.assignedGuardIds();
        for (int index = 0; index < assignments.size(); index++) {
            Entity entity = level.getEntity(assignments.get(index));
            if (!(entity instanceof GuardEntity guard) || !GuardOwnership.isOwnedBy(guard, ownerId)) continue;
            if (!state.active()) {
                DefendAreas.clearIfFromCommandPost(guard, state.postId());
                continue;
            }
            DefendAreaState current = DefendAreas.active(guard).orElse(null);
            if (current != null && current.commandPostId().filter(state.postId()::equals).isEmpty()) continue;
            DefendAreas.setFromCommandPost(
                    guard,
                    center,
                    formationPost(center, state, index),
                    state.radius(),
                    state.postId()
            );
        }
    }

    public static void release(ServerLevel level, CommandPostState state) {
        for (UUID guardId : state.assignedGuardIds()) {
            Entity entity = level.getEntity(guardId);
            if (entity != null) DefendAreas.clearIfFromCommandPost(entity, state.postId());
        }
    }

    public static BlockPos formationPost(BlockPos center, CommandPostState state, int index) {
        FormationOffset offset = FormationLayouts.slot(
                state.formation(), index, state.assignedGuardIds().size(), COLUMN_SPACING, ROW_SPACING
        );
        return center.offset((int)Math.round(offset.right()), 0, (int)Math.round(offset.forward()));
    }
}
