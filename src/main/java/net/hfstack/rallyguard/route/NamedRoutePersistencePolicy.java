package net.hfstack.rallyguard.route;

import java.util.Objects;

public final class NamedRoutePersistencePolicy {
    private NamedRoutePersistencePolicy() {
    }

    public static boolean canWrite(NamedRouteRegistryReadResult result) {
        Objects.requireNonNull(result, "result");
        return !(result instanceof NamedRouteRegistryReadResult.FutureSchema);
    }

    public static boolean shouldCanonicalize(NamedRouteRegistryReadResult result) {
        return result instanceof NamedRouteRegistryReadResult.Available available && available.repaired();
    }
}
