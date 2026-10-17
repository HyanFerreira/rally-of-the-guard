package net.hfstack.rallyguard.raid;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaidOpportunitySessionsTest {
    private static final RaidOpportunityKey RAID = new RaidOpportunityKey(
            4, "minecraft:overworld", new BlockPos(20, 64, -10)
    );

    @Test
    void sessionIsSingleUseAndBoundToExpectedRaid() {
        UUID player = UUID.randomUUID();
        RaidOpportunitySessions sessions = new RaidOpportunitySessions();
        sessions.open(player, RAID, 100, 40);

        assertTrue(sessions.alreadySeen(player, RAID));
        assertTrue(sessions.consume(player, RAID, 120).isPresent());
        assertFalse(sessions.consume(player, RAID, 120).isPresent());
    }

    @Test
    void expiredAndMismatchedResponsesAreRejected() {
        UUID player = UUID.randomUUID();
        RaidOpportunitySessions sessions = new RaidOpportunitySessions();
        sessions.open(player, RAID, 100, 20);

        RaidOpportunityKey other = new RaidOpportunityKey(5, "minecraft:overworld", BlockPos.ZERO);
        assertFalse(sessions.consume(player, other, 110).isPresent());
        assertTrue(sessions.consume(player, RAID, 110).isPresent());

        sessions.open(player, RAID, 100, 20);
        assertFalse(sessions.consume(player, RAID, 121).isPresent());
    }

    @Test
    void finishedRaidMayBeRemovedFromDeduplicationState() {
        UUID player = UUID.randomUUID();
        RaidOpportunitySessions sessions = new RaidOpportunitySessions();
        sessions.open(player, RAID, 0, 20);
        sessions.clearFinished(RAID);

        assertFalse(sessions.alreadySeen(player, RAID));
    }

    @Test
    void disconnectClearsSessionAndDeduplication() {
        UUID player = UUID.randomUUID();
        RaidOpportunitySessions sessions = new RaidOpportunitySessions();
        sessions.open(player, RAID, 0, 20);
        sessions.clearPlayer(player);

        assertFalse(sessions.alreadySeen(player, RAID));
        assertFalse(sessions.consume(player, RAID, 1).isPresent());
    }

    @Test
    void automaticResponseMayDeduplicateWithoutOpeningAClientSession() {
        UUID player = UUID.randomUUID();
        RaidOpportunitySessions sessions = new RaidOpportunitySessions();
        sessions.markSeen(player, RAID);

        assertTrue(sessions.alreadySeen(player, RAID));
        assertFalse(sessions.consume(player, RAID, 1).isPresent());
    }
}
