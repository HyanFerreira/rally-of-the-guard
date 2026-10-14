# Squads & Officers Implementation Plan

> Implement this plan task-by-task with strict red-green-refactor cycles. Do not
> add production behavior before its failing test has been observed.

**Goal:** Add persistent player-owned Squads, loaded-guard membership, scoped
Ledger commands, and one existing Captain Officer per Squad.

**Architecture:** Store versioned Squad definitions on the owning player and a
versioned optional Squad UUID on each guard. Keep mutation behind a validated
server service, resolve loaded guards through an immutable command scope, and
extend the current Ledger snapshot/network flow instead of creating a global
guard registry.

**Tech Stack:** Java 25, Minecraft 26.1, Fabric API attachments/networking,
MidnightLib configuration, JUnit 5, Gradle.

**Spec:** `docs/superpowers/specs/2026-10-14-squads-officers-design.md`

## File Structure

New Squad domain files live in `net.hfstack.rallyguard.squad`; public read-only
types live in `api.squad`; wire records remain in `network.payload`; client UI
remains in `screen`. Persistence codecs know NBT only, repositories know Fabric
attachments only, and services own validation and multi-record mutations.

## Review Focus

- A future registry or assignment schema must never be overwritten; Task 1 adds
  explicit decode and repository-write tests.
- Moving the current Officer must clear the old leadership link without changing
  the Captain profile; Task 2 tests this exact transition.
- A deleted Squad referenced by an unloaded guard must become Unassigned when
  loaded later; Tasks 2 and 3 test lazy repair and effective matching.
- A forged Squad UUID or entity ID must not cross ownership boundaries; Tasks 2,
  3, and 4 test service and networking validation.
- Partial batch failure must still command eligible members exactly once and
  report accurate counts; Task 3 pins the aggregate result.

### Task 1: Versioned Squad persistence

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/squad/SquadDefinition.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/SquadRegistry.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/SquadRegistryReadResult.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/SquadRegistryCodec.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/GuardSquadAssignment.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/GuardSquadAssignmentReadResult.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/GuardSquadAssignmentCodec.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/SquadPersistencePolicy.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/SquadAttachments.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/SquadRegistryRepository.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/GuardSquadRepository.java`
- Modify: `src/main/java/net/hfstack/rallyguard/RallyOfTheGuard.java`
- Test: `src/test/java/net/hfstack/rallyguard/squad/SquadRegistryCodecTest.java`
- Test: `src/test/java/net/hfstack/rallyguard/squad/GuardSquadAssignmentCodecTest.java`
- Test: `src/test/java/net/hfstack/rallyguard/squad/SquadPersistencePolicyTest.java`

**Interfaces:**
- `SquadDefinition(UUID id, String name, Optional<UUID> officerGuardId)`
- `SquadRegistry(int schemaVersion, List<SquadDefinition> squads)` with
  `CURRENT_SCHEMA_VERSION = 1` and `empty()`.
- `GuardSquadAssignment(int schemaVersion, Optional<UUID> squadId)` with
  `CURRENT_SCHEMA_VERSION = 1` and `unassigned()`.
- Both codecs expose `encode(value)` and `decode(@Nullable CompoundTag)` and
  return `Absent`, `Available(value, repaired)`, or `FutureSchema`.
- Repositories expose `read`, `save`, and canonical-repair operations; `save`
  returns `false` for future schemas.

- [ ] **Step 1: Write failing codec tests** for complete round trips, absent
  tags, malformed UUID/name/list fields, duplicate IDs keeping the first valid
  Squad, invalid Officer clearing, canonical repair, and future schemas leaving
  the source `CompoundTag` byte-for-byte unchanged.
- [ ] **Step 2: Run**
  `./gradlew.bat test --tests "net.hfstack.rallyguard.squad.*CodecTest" --tests "net.hfstack.rallyguard.squad.SquadPersistencePolicyTest"`
  and confirm compilation/test failure because the Squad persistence types do
  not exist.
- [ ] **Step 3: Implement immutable values and codecs** with schema keys
  `schema_version`, `squads`, `id`, `name`, `officer`, and `squad_id`; decoding
  must never mutate the supplied tag.
- [ ] **Step 4: Implement persistent attachments and repositories** using one
  player attachment `rallyguard:squad_registry` and one guard attachment
  `rallyguard:squad_assignment`; register them from common initialization.
- [ ] **Step 5: Re-run the focused tests**, then run `./gradlew.bat test` and
  confirm all tests pass.
- [ ] **Step 6: Commit** `feat(squads): add persistent squad data`.

### Task 2: Validated Squad management and Officer rules

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/api/squad/SquadView.java`
- Create: `src/main/java/net/hfstack/rallyguard/api/squad/GuardSquadView.java`
- Create: `src/main/java/net/hfstack/rallyguard/api/squad/SquadService.java`
- Create: `src/main/java/net/hfstack/rallyguard/api/squad/SquadMutationResult.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/SquadNamePolicy.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/SquadRegistryEditor.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/SquadMembershipPolicy.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/DefaultSquadService.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/GuardSquadAccess.java`
- Modify: `src/main/java/net/hfstack/rallyguard/api/RallyGuardApi.java`
- Modify: `src/main/java/net/hfstack/rallyguard/config/RallyConfig.java`
- Test: `src/test/java/net/hfstack/rallyguard/squad/SquadNamePolicyTest.java`
- Test: `src/test/java/net/hfstack/rallyguard/squad/SquadRegistryEditorTest.java`
- Test: `src/test/java/net/hfstack/rallyguard/squad/SquadMembershipPolicyTest.java`
- Test: `src/test/java/net/hfstack/rallyguard/squad/DefaultSquadServiceTest.java`
- Modify: `src/test/java/net/hfstack/rallyguard/config/RallyConfigTest.java`

**Interfaces:**
- `SquadService` methods: `create(ServerPlayer, String)`,
  `rename(ServerPlayer, UUID, String)`, `delete(ServerPlayer, UUID)`,
  `assign(ServerPlayer, GuardEntity, Optional<UUID>)`, and
  `setOfficer(ServerPlayer, UUID, Optional<GuardEntity>)`.
- `SquadMutationResult` is a sealed success/rejection result carrying a localized
  feedback key and the affected Squad ID when one exists.
- `GuardSquadAccess.find(ServerPlayer, GuardEntity)` returns a read-only effective
  view and lazily clears stale assignments for supported writable schemas.
- Configuration getters: `squadsEnabled()`, `maxSquadsPerPlayer()` clamped 1–64,
  `maxMembersPerSquad()` clamped 1–256, and `maxSquadNameLength()` clamped 1–48.

- [ ] **Step 1: Write failing pure policy/editor tests** for trim and Unicode-safe
  length validation, blank/duplicate names, deterministic registry ordering,
  maximum Squads, missing IDs, and Officer replacement/clearing.
- [ ] **Step 2: Run the focused tests** and confirm failure from missing APIs.
- [ ] **Step 3: Implement name policy and immutable registry editor**; compare
  names case-insensitively with `Locale.ROOT` and never expose mutable lists.
- [ ] **Step 4: Write failing service tests** using a small in-memory store seam
  for ownership and loaded-guard lookup. Cover create/rename/delete, member
  limit, cross-owner denial, stale assignment repair, move/unassign, Captain
  eligibility, one Officer, Officer removal on move, and no partial mutation on
  rejection.
- [ ] **Step 5: Run service tests and confirm expected failures.**
- [ ] **Step 6: Implement `DefaultSquadService` and read-only access**, using
  `GuardProfileRepository` only to validate the persisted Captain flag; never
  rewrite `GuardProfile` during Squad operations.
- [ ] **Step 7: Add and test configuration defaults** `true`, `16`, `32`, and
  `24`, including all clamps and disabled behavior.
- [ ] **Step 8: Run focused tests and the full suite.**
- [ ] **Step 9: Commit** `feat(squads): add management and officer rules`.

### Task 3: Command scopes and collective execution

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/api/command/GuardCommandScope.java`
- Create: `src/main/java/net/hfstack/rallyguard/api/command/GuardCommandScopeKind.java`
- Create: `src/main/java/net/hfstack/rallyguard/api/command/GuardBatchCommandResult.java`
- Create: `src/main/java/net/hfstack/rallyguard/squad/GuardScopeMatcher.java`
- Create: `src/main/java/net/hfstack/rallyguard/service/GuardBatchCommandExecutor.java`
- Create: `src/main/java/net/hfstack/rallyguard/combat/ScopedAttackPolicy.java`
- Modify: `src/main/java/net/hfstack/rallyguard/network/GuardCommandNetworking.java`
- Create: `src/main/java/net/hfstack/rallyguard/network/GuardLedgerSnapshotFactory.java`
- Test: `src/test/java/net/hfstack/rallyguard/squad/GuardScopeMatcherTest.java`
- Test: `src/test/java/net/hfstack/rallyguard/service/GuardBatchCommandExecutorTest.java`
- Test: `src/test/java/net/hfstack/rallyguard/combat/ScopedAttackPolicyTest.java`

**Interfaces:**
- `GuardCommandScope.all()`, `unassigned()`, and `squad(UUID)` create only valid
  scope shapes.
- `GuardScopeMatcher.matches(scope, effectiveSquadId)` handles All, Unassigned,
  and exact Squad identity.
- `GuardBatchCommandExecutor.execute(commander, scope, action)` resolves only
  alive, loaded, same-dimension, owned guards and returns success, rejected, and
  unavailable counts.

- [ ] **Step 1: Write failing scope tests** for all three scopes, null/invalid
  shapes, stale assignment as Unassigned, and unknown Squad rejection.
- [ ] **Step 2: Run focused tests and confirm missing-type failures.**
- [ ] **Step 3: Implement immutable scopes and matching.**
- [ ] **Step 4: Write failing batch tests** proving each eligible guard invokes
  the existing `GuardCommandService` once, ineligible guards are excluded,
  individual rejection does not stop the batch, and counts remain exact.
- [ ] **Step 5: Implement batch execution** for Summon, Follow, Wait, and Patrol;
  patrol supplies the commander's current `BlockPos` to each service call.
- [ ] **Step 6: Extract scoped attack matching** from networking and test All,
  Unassigned, named Squad, ownership, follow/rally state, distance, and
  infantry/ranged filters without changing existing target validation.
- [ ] **Step 7: Run focused tests and the full suite.**
- [ ] **Step 8: Commit** `feat(commands): add scoped squad orders`.

### Task 4: Server-authoritative Squad networking

**Files:**
- Modify: `src/main/java/net/hfstack/rallyguard/network/payload/GuardListS2CPayload.java`
- Create: `src/main/java/net/hfstack/rallyguard/network/payload/SquadDefinitionC2SPayload.java`
- Create: `src/main/java/net/hfstack/rallyguard/network/payload/GuardSquadAssignmentC2SPayload.java`
- Create: `src/main/java/net/hfstack/rallyguard/network/payload/SquadOfficerC2SPayload.java`
- Create: `src/main/java/net/hfstack/rallyguard/network/payload/ScopedGuardActionC2SPayload.java`
- Modify: `src/main/java/net/hfstack/rallyguard/network/payload/GuardAttackTargetC2SPayload.java`
- Modify: `src/main/java/net/hfstack/rallyguard/network/NetworkConstants.java`
- Modify: `src/main/java/net/hfstack/rallyguard/network/NetworkBootstrap.java`
- Modify: `src/main/java/net/hfstack/rallyguard/network/GuardCommandNetworking.java`
- Test: `src/test/java/net/hfstack/rallyguard/network/SquadPayloadCodecTest.java`
- Test: `src/test/java/net/hfstack/rallyguard/network/GuardLedgerSnapshotFactoryTest.java`

**Interfaces:**
- Extend each Ledger guard entry with `UUID guardId`, `Optional<UUID> squadId`,
  and `boolean captain`; add `List<SquadEntry>` to the snapshot payload.
- Squad definition actions are `CREATE`, `RENAME`, and `DELETE`; payload codecs
  cap names at 48 wire characters and lists at 64 Squads/4096 guards before
  allocation.
- Every accepted mutation calls one `sendGuardList(player)` refresh; every
  rejection sends only feedback and leaves state unchanged.

- [ ] **Step 1: Write failing codec symmetry/bounds tests** for every new payload,
  optional UUID, invalid enum/action values, oversized strings, and oversized
  declared collection sizes.
- [ ] **Step 2: Run codec tests and confirm expected failures.**
- [ ] **Step 3: Implement payloads and register their types.**
- [ ] **Step 4: Write failing snapshot factory tests** for Squad ordering,
  effective Unassigned repair, Captain metadata, Officer identity, and guards
  loaded only in the commander's current dimension.
- [ ] **Step 5: Implement a focused `GuardLedgerSnapshotFactory`** and replace the
  inline list assembly in `GuardCommandNetworking`.
- [ ] **Step 6: Register mutation and scoped-action receivers**; resolve all
  entity/Squad IDs on the server, invoke `SquadService`/batch executor, aggregate
  feedback, and refresh only after accepted mutations.
- [ ] **Step 7: Extend attack handling with the transmitted scope** and reject
  unknown/foreign Squad IDs before selecting guards.
- [ ] **Step 8: Run networking tests and the full suite.**
- [ ] **Step 9: Commit** `feat(network): synchronize squads and scoped commands`.

### Task 5: Ledger filtering and Squad management UI

**Files:**
- Modify: `src/client/java/net/hfstack/rallyguard/screen/GuardCommandScreen.java`
- Modify: `src/client/java/net/hfstack/rallyguard/screen/GuardCombatScreen.java`
- Create: `src/client/java/net/hfstack/rallyguard/screen/SquadManagementScreen.java`
- Create: `src/main/java/net/hfstack/rallyguard/screen/SquadScreenModel.java`
- Modify: `src/client/java/net/hfstack/rallyguard/RallyOfTheGuardClient.java`
- Modify: `src/main/java/net/hfstack/rallyguard/screen/ScreenLayout.java`
- Test: `src/test/java/net/hfstack/rallyguard/screen/SquadScreenModelTest.java`
- Modify: `src/test/java/net/hfstack/rallyguard/screen/ScreenLayoutTest.java`

**Interfaces:**
- `SquadScreenModel` owns immutable snapshot-derived filtering, selected scope,
  page clamping, eligible Officer choices, and labels; Minecraft widgets only
  render it and emit payloads.
- `GuardCommandScreen` preserves selected scope when refreshed if it still
  exists, otherwise falls back to All Guards.
- `GuardCombatScreen(Screen parent, GuardCommandScope scope)` includes the scope
  in every attack packet; the global hotkey continues to open with All Guards.

- [ ] **Step 1: Write failing model tests** for All/Unassigned/Squad filters,
  selection fallback after deletion, pagination after filtering, Officer choices
  restricted to Captains in the selected Squad, and exact member counts.
- [ ] **Step 2: Run model tests and confirm missing behavior.**
- [ ] **Step 3: Implement the pure screen model.**
- [ ] **Step 4: Write failing layout tests** for selector, collective bar, rows,
  management controls, and confirmation layout at the supported narrow width.
- [ ] **Step 5: Implement Ledger controls** for scope cycling/selection,
  individual actions, scoped batch actions, and `Squads...`; keep Route per row.
- [ ] **Step 6: Implement `SquadManagementScreen`** with create, rename, delete
  confirmation, member assignment/unassignment, Officer select/clear, and
  server-snapshot refresh without optimistic committed state.
- [ ] **Step 7: Pass the current scope into Combat** and use All for the hotkey.
- [ ] **Step 8: Run client compile, focused tests, and full tests:**
  `./gradlew.bat compileClientJava test`.
- [ ] **Step 9: Commit** `feat(ui): manage and command squads from the ledger`.

### Task 6: Configuration, localization, public docs, and release status

**Files:**
- Modify: `src/main/resources/assets/rallyguard/lang/en_us.json`
- Modify: `src/main/resources/assets/rallyguard/lang/pt_br.json`
- Modify: `src/test/java/net/hfstack/rallyguard/ResourceSchemaTest.java`
- Modify: `README.md`
- Modify: `docs/API.md`
- Modify: `CHANGELOG.md`
- Modify: `ROADMAP.md`
- Create: `docs/testing/26.1-squads-officers-smoke-test.md`

**Interfaces:**
- Document the read-only Squad API and state explicitly that only loaded guards
  in the commander's current dimension are listed/commanded.
- Mark `Captains` and `Squads` complete only after automated and manual-ready
  verification; do not mark formations or tactical-order roadmap items complete.

- [ ] **Step 1: Add failing resource contract assertions** for every new config,
  screen, feedback, scope, batch-result, and Officer label in both languages.
- [ ] **Step 2: Run `ResourceSchemaTest` and confirm missing-key failures.**
- [ ] **Step 3: Add English and Brazilian Portuguese translations** and rerun the
  resource test.
- [ ] **Step 4: Update README/API/changelog** with persistence, limits, commands,
  Officer invariants, future-schema behavior, and compatibility constraints.
- [ ] **Step 5: Write the manual smoke matrix** covering create/rename/delete,
  save/reload, two players, same-name guards, unloaded former members, Officer
  replacement/removal, every collective order, scoped attacks, disabled config,
  and narrow-window navigation.
- [ ] **Step 6: Update roadmap checkboxes only after Task 7 verification.**
- [ ] **Step 7: Run full tests before committing documentation.**
- [ ] **Step 8: Commit** `docs: document squads and officers`.

### Task 7: Final verification and release-quality review

**Files:**
- Review: all files changed by Tasks 1–6
- Modify if needed: tests and documentation covering defects found during review

- [ ] **Step 1: Compare the implementation line-by-line with the approved spec**
  and record/fix every missing or extra behavior through a failing regression
  test before production changes.
- [ ] **Step 2: Run the complete automated suite:** `./gradlew.bat test`.
  Expected: `BUILD SUCCESSFUL`, zero failed tests.
- [ ] **Step 3: Run the production build:** `./gradlew.bat build`.
  Expected: `BUILD SUCCESSFUL`, remapped JAR and sources JAR produced.
- [ ] **Step 4: Run** `git diff --check` and inspect `git status --short` plus the
  complete branch diff for accidental generated files or unrelated edits.
- [ ] **Step 5: Execute the feasible smoke checks** from
  `docs/testing/26.1-squads-officers-smoke-test.md`; clearly record any checks
  requiring an interactive Minecraft client rather than claiming them complete.
- [ ] **Step 6: Mark the Roadmap entries complete** for `Captains` and `Squads`
  only if their implementation and evidence are present.
- [ ] **Step 7: Commit verification fixes/status updates** as
  `chore(release): finalize squads and officers` if changes remain.

