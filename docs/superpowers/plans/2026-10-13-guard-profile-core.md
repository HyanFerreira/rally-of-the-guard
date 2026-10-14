# Guard Profile Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a versioned, server-authoritative Guard Villager profile that persists through Fabric entity attachments, safely initializes legacy guards, preserves future schemas, and exposes a read-only API.

**Architecture:** Keep the profile as a validated immutable domain record and encode it into a persistent `CompoundTag` attachment registered as `rallyguard:profile`. A repository is the only persistence boundary; a server entity-load handler initializes missing legacy profiles, while a public facade exposes immutable views. Natural randomization and natural-spawn classification belong to the next plan and initially use conservative neutral level-1 legacy defaults.

**Tech Stack:** Java 25, Fabric Loader 0.18.4, Fabric API 0.144.3+26.1 Data Attachment API, Minecraft 26.1, Guard Villagers, JUnit 5.11.4, Gradle 9.5.1.

**Spec:** `docs/superpowers/specs/2026-10-13-guard-profiles-progression-design.md`

## Global Constraints

- The server is authoritative; this plan adds no client-to-server profile mutation.
- Persist exactly one attachment with identifier `rallyguard:profile` on each initialized Guard Villager.
- Schema version and generation version both start at `1`.
- XP uses non-negative `long` thousandths; quality is `0..10000`; rolls are `-10000..10000`; statistics are non-negative `long` values.
- Level, rank, stars, final attributes, and hiring value are derived and must not be persisted by this plan.
- A schema newer than `1` must remain byte-for-byte logically untouched and unavailable for mutation.
- Missing or malformed version-1 fields are repaired conservatively without discarding other valid version-1 fields.
- Entity-load handling must be idempotent and must not attach profiles to non-Guard Villager entities.
- No mixin is required for persistence on 26.1: `Entity` already implements Fabric `AttachmentTarget` and Fabric API provides persistent attachments.
- No code in this plan may change Guard Villagers AI, attributes, combat, orders, or recruitment behavior.

## Review Focus

- A future-schema attachment must survive reads and entity saves without being decoded into or overwritten by the version-1 model; Task 3 tests and the manual smoke test pin this behavior.
- A partial version-1 compound must preserve each valid field while defaulting or clamping only invalid fields; Task 2 tests every field class, and Task 4 performs the canonical rewrite during lifecycle handling rather than during public reads.
- Extreme signed numeric values must not wrap when normalized; Task 1 and Task 2 use `Long.MIN_VALUE`, negative counters, and integer roll bounds.
- Repeated entity-load callbacks must retain the first profile and never recreate it; Task 4 tests the pure initialization decision and the smoke matrix exercises the registered callback.
- Non-guard entities and guards carrying future data must remain unchanged; Task 4 tests policy outcomes and the smoke matrix verifies runtime filtering.

---

## File Structure

### Public API

- `src/main/java/net/hfstack/rallyguard/api/profile/GuardProfileView.java` — immutable read contract exposed to integrations.
- `src/main/java/net/hfstack/rallyguard/api/profile/GuardProfileAccess.java` — public read-only lookup facade.

### Internal profile domain

- `src/main/java/net/hfstack/rallyguard/profile/GuardProfileOrigin.java` — stable persisted origin identifiers.
- `src/main/java/net/hfstack/rallyguard/profile/GuardProfile.java` — validated immutable schema-v1 value implementing the public view.
- `src/main/java/net/hfstack/rallyguard/profile/GuardProfileReadResult.java` — distinguishes an available v1 profile from unsupported future data.
- `src/main/java/net/hfstack/rallyguard/profile/GuardProfileCodec.java` — version-aware `CompoundTag` encoding, repair, and decoding.

### Persistence and lifecycle

- `src/main/java/net/hfstack/rallyguard/profile/GuardProfileAttachments.java` — registers the persistent raw compound attachment.
- `src/main/java/net/hfstack/rallyguard/profile/GuardProfilePersistencePolicy.java` — pure overwrite and canonical-repair decisions.
- `src/main/java/net/hfstack/rallyguard/profile/GuardProfileRepository.java` — only read/create/save boundary for entity attachments.
- `src/main/java/net/hfstack/rallyguard/profile/GuardProfileInitializationPolicy.java` — pure decision for entity-load initialization.
- `src/main/java/net/hfstack/rallyguard/profile/GuardProfileLifecycle.java` — registers the server entity-load callback.
- `src/main/java/net/hfstack/rallyguard/RallyOfTheGuard.java` — initializes attachment and lifecycle registration.

### Tests and docs

- `src/test/java/net/hfstack/rallyguard/profile/GuardProfileTest.java`
- `src/test/java/net/hfstack/rallyguard/profile/GuardProfileCodecTest.java`
- `src/test/java/net/hfstack/rallyguard/profile/GuardProfilePersistencePolicyTest.java`
- `src/test/java/net/hfstack/rallyguard/profile/GuardProfileInitializationPolicyTest.java`
- `src/test/java/net/hfstack/rallyguard/api/profile/GuardProfileAccessTest.java`
- `docs/API.md` — documents the read-only profile API and future-schema behavior.
- `docs/testing/26.1-smoke-test.md` — adds profile persistence and idempotence checks.

## Task 1: Immutable Profile Domain

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/api/profile/GuardProfileView.java`
- Create: `src/main/java/net/hfstack/rallyguard/profile/GuardProfileOrigin.java`
- Create: `src/main/java/net/hfstack/rallyguard/profile/GuardProfile.java`
- Test: `src/test/java/net/hfstack/rallyguard/profile/GuardProfileTest.java`

**Interfaces:**
- Consumes: no feature-specific interfaces.
- Produces: `GuardProfileView`; `GuardProfileOrigin.fromSerializedName(String)`; `GuardProfile.createV1(GuardProfileOrigin, long, int, int, int, int, int, long, long)`; `GuardProfile.legacyNeutral()`; schema constants and immutable accessors used by every later task.

- [ ] **Step 1: Write failing domain-boundary tests**

Add tests named:

```text
createV1ClampsQualityToZeroThroughTenThousand
createV1ClampsEveryRollToSignedTenThousand
createV1ClampsNegativeXpKillsAndAssistsToZero
createV1HandlesLongMinValueWithoutOverflow
legacyNeutralUsesV1LegacyLevelOneDefaults
unknownOriginNameMapsToOther
```

Assert that `legacyNeutral()` returns schema `1`, generation `1`, origin
`LEGACY`, zero XP/statistics/rolls, and quality `5000`. Assert exact lower and
upper boundary values rather than only checking ranges.

- [ ] **Step 2: Run the test to verify it fails**

Run:

```powershell
.\gradlew.bat test --tests "net.hfstack.rallyguard.profile.GuardProfileTest"
```

Expected: FAIL because the profile types do not exist.

- [ ] **Step 3: Define the public read contract**

Create `GuardProfileView` with these accessors:

```java
int schemaVersion();
int generationVersion();
String origin();
long totalXpMilli();
int naturalQuality();
int healthRoll();
int attackRoll();
int armorRoll();
int speedRoll();
long kills();
long assists();
```

The API returns the serialized origin string rather than the internal enum.

- [ ] **Step 4: Implement the origin and immutable profile**

Create enum constants `NATURAL`, `LEGACY`, `COMMAND`, and `OTHER`, each with a
lowercase serialized name. Implement the profile as an immutable record whose
internal origin component is named `originType`; implement the public
`String origin()` accessor by returning `originType.serializedName()`. Use
constants `CURRENT_SCHEMA_VERSION = 1`, `CURRENT_GENERATION_VERSION = 1`,
`QUALITY_MIN = 0`, `QUALITY_MAX = 10000`, `ROLL_MIN = -10000`, and
`ROLL_MAX = 10000`.

Use this exact normalized factory signature:

```java
public static GuardProfile createV1(
    GuardProfileOrigin originType,
    long totalXpMilli,
    int naturalQuality,
    int healthRoll,
    int attackRoll,
    int armorRoll,
    int speedRoll,
    long kills,
    long assists
);
```

Do not accept a caller-provided schema or generation version through this
factory. `legacyNeutral()` supplies the conservative temporary generator that
the Natural Identity plan will replace.

- [ ] **Step 5: Run the focused and full unit suites**

Run:

```powershell
.\gradlew.bat test --tests "net.hfstack.rallyguard.profile.GuardProfileTest"
.\gradlew.bat test
```

Expected: both commands report `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit the domain model**

```powershell
git add src/main/java/net/hfstack/rallyguard/api/profile/GuardProfileView.java src/main/java/net/hfstack/rallyguard/profile/GuardProfileOrigin.java src/main/java/net/hfstack/rallyguard/profile/GuardProfile.java src/test/java/net/hfstack/rallyguard/profile/GuardProfileTest.java
git commit -m "feat(profile): define persistent guard profile model"
```

## Task 2: Version-Aware Compound Codec

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/profile/GuardProfileReadResult.java`
- Create: `src/main/java/net/hfstack/rallyguard/profile/GuardProfileCodec.java`
- Test: `src/test/java/net/hfstack/rallyguard/profile/GuardProfileCodecTest.java`

**Interfaces:**
- Consumes: `GuardProfile`, `GuardProfileOrigin`, and their constants from Task 1.
- Produces: `GuardProfileCodec.encode(GuardProfile): CompoundTag`; `GuardProfileCodec.decode(CompoundTag): GuardProfileReadResult`; `GuardProfileReadResult.Absent`; `GuardProfileReadResult.Available(profile, repaired)`; `GuardProfileReadResult.FutureSchema(schemaVersion)`.

- [ ] **Step 1: Write failing round-trip and repair tests**

Add tests named:

```text
roundTripPreservesEverySchemaV1Field
missingSchemaIsRepairedAsVersionOne
partialVersionOnePreservesValidFieldsAndDefaultsMissingFields
negativeCountersAndOutOfRangeRollsAreClamped
unknownOriginBecomesOtherAndMarksResultRepaired
malformedTypedFieldsUseConservativeDefaultsAndMarkRepaired
futureSchemaReturnsFutureResultWithoutProfile
encodeWritesOnlyTheApprovedSchemaFields
nullCompoundReturnsAbsent
futureSchemaDecodeDoesNotMutateOriginalCompound
decodeDoesNotMutateRepairableOriginalCompound
```

For a partial compound, set a valid quality and attack roll, omit health roll,
and make kills negative. Assert that quality and attack survive, health becomes
zero, kills become zero, and `repaired` is true. For future schema `2`, assert
that the result contains only the version and offers no `GuardProfile`.

- [ ] **Step 2: Run the codec test to verify it fails**

Run:

```powershell
.\gradlew.bat test --tests "net.hfstack.rallyguard.profile.GuardProfileCodecTest"
```

Expected: FAIL because codec and result types do not exist.

- [ ] **Step 3: Define the read-result union**

Create a public sealed interface with exactly:

```java
record Absent() implements GuardProfileReadResult {}
record Available(GuardProfile profile, boolean repaired) implements GuardProfileReadResult {}
record FutureSchema(int schemaVersion) implements GuardProfileReadResult {}
```

Reject null profiles in the `Available` constructor.

- [ ] **Step 4: Implement the version-aware codec**

Use explicit NBT keys matching the spec's snake_case names. A null compound
returns `Absent`. Decode schema
versions greater than `1` before reading any other field. Treat absent, zero,
or negative schema as a repairable version-1 compound. Use typed optional NBT
reads so a string stored under a numeric key cannot throw.

Defaults for missing or malformed v1 values are those from
`GuardProfile.legacyNeutral()`. Preserve every independently valid field and
set `repaired = true` whenever a default, clamp, or unknown origin is used.
Encoding creates a new `CompoundTag` containing only the approved v1 fields.

- [ ] **Step 5: Run codec and full tests**

Run:

```powershell
.\gradlew.bat test --tests "net.hfstack.rallyguard.profile.GuardProfileCodecTest"
.\gradlew.bat test
```

Expected: both commands report `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit the codec**

```powershell
git add src/main/java/net/hfstack/rallyguard/profile/GuardProfileReadResult.java src/main/java/net/hfstack/rallyguard/profile/GuardProfileCodec.java src/test/java/net/hfstack/rallyguard/profile/GuardProfileCodecTest.java
git commit -m "feat(profile): add versioned profile codec"
```

## Task 3: Persistent Attachment Repository

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/profile/GuardProfileAttachments.java`
- Create: `src/main/java/net/hfstack/rallyguard/profile/GuardProfilePersistencePolicy.java`
- Create: `src/main/java/net/hfstack/rallyguard/profile/GuardProfileRepository.java`
- Modify: `src/main/java/net/hfstack/rallyguard/RallyOfTheGuard.java`
- Test: `src/test/java/net/hfstack/rallyguard/profile/GuardProfilePersistencePolicyTest.java`

**Interfaces:**
- Consumes: `CompoundTag.CODEC`, Fabric `AttachmentRegistry`, `GuardProfileCodec`, and `GuardProfileReadResult`.
- Produces: persistent `AttachmentType<CompoundTag> PROFILE`; `GuardProfilePersistencePolicy.canWrite(GuardProfileReadResult)`; `GuardProfilePersistencePolicy.shouldCanonicalize(GuardProfileReadResult)`; `GuardProfileRepository.instance()`; repository methods `read(GuardEntity)`, `getOrCreateLegacy(GuardEntity)`, and `save(GuardEntity, GuardProfile)`.

- [ ] **Step 1: Write failing persistence-policy tests**

Create policy tests named:

```text
absentDataCanBeInitialized
availableDataCanBeSaved
futureSchemaCannotBeOverwritten
onlyRepairedAvailableDataRequiresCanonicalRewrite
```

Assert that absent and available results allow writes, future schemas reject
writes, and only `Available(profile, true)` requests canonical re-encoding.

- [ ] **Step 2: Run the focused test and observe the new failure**

Run:

```powershell
.\gradlew.bat test --tests "net.hfstack.rallyguard.profile.GuardProfilePersistencePolicyTest"
```

Expected: FAIL because the persistence policy does not exist.

- [ ] **Step 3: Register the persistent raw attachment**

Define:

```java
public static final AttachmentType<CompoundTag> PROFILE =
    AttachmentRegistry.createPersistent(
        Identifier.fromNamespaceAndPath(RallyOfTheGuard.MOD_ID, "profile"),
        CompoundTag.CODEC
    );

public static void initialize();
```

Call `GuardProfileAttachments.initialize()` from `RallyOfTheGuard.onInitialize()`
before lifecycle event registration. Do not add a mixin configuration.

- [ ] **Step 4: Implement persistence policy and repository boundary**

Define the pure policy signatures:

```java
public static boolean canWrite(GuardProfileReadResult current);
public static boolean shouldCanonicalize(GuardProfileReadResult current);
```

The repository must use these methods rather than duplicate future-schema and
repair decisions.

Define:

```java
public static GuardProfileRepository instance();
public GuardProfileReadResult read(GuardEntity guard);
public GuardProfileReadResult getOrCreateLegacy(GuardEntity guard);
public boolean save(GuardEntity guard, GuardProfile profile);
```

Use one process-local repository singleton with a private constructor. The
lifecycle and public API must share it; neither constructs an independent
repository.

Rules:

- `read` returns `Absent` when no attachment exists; absence is handled by
  `getOrCreateLegacy`, not hidden by `read`.
- `getOrCreateLegacy` attaches `GuardProfile.legacyNeutral()` only when absent.
- `read` never calls `setAttached`; repaired v1 data is canonicalized by the
  lifecycle handler, keeping public API reads side-effect free.
- A future schema is returned without calling `setAttached`.
- `save` returns false and leaves data untouched when the existing attachment
  has a future schema; otherwise it stores a fresh encoded compound and returns
  true.
- All public parameters are null-checked.

- [ ] **Step 5: Run tests and compile the production attachment integration**

Run:

```powershell
.\gradlew.bat test
.\gradlew.bat compileJava
```

Expected: both commands report `BUILD SUCCESSFUL`; the compile proves the
Fabric 26.1 attachment signatures are correct.

- [ ] **Step 6: Commit persistence**

```powershell
git add src/main/java/net/hfstack/rallyguard/profile/GuardProfileAttachments.java src/main/java/net/hfstack/rallyguard/profile/GuardProfilePersistencePolicy.java src/main/java/net/hfstack/rallyguard/profile/GuardProfileRepository.java src/main/java/net/hfstack/rallyguard/RallyOfTheGuard.java src/test/java/net/hfstack/rallyguard/profile/GuardProfilePersistencePolicyTest.java
git commit -m "feat(profile): persist profiles with entity attachments"
```

## Task 4: Idempotent Legacy Initialization Lifecycle

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/profile/GuardProfileInitializationPolicy.java`
- Create: `src/main/java/net/hfstack/rallyguard/profile/GuardProfileLifecycle.java`
- Modify: `src/main/java/net/hfstack/rallyguard/RallyOfTheGuard.java`
- Test: `src/test/java/net/hfstack/rallyguard/profile/GuardProfileInitializationPolicyTest.java`

**Interfaces:**
- Consumes: `GuardProfileRepository.getOrCreateLegacy(GuardEntity)` and Fabric `ServerEntityEvents.ENTITY_LOAD`.
- Produces: `GuardProfileLifecycle.register(GuardProfileRepository)` and pure `GuardProfileInitializationPolicy.shouldInitialize(boolean guardEntity, GuardProfileReadResult result)`.

- [ ] **Step 1: Write failing lifecycle decision tests**

Add tests named:

```text
absentGuardProfileRequiresInitialization
availableGuardProfileDoesNotRequireInitialization
futureGuardProfileDoesNotRequireInitialization
nonGuardEntityNeverRequiresInitialization
repeatedDecisionForAvailableProfileIsIdempotent
```

Assert exact booleans for all cases, including `guardEntity = false` with an
absent result.

- [ ] **Step 2: Run the policy test to verify it fails**

Run:

```powershell
.\gradlew.bat test --tests "net.hfstack.rallyguard.profile.GuardProfileInitializationPolicyTest"
```

Expected: FAIL because lifecycle policy does not exist.

- [ ] **Step 3: Implement the pure policy**

Define:

```java
public static boolean shouldInitialize(
    boolean guardEntity,
    GuardProfileReadResult current
);
```

Return true only for a Guard Villager with an `Absent` result.

- [ ] **Step 4: Register server entity-load initialization**

`GuardProfileLifecycle.register(GuardProfileRepository.instance())` registers exactly one
`ServerEntityEvents.ENTITY_LOAD` callback. The callback:

1. ignores every entity not instance of `GuardEntity`;
2. reads the current repository result without mutating it;
3. creates a conservative legacy profile only when policy returns true;
4. re-saves an `Available(profile, true)` result once to canonicalize it;
5. logs initialization at `debug`;
6. logs a repaired v1 profile or future schema once at `warn`;
7. never throws an initialization failure out of the callback.

Call this registration once from `RallyOfTheGuard.onInitialize()` after
attachment initialization. Do not apply attributes in this plan.

- [ ] **Step 5: Run focused tests and a full build**

Run:

```powershell
.\gradlew.bat test --tests "net.hfstack.rallyguard.profile.GuardProfileInitializationPolicyTest"
.\gradlew.bat build
```

Expected: both commands report `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit lifecycle initialization**

```powershell
git add src/main/java/net/hfstack/rallyguard/profile/GuardProfileInitializationPolicy.java src/main/java/net/hfstack/rallyguard/profile/GuardProfileLifecycle.java src/main/java/net/hfstack/rallyguard/RallyOfTheGuard.java src/test/java/net/hfstack/rallyguard/profile/GuardProfileInitializationPolicyTest.java
git commit -m "feat(profile): initialize legacy guard profiles"
```

## Task 5: Read-Only API and Verification Documentation

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/api/profile/GuardProfileAccess.java`
- Test: `src/test/java/net/hfstack/rallyguard/api/profile/GuardProfileAccessTest.java`
- Modify: `src/main/java/net/hfstack/rallyguard/api/RallyGuardApi.java`
- Modify: `docs/API.md`
- Modify: `docs/testing/26.1-smoke-test.md`

**Interfaces:**
- Consumes: `GuardProfileRepository.read(GuardEntity)` and `GuardProfileView`.
- Produces: `GuardProfileAccess.find(GuardEntity): Optional<GuardProfileView>` and `RallyGuardApi.profiles(): GuardProfileAccess`.

- [ ] **Step 1: Write failing API projection tests**

Add tests named:

```text
availableProfileIsExposedAsReadOnlyView
absentProfileReturnsEmpty
futureSchemaReturnsEmpty
returnedViewDoesNotExposePersistenceMutation
```

Use this package-private lookup function so unit tests do not need to construct
Minecraft entities:

```java
static Optional<GuardProfileView> viewOf(GuardProfileReadResult result);
```

For the last test,
assert through reflection that `GuardProfileView` contains only the approved
zero-argument accessors and no setter, attachment, NBT, or repository method.

- [ ] **Step 2: Run the API test to verify it fails**

Run:

```powershell
.\gradlew.bat test --tests "net.hfstack.rallyguard.api.profile.GuardProfileAccessTest"
```

Expected: FAIL because the access facade does not exist.

- [ ] **Step 3: Implement the read-only facade**

Define `GuardProfileAccess` as a final singleton-backed facade with:

```java
public static GuardProfileAccess instance();
public Optional<GuardProfileView> find(GuardEntity guard);
```

Return a view only for `GuardProfileReadResult.Available`. Do not create a
profile as a side effect of API reads. Expose the singleton facade from
`RallyGuardApi.profiles()` by returning `GuardProfileAccess.instance()`, without
exposing the internal repository.

- [ ] **Step 4: Document API and runtime smoke checks**

Add to `docs/API.md`:

- how to obtain `GuardProfileView`;
- field units and bounds;
- empty results for absent/future schemas;
- the guarantee that reads never initialize or mutate a profile;
- a note that level/rank helpers arrive in the Natural Identity plan.

Extend `docs/testing/26.1-smoke-test.md` with this exact matrix:

1. start a dedicated server and load an existing guard;
2. save and stop the server;
3. inspect saved entity data and confirm `rallyguard:profile` schema `1`;
4. restart twice and confirm the attachment values do not change;
5. create a new guard and confirm one profile appears after entity load;
6. load a non-guard entity and confirm no profile is attached;
7. inject a copied test entity with schema `2`, restart, and confirm its raw
   profile data remains unchanged and no v1 profile is exposed.

- [ ] **Step 5: Run all verification commands**

Run:

```powershell
.\gradlew.bat test
.\gradlew.bat build
git diff --check
```

Expected: both Gradle commands report `BUILD SUCCESSFUL`; `git diff --check`
prints no errors.

- [ ] **Step 6: Commit the public API and documentation**

```powershell
git add src/main/java/net/hfstack/rallyguard/api/profile/GuardProfileAccess.java src/main/java/net/hfstack/rallyguard/api/RallyGuardApi.java src/test/java/net/hfstack/rallyguard/api/profile/GuardProfileAccessTest.java docs/API.md docs/testing/26.1-smoke-test.md
git commit -m "feat(api): expose read-only guard profiles"
```

## Final Review Gate

- [ ] Compare every changed file with the Profile Core sections of the spec.
- [ ] Confirm the attachment identifier is exactly `rallyguard:profile`.
- [ ] Confirm no mixin file or client-only import was introduced.
- [ ] Confirm future-schema paths never call `setAttached`.
- [ ] Confirm repeated load callbacks do not change the profile.
- [ ] Confirm all tests and `build` pass from a clean process.
- [ ] Review the five Review Focus cases against their named tests.
- [ ] Record manual smoke-test results before declaring Profile Core complete.

## Subsequent Plans

After this plan is implemented and reviewed, create separate plans in this
order so they can consume proven interfaces:

1. Natural Identity and level/rank derivation.
2. Attribute modifiers and configuration refresh.
3. Combat contribution, target tags, XP, kills, and assists.
4. Dynamic recruitment quotes and atomic confirmation.
5. Muster Roll item, snapshots, networking, and screen.
6. Cross-feature hardening, old-save validation, balance, and release docs.
