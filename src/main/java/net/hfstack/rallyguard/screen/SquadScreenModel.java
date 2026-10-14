package net.hfstack.rallyguard.screen;

import net.hfstack.rallyguard.api.command.GuardCommandScope;
import net.hfstack.rallyguard.network.payload.GuardListS2CPayload;
import net.hfstack.rallyguard.squad.GuardScopeMatcher;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class SquadScreenModel {
    private final GuardListS2CPayload snapshot;
    private final GuardCommandScope scope;

    public SquadScreenModel(GuardListS2CPayload snapshot, GuardCommandScope requestedScope) {
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
        GuardCommandScope requested = Objects.requireNonNull(requestedScope, "requestedScope");
        this.scope = !snapshot.squadsEnabled()
                || requested.squadId().isPresent() && snapshot.squads().stream()
                .noneMatch(squad -> squad.id().equals(requested.squadId().orElseThrow()))
                ? GuardCommandScope.all()
                : requested;
    }

    public GuardCommandScope scope() {
        return scope;
    }

    public List<GuardListS2CPayload.SquadEntry> squads() {
        return snapshot.squadsEnabled() ? snapshot.squads() : List.of();
    }

    public List<GuardListS2CPayload.Entry> allGuards() {
        return snapshot.entries();
    }

    public List<GuardListS2CPayload.Entry> filteredGuards() {
        return snapshot.entries().stream()
                .filter(guard -> GuardScopeMatcher.matches(scope, guard.squadId()))
                .toList();
    }

    public List<GuardListS2CPayload.Entry> officerCandidates(UUID squadId) {
        return snapshot.entries().stream()
                .filter(GuardListS2CPayload.Entry::captain)
                .filter(guard -> guard.squadId().filter(squadId::equals).isPresent())
                .toList();
    }

    public SquadScreenModel withScope(GuardCommandScope nextScope) {
        return new SquadScreenModel(snapshot, nextScope);
    }

    public int pageCount(int perPage) {
        if (perPage < 1) throw new IllegalArgumentException("perPage must be positive");
        return Math.max(1, (filteredGuards().size() + perPage - 1) / perPage);
    }

    public int clampPage(int requestedPage, int perPage) {
        return Math.max(0, Math.min(requestedPage, pageCount(perPage) - 1));
    }
}
