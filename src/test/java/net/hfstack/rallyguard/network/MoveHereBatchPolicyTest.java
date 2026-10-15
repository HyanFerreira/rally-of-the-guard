package net.hfstack.rallyguard.network;

import net.hfstack.rallyguard.api.command.GuardBatchCommandResult;
import net.hfstack.rallyguard.api.command.GuardCommandResult;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveHereBatchPolicyTest {
    private static final UUID FIRST = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SECOND = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID THIRD = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID FOURTH = UUID.fromString("00000000-0000-0000-0000-000000000004");

    @Test
    void categoryFilterUuidOrderingAndPartialSlotFailureAreDeterministic() {
        List<Candidate> candidates = List.of(
                new Candidate(THIRD, true, GuardCommandResult.notOwner()),
                new Candidate(FIRST, true, GuardCommandResult.success("ok")),
                new Candidate(FOURTH, false, GuardCommandResult.success("excluded")),
                new Candidate(SECOND, true, GuardCommandResult.success("unsafe"))
        );
        List<UUID> commanded = new ArrayList<>();

        GuardBatchCommandResult result = MoveHereBatchPolicy.execute(
                candidates,
                Candidate::id,
                Candidate::categoryMatches,
                (candidate, slotIndex) -> slotIndex == 1
                        ? Optional.empty()
                        : Optional.of(new BlockPos(slotIndex, 64, 0)),
                (candidate, destination) -> {
                    commanded.add(candidate.id());
                    return candidate.result();
                }
        );

        assertEquals(List.of(FIRST, THIRD), commanded);
        assertEquals(new GuardBatchCommandResult(1, 1, 1), result);
    }

    private record Candidate(UUID id, boolean categoryMatches, GuardCommandResult result) {
    }
}
