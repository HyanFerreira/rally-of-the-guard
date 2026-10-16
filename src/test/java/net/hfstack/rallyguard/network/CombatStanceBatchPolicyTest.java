package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.api.command.GuardBatchCommandResult;
import net.hfstack.rallyguard.api.command.GuardCommandResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatStanceBatchPolicyTest {
    @Test
    void commandsInUuidOrderAndAggregatesPartialFailure() {
        UUID first = new UUID(0, 1);
        UUID second = new UUID(0, 2);
        UUID third = new UUID(0, 3);
        List<Candidate> candidates = List.of(
                new Candidate(third, GuardCommandResult.invalidGuard()),
                new Candidate(first, GuardCommandResult.success("ok")),
                new Candidate(second, GuardCommandResult.notOwner())
        );
        List<UUID> commanded = new ArrayList<>();

        GuardBatchCommandResult result = CombatStanceBatchPolicy.execute(
                candidates,
                Candidate::id,
                candidate -> {
                    commanded.add(candidate.id());
                    return candidate.result();
                }
        );

        assertEquals(List.of(first, second, third), commanded);
        assertEquals(new GuardBatchCommandResult(1, 1, 1), result);
    }

    private record Candidate(UUID id, GuardCommandResult result) {
    }
}
