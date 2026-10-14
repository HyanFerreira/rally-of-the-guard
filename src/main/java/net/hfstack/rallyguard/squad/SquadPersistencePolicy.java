package net.hfstack.rallyguard.squad;

import java.util.Objects;

public final class SquadPersistencePolicy {
    private SquadPersistencePolicy() {
    }

    public static boolean canWrite(SquadRegistryReadResult result) {
        Objects.requireNonNull(result, "result");
        return !(result instanceof SquadRegistryReadResult.FutureSchema);
    }

    public static boolean canWrite(GuardSquadAssignmentReadResult result) {
        Objects.requireNonNull(result, "result");
        return !(result instanceof GuardSquadAssignmentReadResult.FutureSchema);
    }

    public static boolean shouldCanonicalize(SquadRegistryReadResult result) {
        return result instanceof SquadRegistryReadResult.Available available && available.repaired();
    }

    public static boolean shouldCanonicalize(GuardSquadAssignmentReadResult result) {
        return result instanceof GuardSquadAssignmentReadResult.Available available && available.repaired();
    }
}
