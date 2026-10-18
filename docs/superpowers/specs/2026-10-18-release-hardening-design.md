# Rally of the Guard 1.4.0 — Release Hardening Design

**Date:** 2026-10-18  
**Status:** Approved scope; awaiting specification review before implementation planning

## Intent

Prepare the implemented 1.4.0 feature set for its final manual smoke-test pass
without adding post-1.4.0 gameplay. The work closes concrete security and state
consistency gaps, adds automated regression coverage, and reconciles factual
documentation. Manual in-game results and observation outcomes remain owned by
the human tester and must not be marked approved by this work.

## Scope

This hardening pass includes:

1. bounded and server-validated individual-route updates;
2. deterministic Rally participation and cleanup;
3. reconciliation of Rally effect, participant tags, formation session, and
   scroll presentation across lifecycle transitions;
4. release metadata and obsolete-resource cleanup;
5. automated regression coverage and factual documentation updates.

It excludes Traits, shared multiplayer command, weapon-tag expansion, other
loaders, other Minecraft versions, balancing changes, and new 1.4.0 gameplay.

## Route update hardening

### Wire bounds

`GuardRouteUpdateC2SPayload` will define an absolute protocol maximum of ten
points, matching the maximum value accepted by the current configuration. The
decoder will reject negative or excessive list lengths before allocating a
collection or reading any point. Labels remain bounded by the existing UTF
limit.

The server will accept only the four declared route actions. Invalid actions,
invalid point counts, invalid vertical coordinates, and coordinates outside the
world border will be rejected before any guard state is changed.

### Coordinate authority

The current editor sends its entire draft after every change. Replacing it with
a stateful editing protocol would add avoidable release risk, so the existing
protocol remains.

For each submitted coordinate, the server will accept it only when either:

- it already exists in that guard's stored route, allowing labels, waiting time,
  removal, pause, and start operations to preserve previously captured points;
  or
- it is within normal interaction distance of the player's current position,
  allowing the official `Add current position` and `Use current position`
  actions.

This preserves the official workflow while preventing a modified client from
creating a distant teleport destination. Validation happens for the complete
request before mutation, so mixed valid/invalid drafts cannot partially apply.
Clear remains valid with an empty point list; start still requires at least two
points.

## Rally participation

A guard may join a Rally only when it is owned by the commander, loaded, within
the configured radius, eligible through the public policy chain, and not in an
explicit waiting state or active individual route. Fixed patrol remains
excluded. A territorial Defend Area order may still be temporarily replaced by
Rally as already designed.

The eligibility decision will be expressed in a pure policy so the waiting,
route, patrol, and territorial-defense combinations can be covered without a
running client.

## Rally lifecycle and cleanup

The server-side Rally session is authoritative. A session consists of the
commander effect, a formation anchor, and guards explicitly tagged as Rally
participants. The scroll's `ACTIVE` component is presentation state and must be
reconciled to the server session rather than creating a second authority.

All Rally stops use one cleanup path. It will:

1. remove the commander's Rally effect when the player is available;
2. remove the formation anchor;
3. clear Rally and following state only from loaded guards owned by that
   commander that are explicitly tagged as participants;
4. clear stale `ACTIVE` state from Rally scrolls available in the commander's
   inventory.

Normal toggle-off, effect loss, death/respawn, dimension transition,
disconnect, and login reconciliation invoke or converge through this path.
Cleanup scans loaded entities only and never force-loads a chunk. A guard that
was independently following and never joined the Rally is left unchanged.

The lifecycle coordinator will keep only bounded transient commander UUID
state. Server ticks reconcile sessions whose player disappeared or lost the
effect, preventing abandoned anchors and participant tags.

## Metadata and obsolete paths

- `fabric.mod.json` will declare `CC0-1.0`, matching `LICENSE` and README.
- The obsolete route-placeholder action and `route_soon` translations will be
  removed when no live UI path consumes them.
- `docs/MELHORIAS_TECNICAS.md` will become a current status document: completed
  items will be marked as such, unresolved items will remain explicit, and its
  Java/build language will match Java 25.
- ROADMAP prose will describe Command Post, Global Registry, and Raid Defense as
  implemented for 1.4.0 but awaiting their manual gates.
- CHANGELOG will retain the release as unreleased until the human smoke-test gate
  passes.
- CurseForge copy will be brought up to the implemented 1.4.0 feature set, but
  no publication will be claimed.

## Automated verification

Implementation follows red-green-refactor. Regression coverage will prove:

- excessive and negative route point counts are rejected before allocation;
- unknown actions and invalid/new distant coordinates cannot mutate a route;
- stored coordinates can still be renamed, removed, paused, and restarted;
- waiting, routed, and fixed-patrol guards cannot join a Rally;
- territorial defenders retain the existing Rally transition;
- stopping a Rally affects participants but not independent followers;
- lifecycle reconciliation is idempotent and clears stale session state;
- published metadata has the same license as the repository documentation;
- obsolete route-placeholder resources are absent.

Focused tests run after each change, followed by the complete Gradle build,
server GameTests, client GameTests, `git diff --check`, and artifact inspection.
Automated success will be recorded separately from manual gameplay results.

## Manual handoff

This work will not check any previously open box in the consolidated smoke-test
document solely because an automated test passes. Existing human-entered checks
will be preserved. The final handoff will identify the exact remaining manual
sections, known warnings, final candidate hash, and any observation entries the
human tester should resolve after playing.

## Success criteria

The hardening pass is ready for manual validation when:

- every new regression test passes;
- the complete automated smoke task passes;
- no route packet can allocate or apply an unbounded point list;
- Rally start/stop and lifecycle transitions converge without changing
  unrelated guards;
- metadata and active documentation no longer contradict the implementation;
- manual smoke-test and observation outcomes remain unclaimed and clearly
  assigned to the tester.
