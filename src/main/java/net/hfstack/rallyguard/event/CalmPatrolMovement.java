package net.hfstack.rallyguard.event;

import dev.sterner.guardvillagers.common.entity.GuardEntity;
import net.hfstack.rallyguard.order.PatrolMovementPolicy;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class CalmPatrolMovement {
    private static final double AT_POSITION_SQUARED = 0.7 * 0.7;
    private static final int MIN_MOVE_DELAY = 20 * 8;
    private static final int MOVE_DELAY_VARIANCE = 20 * 7;
    private static final int MIN_AWAY_WAIT = 20 * 3;
    private static final int AWAY_WAIT_VARIANCE = 20 * 3;
    private static final int MIN_LOOK_DELAY = 20 * 2;
    private static final int LOOK_DELAY_VARIANCE = 20 * 3;
    private static final Map<UUID, IdleState> STATES = new HashMap<>();

    private CalmPatrolMovement() {
    }

    static void holdAt(GuardEntity guard, BlockPos anchor, boolean allowIdleStep) {
        long now = guard.level().getGameTime();
        IdleState state = STATES.compute(guard.getUUID(), (id, previous) ->
                previous == null || !previous.anchor.equals(anchor)
                        ? IdleState.atCenter(anchor.immutable(), now + moveDelay(guard), now + lookDelay(guard))
                        : previous
        );

        if (guard.getTarget() != null) {
            clear(guard);
            return;
        }

        lookAroundIfDue(guard, state, now);
        switch (state.phase) {
            case CENTER -> {
                guard.getNavigation().stop();
                if (allowIdleStep && now >= state.nextMoveAt) startIdleStep(guard, state, now);
            }
            case MOVING_OUT -> {
                if (at(guard, state.idleTarget)) {
                    guard.getNavigation().stop();
                    state.phase = Phase.AWAY;
                    state.phaseUntil = now + MIN_AWAY_WAIT + guard.getRandom().nextInt(AWAY_WAIT_VARIANCE + 1);
                } else {
                    walkTo(guard, state.idleTarget, PatrolMovementPolicy.IDLE_STEP_SPEED);
                }
            }
            case AWAY -> {
                guard.getNavigation().stop();
                if (now >= state.phaseUntil) {
                    state.phase = Phase.RETURNING;
                    walkTo(guard, state.anchor, PatrolMovementPolicy.IDLE_STEP_SPEED);
                }
            }
            case RETURNING -> {
                if (at(guard, state.anchor)) {
                    guard.getNavigation().stop();
                    state.resetAtCenter(now + moveDelay(guard));
                } else {
                    walkTo(guard, state.anchor, PatrolMovementPolicy.IDLE_STEP_SPEED);
                }
            }
        }
    }

    static void clear(GuardEntity guard) {
        STATES.remove(guard.getUUID());
    }

    private static void startIdleStep(GuardEntity guard, IdleState state, long now) {
        int direction = guard.getRandom().nextInt(4);
        int dx = direction == 0 ? 1 : direction == 1 ? -1 : 0;
        int dz = direction == 2 ? 1 : direction == 3 ? -1 : 0;
        BlockPos target = state.anchor.offset(dx, 0, dz);
        if (guard.getNavigation().moveTo(
                target.getX() + 0.5, target.getY(), target.getZ() + 0.5,
                PatrolMovementPolicy.IDLE_STEP_SPEED
        )) {
            state.phase = Phase.MOVING_OUT;
            state.idleTarget = target;
            return;
        }
        state.nextMoveAt = now + moveDelay(guard);
    }

    private static void lookAroundIfDue(GuardEntity guard, IdleState state, long now) {
        if (now < state.nextLookAt) return;
        double lookX = guard.getX() + guard.getRandom().nextDouble() * 8.0 - 4.0;
        double lookY = guard.getEyeY() + guard.getRandom().nextDouble() * 2.0 - 1.0;
        double lookZ = guard.getZ() + guard.getRandom().nextDouble() * 8.0 - 4.0;
        guard.getLookControl().setLookAt(lookX, lookY, lookZ, 30.0F, 20.0F);
        state.nextLookAt = now + lookDelay(guard);
    }

    private static void walkTo(GuardEntity guard, BlockPos target, double speed) {
        guard.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, speed);
    }

    private static boolean at(GuardEntity guard, BlockPos target) {
        return guard.distanceToSqr(target.getX() + 0.5, target.getY(), target.getZ() + 0.5)
                <= AT_POSITION_SQUARED;
    }

    private static int moveDelay(GuardEntity guard) {
        return MIN_MOVE_DELAY + guard.getRandom().nextInt(MOVE_DELAY_VARIANCE + 1);
    }

    private static int lookDelay(GuardEntity guard) {
        return MIN_LOOK_DELAY + guard.getRandom().nextInt(LOOK_DELAY_VARIANCE + 1);
    }

    private enum Phase {
        CENTER,
        MOVING_OUT,
        AWAY,
        RETURNING
    }

    private static final class IdleState {
        private final BlockPos anchor;
        private BlockPos idleTarget;
        private Phase phase;
        private long phaseUntil;
        private long nextMoveAt;
        private long nextLookAt;

        private IdleState(BlockPos anchor, long nextMoveAt, long nextLookAt) {
            this.anchor = anchor;
            this.idleTarget = anchor;
            this.phase = Phase.CENTER;
            this.nextMoveAt = nextMoveAt;
            this.nextLookAt = nextLookAt;
        }

        private static IdleState atCenter(BlockPos anchor, long nextMoveAt, long nextLookAt) {
            return new IdleState(anchor, nextMoveAt, nextLookAt);
        }

        private void resetAtCenter(long nextMoveAt) {
            this.idleTarget = anchor;
            this.phase = Phase.CENTER;
            this.phaseUntil = 0;
            this.nextMoveAt = nextMoveAt;
        }
    }
}
