package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.api.command.GuardUnitCategory;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveHereRequestPolicyTest {
    @Test
    void validRequestResolvesStrictCategoryAndFace() {
        MoveHereRequestPolicy.Result result = MoveHereRequestPolicy.validate(
                GuardUnitCategory.RANGED.wireId(), Direction.NORTH.get3DDataValue(),
                true, true, true, true, true
        );

        MoveHereRequestPolicy.Valid valid = (MoveHereRequestPolicy.Valid) result;
        assertEquals(GuardUnitCategory.RANGED, valid.category());
        assertEquals(Direction.NORTH, valid.face());
    }

    @Test
    void everyInvalidGlobalInputIsRejectedBeforeMutation() {
        assertRejected(99, 0, true, true, true, true, true);
        assertRejected(0, 99, true, true, true, true, true);
        assertRejected(0, 0, false, true, true, true, true);
        assertRejected(0, 0, true, false, true, true, true);
        assertRejected(0, 0, true, true, false, true, true);
        assertRejected(0, 0, true, true, true, false, true);
        assertRejected(0, 0, true, true, true, true, false);
    }

    private static void assertRejected(
            int category,
            int face,
            boolean scope,
            boolean range,
            boolean loaded,
            boolean border,
            boolean safe
    ) {
        assertTrue(MoveHereRequestPolicy.validate(category, face, scope, range, loaded, border, safe)
                instanceof MoveHereRequestPolicy.Rejected);
    }
}
