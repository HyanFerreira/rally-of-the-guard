# Squads & Officers Design

**Date:** 2026-10-14  
**Status:** Approved for implementation planning

## Goal

Add player-owned, persistent Squads to the Commander's Ledger so players can
organize hired guards, filter and command loaded members as a group, and assign
one existing natural Captain in each Squad as its Officer.

This milestone must make Captains operational without implementing Captain
promotion, leadership bonuses, additional formations, unloaded-guard commands,
or the future Global Guard Registry.

## Scope

The first Squads & Officers release includes:

- custom Squad creation, rename, and deletion;
- persistent Squad definitions owned by a player;
- persistent assignment of a hired guard to one Squad at most;
- an explicit `Unassigned` state for guards with no valid Squad;
- one optional Officer per Squad;
- Officer selection only from Captains already assigned to that Squad;
- Ledger filtering by All Guards, Unassigned, or one Squad;
- existing individual guard actions in every filtered view;
- collective Summon, Follow, Wait, and Patrol commands for the selected scope;
- attack orders constrained to the selected scope;
- server-authoritative snapshots after every accepted mutation;
- English and Brazilian Portuguese text.

The release excludes:

- manual Captain promotion or demotion;
- leadership bonuses;
- more than one Officer per Squad;
- persistent global membership lists;
- commands targeting unloaded guards or other dimensions;
- shared or multiplayer-owned Squads;
- Squad formations, named patrol routes, Move Here, Defend Area, or stances;
- collective route creation or editing.

## Persistence Model

### Player Squad registry

Each player owns a persistent, versioned `SquadRegistry` attachment. It contains
an ordered collection of Squad definitions. Each definition contains:

- a stable random UUID;
- a validated custom display name;
- an optional Officer guard UUID.

The registry is the source of truth for whether a Squad exists and who leads
it. It deliberately does not contain a member list. Keeping membership out of
the registry prevents duplicated authority and avoids turning this milestone
into the Global Guard Registry.

The codec must preserve unsupported future schemas without overwriting them.
Malformed supported data is repaired to a canonical representation when that
can be done without inventing identity. Duplicate Squad IDs retain the first
valid definition. Invalid definitions and invalid Officer UUID fields are
discarded or cleared rather than crashing world load.

### Guard Squad assignment

Each guard has a separate persistent, versioned assignment attachment containing
an optional Squad UUID. It is independent from `GuardProfile`; Captain remains
a profile role and Squad membership is organizational state.

A guard is effectively assigned only when all of these are true:

1. the guard is hired by the querying player;
2. the referenced Squad exists in that player's registry;
3. the data schema is supported.

If an assignment references a deleted or foreign Squad, it is treated as
Unassigned and repaired when the guard is next loaded, listed, or mutated.
This gives deletion deterministic behavior even for members that were unloaded
when the Squad was removed.

## Invariants

- Squad names are trimmed, non-empty, length-bounded, and unique per owner using
  case-insensitive comparison.
- A configured maximum limits Squads per player and guards assigned per Squad.
- A guard can belong to zero or one Squad.
- Only the guard's owner may change its assignment.
- A Squad may have zero or one Officer.
- An Officer must be alive, hired by the same player, assigned to the Squad,
  loaded when selected, and have `GuardProfileView.captain() == true`.
- Moving or unassigning the current Officer clears the Squad's Officer field.
- Deleting a Squad removes its definition immediately. Loaded former members
  are cleared eagerly; unloaded former members become Unassigned through lazy
  repair.
- Deleting a Squad never deletes guards and never changes their current orders.
- A Captain remains a Captain when removed as Officer or moved between Squads.

## Services and Boundaries

### Pure data and codecs

`SquadDefinition`, `SquadRegistry`, and `GuardSquadAssignment` are immutable
values. Dedicated codecs own schema validation and canonical repair. Repository
classes are the only code that reads or writes their attachments.

### Squad service

A server-side `SquadService` owns create, rename, delete, assign, unassign, and
Officer operations. It validates ownership, limits, names, membership, and
Captain status before mutation. Results are explicit success or rejection
values with localized feedback keys; expected invalid input does not throw.

The service applies multi-object changes in a safe order. For example, moving
an Officer first clears the old leadership link and then persists the new guard
assignment. If a precondition fails, no state is changed.

### Scope resolution

A small immutable command scope identifies one of:

- All owned loaded guards;
- Unassigned owned loaded guards;
- one Squad UUID.

The server resolves scopes against the current player's registry and currently
loaded guards in the player's dimension. Entity IDs, Squad IDs, membership, and
ownership are revalidated for every packet. Unknown Squad IDs are rejected.

The existing individual `GuardCommandService` remains the authority for each
guard action so eligibility hooks and mod integrations continue to run. A batch
executor resolves the selected guards and invokes the service once per member,
then returns counts for successful, rejected, and no-longer-available guards.
One member's rejection does not block eligible members.

Patrol uses the commander's current block position for every selected member,
matching the existing individual action. Route operations remain individual.

## Networking

Opening the Ledger requests a fresh server snapshot. The response includes:

- the ordered Squad definitions and Officer UUIDs;
- each loaded owned guard's existing command data;
- guard UUID, effective Squad UUID, and Captain flag;
- enough identity data to label the Officer and assignment controls.

Separate C2S mutations cover Squad create, rename, delete, guard assignment,
and Officer selection. A scoped action packet carries a validated scope and one
of the supported collective action identifiers. Attack packets also carry the
selected scope.

After every accepted Squad mutation, the server sends a complete updated Ledger
snapshot. Rejected requests send localized feedback and do not let the client
optimistically commit state. Collective actions return aggregated feedback and
do not require a full snapshot unless they change data shown by the Ledger.

Packet codecs bound collection sizes and strings before allocating or accepting
data. Server-side validation remains mandatory even when the codec accepts the
wire shape.

## Commander's Ledger UI

The existing guard list remains the primary screen. A scope selector filters it
by All Guards, Unassigned, or a named Squad. Individual Summon, Follow, Wait,
Patrol, and Route controls remain available for visible rows.

When a Squad or Unassigned scope is selected, a compact collective command bar
offers Summon, Follow, Wait, and Patrol. All Guards also supports the same batch
commands. The Combat screen receives and displays the selected scope and submits
attack orders with it.

A `Squads...` button opens a dedicated management screen. That screen supports:

- creating a Squad through a validated text field;
- selecting and renaming a Squad;
- deleting a Squad with an explicit confirmation step;
- assigning or unassigning loaded owned guards;
- selecting or clearing the Officer from eligible Captains in the Squad.

UI controls disable actions that are locally known to be invalid, but the server
still performs all authoritative checks. Pagination and bounded layouts must
remain usable at the project's supported minimum window dimensions.

## Configuration

Add conservative, clamped settings:

- `squads.enabled`, default `true`;
- `squads.maxSquadsPerPlayer`, default `16`;
- `squads.maxMembersPerSquad`, default `32`;
- `squads.maxNameLength`, default `24`.

Disabling Squads hides management and collective Squad controls but does not
delete persistent data. Existing individual Ledger behavior remains available.

## Error Handling and Consistency

- Malformed, stale, foreign, or unauthorized packet data is rejected safely.
- Duplicate and blank names are rejected with specific feedback.
- Limit failures do not partially mutate either player or guard data.
- Stale screens recover by receiving a fresh snapshot after accepted changes
  and by showing feedback after rejection.
- A guard disappearing between snapshot and action is counted as unavailable.
- Future-schema player or guard data remains untouched and unavailable to Squad
  mutation until the running mod supports it.
- Failures affecting one guard in a batch are reported in aggregate and do not
  roll back commands already accepted for other guards.

## Compatibility and API

Existing profile, recruitment, command, eligibility, and presentation APIs keep
their current behavior. Squad commands call the public guard command service
rather than bypassing it.

Expose a small read-only Squad API so integrations can inspect effective Squad
identity, name, and Officer status without obtaining mutable registry objects.
Mutation stays behind `SquadService` to preserve invariants.

Existing worlds require no manual migration. Players begin with an empty Squad
registry, and existing hired guards begin Unassigned. Existing Guard Profiles
are not rewritten for this feature.

## Testing

Implementation follows test-driven development. Automated coverage must include:

- player registry and guard assignment codec round trips;
- malformed supported data repair and future-schema preservation;
- name normalization, duplicate rejection, and configured limits;
- create, rename, delete, assign, move, and unassign operations;
- Officer eligibility, replacement, clearing, and Captain preservation;
- stale assignment repair after Squad deletion;
- All, Unassigned, and named-Squad scope resolution;
- batch command partial success and ownership enforcement;
- attack filtering by scope;
- payload symmetry and collection/string bounds;
- English and Brazilian Portuguese resource contracts;
- relevant layout calculations at narrow window sizes;
- regression coverage for existing individual commands and routes.

The final verification gate is the full Gradle test suite plus a production
build. The manual smoke matrix covers persistence across save/reload, multiple
players, unloaded members, Officer replacement, all collective orders, and GUI
navigation.

## Delivery Sequence

1. Persistent registry, assignments, codecs, and repositories.
2. Validated Squad service and Officer rules.
3. Scope resolution and collective command execution.
4. Network snapshot and mutation/action payloads.
5. Ledger filtering, management screen, and scoped Combat flow.
6. Configuration, localization, API documentation, roadmap, changelog, and
   smoke-test documentation.

