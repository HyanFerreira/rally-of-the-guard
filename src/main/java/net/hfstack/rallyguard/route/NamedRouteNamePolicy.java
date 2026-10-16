package net.hfstack.rallyguard.route;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class NamedRouteNamePolicy {
    private NamedRouteNamePolicy() {
    }

    public static Optional<String> normalize(String value, int maximumCodePoints) {
        if (value == null || maximumCodePoints < 1) return Optional.empty();
        String normalized = value.trim();
        if (normalized.isEmpty() || normalized.codePointCount(0, normalized.length()) > maximumCodePoints) {
            return Optional.empty();
        }
        return Optional.of(normalized);
    }

    public static boolean isDuplicate(NamedRouteRegistry registry, String normalizedName, Optional<UUID> excludedId) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(normalizedName, "normalizedName");
        Optional<UUID> excluded = excludedId == null ? Optional.empty() : excludedId;
        String folded = normalizedName.toLowerCase(Locale.ROOT);
        return registry.routes().stream().anyMatch(route ->
                !excluded.map(route.id()::equals).orElse(false)
                        && route.name().toLowerCase(Locale.ROOT).equals(folded)
        );
    }
}
