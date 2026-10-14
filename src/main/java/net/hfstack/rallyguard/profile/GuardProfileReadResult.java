package net.hfstack.rallyguard.profile;

import java.util.Objects;

public sealed interface GuardProfileReadResult {
    record Absent() implements GuardProfileReadResult {
    }

    record Available(GuardProfile profile, boolean repaired) implements GuardProfileReadResult {
        public Available {
            profile = Objects.requireNonNull(profile, "profile");
        }
    }

    record FutureSchema(int schemaVersion) implements GuardProfileReadResult {
    }
}
