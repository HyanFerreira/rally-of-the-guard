package net.hfstack.rallyguard.formation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RallyFormationTest {
    @Test
    void persistedIdsAreStableAndUnknownValuesUseEscort() {
        assertEquals("shield_wall", RallyFormation.SHIELD_WALL.id());
        assertEquals(RallyFormation.WEDGE, RallyFormation.fromId("wedge"));
        assertEquals(RallyFormation.ESCORT, RallyFormation.fromId("future_formation"));
        assertEquals(RallyFormation.ESCORT, RallyFormation.fromId(null));
    }

    @Test
    void cyclesThroughEveryFormationAndWrapsToEscort() {
        RallyFormation current = RallyFormation.ESCORT;
        for (int i = 1; i < RallyFormation.values().length; i++) {
            current = current.next();
            assertEquals(RallyFormation.values()[i], current);
        }
        assertEquals(RallyFormation.ESCORT, current.next());
    }
}
