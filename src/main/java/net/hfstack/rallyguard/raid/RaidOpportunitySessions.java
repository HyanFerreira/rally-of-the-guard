package net.hfstack.rallyguard.raid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class RaidOpportunitySessions {
    private final Map<UUID, Session> sessions = new HashMap<>();
    private final Map<UUID, Set<RaidOpportunityKey>> seen = new HashMap<>();

    public boolean alreadySeen(UUID playerId, RaidOpportunityKey key) {
        return seen.getOrDefault(playerId, Set.of()).contains(key);
    }

    public void open(UUID playerId, RaidOpportunityKey key, long currentTick, long lifetimeTicks) {
        if (lifetimeTicks <= 0) throw new IllegalArgumentException("lifetimeTicks must be positive");
        seen.computeIfAbsent(playerId, ignored -> new HashSet<>()).add(key);
        sessions.put(playerId, new Session(key, saturatedAdd(currentTick, lifetimeTicks)));
    }

    public Optional<RaidOpportunityKey> consume(UUID playerId, RaidOpportunityKey expected, long currentTick) {
        Session session = sessions.get(playerId);
        if (session == null) {
            return Optional.empty();
        }
        if (session.expiresAtTick() < currentTick) {
            sessions.remove(playerId);
            return Optional.empty();
        }
        if (!session.key().equals(expected)) return Optional.empty();
        sessions.remove(playerId);
        return Optional.of(session.key());
    }

    public void expire(long currentTick) {
        sessions.entrySet().removeIf(entry -> entry.getValue().expiresAtTick() < currentTick);
    }

    public void clearFinished(RaidOpportunityKey key) {
        sessions.entrySet().removeIf(entry -> entry.getValue().key().equals(key));
        seen.values().forEach(keys -> keys.remove(key));
        seen.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public void clearPlayer(UUID playerId) {
        sessions.remove(playerId);
        seen.remove(playerId);
    }

    public void clear() {
        sessions.clear();
        seen.clear();
    }

    private static long saturatedAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    private record Session(RaidOpportunityKey key, long expiresAtTick) {
    }
}
