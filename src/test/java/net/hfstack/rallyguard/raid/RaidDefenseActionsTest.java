package net.hfstack.rallyguard.raid;

import net.minecraft.world.entity.raid.Raid;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaidDefenseActionsTest {
    @Test
    void defenseRadiusTracksTheVanillaVillageBuffer() {
        assertEquals(Raid.VILLAGE_RADIUS_BUFFER, RaidDefenseActions.DEFENSE_RADIUS);
        assertTrue(RaidDefenseActions.DEFENSE_RADIUS > 0);
    }
}
