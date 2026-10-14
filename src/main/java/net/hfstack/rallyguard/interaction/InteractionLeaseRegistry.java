package net.hfstack.rallyguard.interaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class InteractionLeaseRegistry<R, V> {
    private final Map<R, Set<V>> viewersByResource = new HashMap<>();

    boolean acquire(R resource, V viewer) {
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(viewer, "viewer");
        Set<V> viewers = viewersByResource.computeIfAbsent(resource, ignored -> new HashSet<>());
        boolean first = viewers.isEmpty();
        viewers.add(viewer);
        return first;
    }

    boolean release(R resource, V viewer) {
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(viewer, "viewer");
        Set<V> viewers = viewersByResource.get(resource);
        if (viewers == null || !viewers.remove(viewer)) return false;
        if (!viewers.isEmpty()) return false;
        viewersByResource.remove(resource);
        return true;
    }

    Set<R> releaseAll(V viewer) {
        Objects.requireNonNull(viewer, "viewer");
        Set<R> released = new LinkedHashSet<>();
        var iterator = viewersByResource.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<R, Set<V>> entry = iterator.next();
            if (entry.getValue().remove(viewer) && entry.getValue().isEmpty()) {
                released.add(entry.getKey());
                iterator.remove();
            }
        }
        return Set.copyOf(released);
    }

    Set<R> clear() {
        Set<R> held = Set.copyOf(viewersByResource.keySet());
        viewersByResource.clear();
        return held;
    }
}
