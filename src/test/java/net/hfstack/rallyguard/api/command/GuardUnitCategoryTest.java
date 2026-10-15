package net.hfstack.rallyguard.api.command;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuardUnitCategoryTest {
    @Test
    void knownWireIdsRoundTripAndUnknownIdsAreRejected() {
        assertEquals(Optional.of(GuardUnitCategory.ALL), GuardUnitCategory.fromWireId(0));
        assertEquals(Optional.of(GuardUnitCategory.INFANTRY), GuardUnitCategory.fromWireId(1));
        assertEquals(Optional.of(GuardUnitCategory.RANGED), GuardUnitCategory.fromWireId(2));
        assertEquals(Optional.empty(), GuardUnitCategory.fromWireId(-1));
        assertEquals(Optional.empty(), GuardUnitCategory.fromWireId(3));
    }
}
