package net.hfstack.rallyguard.profile;

import net.minecraft.world.entity.EntitySpawnReason;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardProfileInitializerTest {
    @Test
    void classifiesOnlyTrustedWorldGenerationAsNatural() {
        assertEquals(GuardProfileOrigin.NATURAL, GuardProfileInitializer.origin(EntitySpawnReason.NATURAL));
        assertEquals(GuardProfileOrigin.NATURAL, GuardProfileInitializer.origin(EntitySpawnReason.STRUCTURE));
        assertEquals(GuardProfileOrigin.COMMAND, GuardProfileInitializer.origin(EntitySpawnReason.COMMAND));
        assertEquals(GuardProfileOrigin.OTHER, GuardProfileInitializer.origin(EntitySpawnReason.SPAWN_ITEM_USE));
        assertTrue(GuardProfileInitializer.isNaturalLevelEligible(EntitySpawnReason.CHUNK_GENERATION));
        assertFalse(GuardProfileInitializer.isNaturalLevelEligible(EntitySpawnReason.SPAWNER));
        assertFalse(GuardProfileInitializer.isNaturalLevelEligible(EntitySpawnReason.COMMAND));
    }
}
