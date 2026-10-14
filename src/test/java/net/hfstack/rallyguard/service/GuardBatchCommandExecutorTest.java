package net.hfstack.rallyguard.service;

import net.hfstack.rallyguard.api.command.GuardBatchCommandResult;
import net.hfstack.rallyguard.api.command.GuardCommandResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuardBatchCommandExecutorTest {
    @Test
    void partialFailuresDoNotStopEligibleCandidatesAndCountsAreExact() {
        GuardBatchCommandResult result = GuardBatchCommandExecutor.executeCandidates(
                List.of("success-a", "denied", "missing", "success-b"),
                value -> switch (value) {
                    case "denied" -> GuardCommandResult.notOwner();
                    case "missing" -> GuardCommandResult.invalidGuard();
                    default -> GuardCommandResult.success("ok");
                }
        );

        assertEquals(2, result.succeeded());
        assertEquals(1, result.rejected());
        assertEquals(1, result.unavailable());
        assertEquals(4, result.total());
    }
}
