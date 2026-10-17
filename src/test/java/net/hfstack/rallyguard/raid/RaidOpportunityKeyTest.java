package net.hfstack.rallyguard.raid;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaidOpportunityKeyTest {
    @Test
    void raidIdentitySurvivesVanillaCenterAdjustment() {
        RaidOpportunityKey opened = new RaidOpportunityKey(
                7, "minecraft:overworld", new BlockPos(10, 64, 10)
        );
        RaidOpportunityKey adjusted = new RaidOpportunityKey(
                7, "minecraft:overworld", new BlockPos(18, 64, 4)
        );

        assertTrue(opened.identifiesSameRaid(adjusted));
        assertFalse(opened.identifiesSameRaid(new RaidOpportunityKey(
                8, "minecraft:overworld", adjusted.center()
        )));
        assertFalse(opened.identifiesSameRaid(new RaidOpportunityKey(
                7, "minecraft:the_nether", adjusted.center()
        )));
    }
}
