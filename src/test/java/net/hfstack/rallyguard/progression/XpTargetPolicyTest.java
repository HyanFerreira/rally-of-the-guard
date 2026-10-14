package net.hfstack.rallyguard.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XpTargetPolicyTest {
    @Test
    void highestBudgetCategoryWinsRegardlessOfOtherMemberships() {
        assertEquals(XpCategory.BOSS,
                XpTargetPolicy.highestCategory(true, true, true, true, true, true));
        assertEquals(XpCategory.RAID_ELITE,
                XpTargetPolicy.highestCategory(true, true, true, true, true, false));
        assertEquals(XpCategory.ELITE,
                XpTargetPolicy.highestCategory(true, true, true, true, false, false));
        assertEquals(XpCategory.RAID,
                XpTargetPolicy.highestCategory(true, true, true, false, false, false));
        assertEquals(XpCategory.DANGEROUS,
                XpTargetPolicy.highestCategory(true, true, false, false, false, false));
        assertEquals(XpCategory.COMMON,
                XpTargetPolicy.highestCategory(false, false, false, false, false, false));
    }
}
