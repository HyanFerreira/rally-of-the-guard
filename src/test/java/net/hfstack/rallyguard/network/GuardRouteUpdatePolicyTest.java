package net.hfstack.rallyguard.network;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardRouteUpdatePolicyTest {
    private static final BlockPos PLAYER = new BlockPos(0, 64, 0);
    private static final int MIN_Y = -64;
    private static final int MAX_Y_EXCLUSIVE = 320;
    private static final double CAPTURE_RADIUS_SQUARED = 64.0;

    @Test
    void acceptsValidSaveStartPauseAndEmptyClear() {
        List<BlockPos> points = List.of(new BlockPos(1, 64, 1), new BlockPos(2, 64, 2));

        assertAccepted(validate(NetworkConstants.ROUTE_SAVE, points, List.of()));
        assertAccepted(validate(NetworkConstants.ROUTE_START, points, List.of()));
        assertAccepted(validate(NetworkConstants.ROUTE_PAUSE, points, List.of()));
        assertAccepted(validate(NetworkConstants.ROUTE_CLEAR, List.of(), List.of()));
    }

    @Test
    void rejectsUnknownActionInvalidStartAndNonEmptyClear() {
        assertRejected(validate(99, List.of(), List.of()), GuardRouteUpdatePolicy.INVALID_REQUEST_KEY);
        assertRejected(validate(NetworkConstants.ROUTE_START, List.of(PLAYER), List.of()),
                GuardRouteUpdatePolicy.INVALID_REQUEST_KEY);
        assertRejected(validate(NetworkConstants.ROUTE_CLEAR, List.of(PLAYER), List.of()),
                GuardRouteUpdatePolicy.INVALID_REQUEST_KEY);
    }

    @Test
    void rejectsMoreThanConfiguredMaximum() {
        List<BlockPos> points = List.of(
                PLAYER, PLAYER, PLAYER, PLAYER, PLAYER, PLAYER,
                PLAYER, PLAYER, PLAYER, PLAYER, PLAYER
        );

        assertRejected(validate(NetworkConstants.ROUTE_SAVE, points, List.of()),
                GuardRouteUpdatePolicy.INVALID_REQUEST_KEY);
    }

    @Test
    void rejectsPointsOutsideBuildHeightOrWorldBorder() {
        assertRejected(validate(NetworkConstants.ROUTE_SAVE, List.of(new BlockPos(0, MIN_Y - 1, 0)), List.of()),
                GuardRouteUpdatePolicy.INVALID_POINT_KEY);
        assertRejected(validate(NetworkConstants.ROUTE_SAVE, List.of(new BlockPos(0, MAX_Y_EXCLUSIVE, 0)), List.of()),
                GuardRouteUpdatePolicy.INVALID_POINT_KEY);

        GuardRouteUpdatePolicy.Validation outsideBorder = GuardRouteUpdatePolicy.validate(
                NetworkConstants.ROUTE_SAVE,
                List.of(new BlockPos(1, 64, 1)),
                List.of(),
                PLAYER,
                MIN_Y,
                MAX_Y_EXCLUSIVE,
                ignored -> false,
                CAPTURE_RADIUS_SQUARED,
                10
        );
        assertRejected(outsideBorder, GuardRouteUpdatePolicy.INVALID_POINT_KEY);
    }

    @Test
    void preservesStoredDistantPointsAndAcceptsNewNearbyPoints() {
        BlockPos storedDistant = new BlockPos(1000, 64, 1000);

        assertAccepted(validate(NetworkConstants.ROUTE_SAVE, List.of(storedDistant), List.of(storedDistant)));
        assertAccepted(validate(NetworkConstants.ROUTE_SAVE, List.of(new BlockPos(8, 64, 0)), List.of()));
    }

    @Test
    void rejectsNewDistantPointAndMixedDraftAtomically() {
        BlockPos nearby = new BlockPos(1, 64, 1);
        BlockPos distant = new BlockPos(9, 64, 0);

        assertRejected(validate(NetworkConstants.ROUTE_SAVE, List.of(distant), List.of()),
                GuardRouteUpdatePolicy.INVALID_POINT_KEY);
        assertRejected(validate(NetworkConstants.ROUTE_SAVE, List.of(nearby, distant), List.of()),
                GuardRouteUpdatePolicy.INVALID_POINT_KEY);
    }

    private static GuardRouteUpdatePolicy.Validation validate(int action, List<BlockPos> submitted,
                                                               List<BlockPos> stored) {
        return GuardRouteUpdatePolicy.validate(
                action,
                submitted,
                stored,
                PLAYER,
                MIN_Y,
                MAX_Y_EXCLUSIVE,
                ignored -> true,
                CAPTURE_RADIUS_SQUARED,
                10
        );
    }

    private static void assertAccepted(GuardRouteUpdatePolicy.Validation validation) {
        assertTrue(validation.accepted());
        assertEquals("", validation.translationKey());
    }

    private static void assertRejected(GuardRouteUpdatePolicy.Validation validation, String key) {
        assertFalse(validation.accepted());
        assertEquals(key, validation.translationKey());
    }
}
