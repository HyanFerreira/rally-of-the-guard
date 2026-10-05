# Allied Protection and Rally Retreat Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent hired guards from targeting or damaging their commander's owned companions and make rallied guards automatically disengage and return when the commander moves more than 24 blocks away.

**Architecture:** Centralize ally classification in `ProtectedAllyPolicy`, centralize combat cleanup in `GuardCombatState`, and express Rally behavior through a pure `RallyCombatPolicy` decision. Enforce ally safety at manual-order, AI-target, and damage-event boundaries, then deliver each tested slice from canonical `main` to `26.1`, `1.21.11`, and `1.21.1` before starting another feature.

**Tech Stack:** Java 25/Mojang mappings on Minecraft 26.1, Java 21/Yarn mappings on Minecraft 1.21.11 and 1.21.1, Fabric API entity/lifecycle events, Guard Villagers, MidnightLib, Gradle, and JUnit 5.

**Spec:** `docs/superpowers/specs/2026-10-05-allied-protection-and-rally-retreat-design.md`

## Global Constraints

- `main` is the canonical implementation branch and targets Minecraft 26.1.
- Release branches are `26.1`, `1.21.11`, and `1.21.1`; every branch must build independently.
- Ally protection is mandatory and is not disabled by the retreat configuration.
- Default retreat configuration is enabled with a distance of `24.0` blocks.
- Retreat applies only to guards marked as rallied; wait, patrol, route, and ordinary follow behavior stay unchanged.
- `formationTeleportDistance` remains the only formation teleport threshold.
- Cross-dimensional following, a Retreat button, health-based retreat, teams, and shared commanders are out of scope.
- Happy Ghasts are protected only while the commander is riding or directly leading them; a harness alone does not establish vanilla ownership.
- Do not introduce a new runtime dependency.
- Use the version-specific mappings already selected by each branch; do not try to share compiled source between Mojang- and Yarn-mapped branches in this feature.

## Review Focus

- Missing or unresolved owner reference: an untamed/unowned target must remain attackable, while a stored matching owner UUID must work without requiring the owner entity to resolve.
- Transient ownership: a ridden or directly leashed entity is protected, but a merely harnessed Happy Ghast is not classified as owned.
- Mandatory precedence: a protected target forces disengagement even when `rallyRetreatEnabled` is false.
- Boundary and recovery: exactly `24.0` blocks permits combat, greater than `24.0` disengages, and loaded rallied guards are not dropped by the old combat-search-radius filter.
- Damage-source shape: melee and projectile damage whose causing entity is the hired guard are blocked, while unrelated environmental or other-player damage remains untouched.

---

## File Structure

New focused units on every branch:

- `src/main/java/net/hfstack/rallyguard/combat/ProtectedAllyPolicy.java` — extracts persistent/transient vanilla relationships and answers whether a target is protected.
- `src/main/java/net/hfstack/rallyguard/combat/RallyCombatPolicy.java` — pure Rally combat/retreat decision table.
- `src/main/java/net/hfstack/rallyguard/combat/GuardCombatState.java` — performs the version-specific complete disengagement operation.
- `src/main/java/net/hfstack/rallyguard/event/GuardAllySafetyHandler.java` — corrects native AI targets and cancels guard-to-ally damage.
- `src/test/java/net/hfstack/rallyguard/combat/ProtectedAllyPolicyTest.java` — ownership and transient-association decision tests.
- `src/test/java/net/hfstack/rallyguard/combat/RallyCombatPolicyTest.java` — retreat precedence and distance-boundary tests.
- `docs/testing/2026-10-05-allied-protection-and-rally-retreat-manual.md` — final cross-version gameplay evidence.

Existing units to modify:

- `src/main/java/net/hfstack/rallyguard/network/GuardCommandNetworking.java` — reject protected manual targets and use shared combat cleanup.
- `src/main/java/net/hfstack/rallyguard/event/RallyFormationTicker.java` — replace unconditional combat priority with retreat decisions.
- `src/main/java/net/hfstack/rallyguard/config/RallyConfig.java` — add enabled/distance configuration and clamped accessors.
- `src/main/java/net/hfstack/rallyguard/RallyOfTheGuard.java` — register ally-safety enforcement.
- `src/main/resources/assets/rallyguard/lang/en_us.json` — English configuration labels and tooltips.
- `src/main/resources/assets/rallyguard/lang/pt_br.json` — Brazilian Portuguese configuration labels and tooltips.
- `README.md` and `CHANGELOG.md` — document behavior and configuration.

### Task 0: Isolate Branches and Establish Baselines

**Files:**
- No product files changed.
- Worktrees: current workspace for `main`; sibling worktrees for `26.1`, `1.21.11`, and `1.21.1`.

**Interfaces:**
- Consumes: clean local branches `main`, `26.1`, `1.21.11`, and `1.21.1`.
- Produces: four isolated, clean worktrees and recorded baseline build results.

- [ ] **Step 1: Invoke the required worktree workflow**

Use `superpowers:using-git-worktrees` before creating any worktree. Resolve and verify the absolute sibling paths before creation; do not reuse a non-empty unrelated directory.

- [ ] **Step 2: Create one worktree per release branch**

Use these intended sibling names unless the worktree skill finds an existing project convention:

```powershell
git worktree add '..\rally-of-the-guard-26.1' 26.1
git worktree add '..\rally-of-the-guard-1.21.11' 1.21.11
git worktree add '..\rally-of-the-guard-1.21.1' 1.21.1
```

Expected: `git worktree list` reports one unique path for each of the four branches.

- [ ] **Step 3: Run baseline tests in all four worktrees**

Run in each worktree:

```powershell
.\gradlew.bat test
```

Expected: `BUILD SUCCESSFUL` four times. If a baseline fails, stop and diagnose it before attributing failures to this feature.

- [ ] **Step 4: Record immutable starting commits**

Run `git rev-parse HEAD` in each worktree and keep the four hashes in the execution notes. These hashes define the ranges used during porting.

### Task 1: Add Pure Ally and Retreat Policies on `main`

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/combat/ProtectedAllyPolicy.java`
- Create: `src/main/java/net/hfstack/rallyguard/combat/RallyCombatPolicy.java`
- Create: `src/test/java/net/hfstack/rallyguard/combat/ProtectedAllyPolicyTest.java`
- Create: `src/test/java/net/hfstack/rallyguard/combat/RallyCombatPolicyTest.java`

**Interfaces:**
- Consumes: `GuardOwnership.getOwner(Entity)` and Minecraft 26.1 `OwnableEntity`, `Leashable`, and passenger APIs.
- Produces: `ProtectedAllyPolicy.isProtectedAlly(UUID commanderId, Entity target) -> boolean`.
- Produces: package-private `ProtectedAllyPolicy.matchesRelationship(UUID commanderId, UUID targetId, UUID persistentOwnerId, boolean commanderRidingTarget, boolean targetLeashedToCommander) -> boolean` for unit tests.
- Produces: `RallyCombatPolicy.decide(boolean hasLiveTarget, boolean protectedTarget, boolean retreatEnabled, double commanderDistanceSquared, double retreatDistance) -> RallyCombatPolicy.Decision`.
- Produces: enum values `FORM_UP`, `CONTINUE_COMBAT`, and `DISENGAGE_AND_FORM_UP`.

- [ ] **Step 1: Write the failing relationship-policy test**

Create tests named:

```java
protectsCommanderAndMatchingPersistentOwner()
protectsCurrentRiderAndDirectLeashHolder()
doesNotProtectMissingOwnerDifferentOwnerOrUnassociatedTarget()
```

Use fixed UUIDs and assert that target UUID equality, matching persistent owner, riding, and direct leash each return `true`; null/different owners with both transient flags false return `false`.

- [ ] **Step 2: Write the failing Rally decision test**

Create tests named:

```java
formsUpWithoutALiveTarget()
protectedTargetAlwaysForcesDisengagement()
retreatDisabledPreservesValidCombat()
retreatBoundaryIsExclusive()
distanceAboveBoundaryForcesDisengagement()
configuredMaximumStillDisengagesAboveBoundary()
```

Pin the normal boundary with `24.0 * 24.0`: equality returns
`CONTINUE_COMBAT`; `24.01 * 24.01` returns
`DISENGAGE_AND_FORM_UP`. Pin the configured maximum with `512.01 * 512.01`
against a retreat distance of `512.0`, also expecting disengagement.

- [ ] **Step 3: Run the tests and verify the red state**

```powershell
.\gradlew.bat test --tests 'net.hfstack.rallyguard.combat.*PolicyTest'
```

Expected: compilation fails because the two policy classes do not exist.

- [ ] **Step 4: Implement `ProtectedAllyPolicy` minimally**

Require non-null `commanderId` and `target`. For Minecraft 26.1, extract persistent ownership in this order: Rally guard via `GuardOwnership.getOwner`, then `OwnableEntity.getOwnerReference().getUUID()` when present. Determine transient association from a passenger whose UUID matches the commander and from `Leashable.getLeashHolder()` whose UUID matches the commander. Delegate the final boolean result to `matchesRelationship`.

- [ ] **Step 5: Implement `RallyCombatPolicy` minimally**

Apply this precedence: no live target → `FORM_UP`; protected target → `DISENGAGE_AND_FORM_UP`; retreat enabled and squared distance strictly greater than the squared configured distance → `DISENGAGE_AND_FORM_UP`; otherwise → `CONTINUE_COMBAT`.

- [ ] **Step 6: Run focused and full tests**

```powershell
.\gradlew.bat test --tests 'net.hfstack.rallyguard.combat.*PolicyTest'
.\gradlew.bat test
```

Expected: both commands report `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit the pure policies**

```powershell
git add -- 'src/main/java/net/hfstack/rallyguard/combat' 'src/test/java/net/hfstack/rallyguard/combat'
git commit -m 'feat(combat): define ally and retreat policies'
```

### Task 2: Enforce Ally Safety on `main`

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/combat/GuardCombatState.java`
- Create: `src/main/java/net/hfstack/rallyguard/event/GuardAllySafetyHandler.java`
- Create: `src/test/java/net/hfstack/rallyguard/InitializerRegistrationTest.java`
- Modify: `src/main/java/net/hfstack/rallyguard/network/GuardCommandNetworking.java:145-149,211-268`
- Modify: `src/main/java/net/hfstack/rallyguard/RallyOfTheGuard.java:35-42`

**Interfaces:**
- Consumes: `ProtectedAllyPolicy.isProtectedAlly(UUID, Entity)` from Task 1.
- Produces: `GuardCombatState.disengage(GuardEntity guard) -> void`, which clears target and aggression, stops active item use, and stops navigation.
- Produces: `GuardAllySafetyHandler.register() -> void`, registering loaded-guard target correction and damage cancellation exactly once during mod initialization.

- [ ] **Step 1: Add a failing registration/source-schema test**

Create `InitializerRegistrationTest` with a test named
`initializerRegistersGuardAllySafetyHandler`. Read
`src/main/java/net/hfstack/rallyguard/RallyOfTheGuard.java` with
`Files.readString` and assert that it contains
`GuardAllySafetyHandler.register();`.

- [ ] **Step 2: Run the focused test and verify it fails**

```powershell
.\gradlew.bat test --tests 'net.hfstack.rallyguard.InitializerRegistrationTest.initializerRegistersGuardAllySafetyHandler'
```

Expected: FAIL because the registration line is absent.

- [ ] **Step 3: Implement shared disengagement**

In `GuardCombatState.disengage`, call the Minecraft 26.1/Guard Villagers operations that correspond to `setTarget(null)`, `setAggressive(false)`, `stopUsingItem()`, and `getNavigation().stop()`. Replace `GuardCommandNetworking.stopGuardActions` and its callers with this shared method; remove the private duplicate.

- [ ] **Step 4: Implement native-target correction**

Register an `END_LEVEL_TICK` callback in `GuardAllySafetyHandler`, throttled to the same two-tick cadence as formation processing. Iterate loaded Guard Villagers guards that have a Rally owner and a live target; call `GuardCombatState.disengage` only when `ProtectedAllyPolicy.isProtectedAlly(guardOwner, target)` is true. Do not require `GuardOrders.isRallied` here.

- [ ] **Step 5: Implement damage cancellation**

Register `ServerLivingEntityEvents.ALLOW_DAMAGE`. Resolve the responsible attacker from `DamageSource.getEntity()` first and `getDirectEntity()` only as fallback. If the resolved entity is an owned Guard Villagers guard and the victim is protected for that guard's owner, return `false`; otherwise return `true`. Do not consult `rallyRetreatEnabled` or `rallyProtectRalliedGuardsFromOwner`.

- [ ] **Step 6: Reject protected manual attack orders**

In `handleAttackTarget`, replace the current commander/self-owned-guard check with `ProtectedAllyPolicy.isProtectedAlly(player.getUUID(), target)`. Preserve the existing invalid-target feedback and perform this check before passive/player configuration checks.

- [ ] **Step 7: Register the safety handler**

Import `GuardAllySafetyHandler` in `RallyOfTheGuard` and call `GuardAllySafetyHandler.register()` beside the existing friendly-fire and neutrality handlers.

- [ ] **Step 8: Run focused tests, full tests, and compilation**

```powershell
.\gradlew.bat test --tests 'net.hfstack.rallyguard.InitializerRegistrationTest.initializerRegistersGuardAllySafetyHandler'
.\gradlew.bat test
.\gradlew.bat compileJava
```

Expected: all commands report `BUILD SUCCESSFUL`. Inspect the handler to confirm environmental damage and damage from guards owned by another commander return `true`.

- [ ] **Step 9: Perform the focused main-version smoke test**

Launch the development client. Verify a hired melee guard and hired archer cannot damage the commander's wolf after retaliation is provoked, covering melee and projectile damage-source shapes. Verify a non-owned hostile can still damage the wolf and guard.

- [ ] **Step 10: Commit ally enforcement**

```powershell
git add -- 'src/main/java/net/hfstack/rallyguard/combat/GuardCombatState.java' 'src/main/java/net/hfstack/rallyguard/event/GuardAllySafetyHandler.java' 'src/main/java/net/hfstack/rallyguard/network/GuardCommandNetworking.java' 'src/main/java/net/hfstack/rallyguard/RallyOfTheGuard.java' 'src/test/java/net/hfstack/rallyguard/InitializerRegistrationTest.java'
git commit -m 'fix(combat): protect commander-owned companions'
```

### Task 3: Add Automatic Rally Retreat on `main`

**Files:**
- Modify: `src/main/java/net/hfstack/rallyguard/config/RallyConfig.java:21-41,104-129`
- Modify: `src/main/java/net/hfstack/rallyguard/event/RallyFormationTicker.java:79-124,160-170`
- Modify: `src/main/resources/assets/rallyguard/lang/en_us.json:24-44`
- Modify: `src/main/resources/assets/rallyguard/lang/pt_br.json:24-44`
- Create: `src/test/java/net/hfstack/rallyguard/config/RallyConfigTest.java`

**Interfaces:**
- Consumes: `RallyCombatPolicy`, `ProtectedAllyPolicy`, and `GuardCombatState` from Tasks 1-2.
- Produces: `RallyConfig.rallyRetreatEnabled() -> boolean`.
- Produces: `RallyConfig.rallyRetreatDistance() -> double`, clamped to `[1.0, 512.0]` with default `24.0`.

- [ ] **Step 1: Write the failing retreat-configuration test**

Create `RallyConfigTest` with
`retreatDefaultsAndDistanceClampsToSupportedRange`. Assert the initial enabled
flag is `true` and initial distance accessor returns `24.0`. In a `try/finally`
that restores both public static fields, set the raw distance to `0.0` and
assert the accessor returns `1.0`, then set it to `999.0` and assert `512.0`.

- [ ] **Step 2: Run the focused test and verify the expected red state**

```powershell
.\gradlew.bat test --tests 'net.hfstack.rallyguard.config.RallyConfigTest'
```

Expected: compilation fails because the retreat fields and accessors do not
exist.

- [ ] **Step 3: Add configuration fields and accessors**

Add MidnightLib entries in category `rally`:

```java
public static boolean rallyRetreatEnabled = true;
public static double rallyRetreatDistance = 24.0;
```

Annotate the distance with `min = 1.0, max = 512.0`. Add accessors with the exact names in the Interfaces block and clamp the distance to the same range.

- [ ] **Step 4: Replace unconditional combat priority in the formation ticker**

For each rallied guard, derive live target, protected-target status, and squared commander distance. Call `RallyCombatPolicy.decide`. Continue immediately only for `CONTINUE_COMBAT`; call `GuardCombatState.disengage` for `DISENGAGE_AND_FORM_UP`; then calculate and apply the existing formation slot in the same tick. Remove the obsolete `isBusyFighting` helper.

- [ ] **Step 5: Preserve recovery for all loaded rallied guards**

Remove the `combatGuardSearchRadius` distance predicate from `ralliedGuards`. Keep ownership, rallied, patrol, wait, route, and same-world loaded-entity constraints unchanged. This prevents a configured retreat distance or pathfinding delay from making a loaded rallied guard disappear from formation processing.

- [ ] **Step 6: Add localized configuration copy**

Add keys for `rallyRetreatEnabled`, its tooltip, `rallyRetreatDistance`, and its tooltip to both language files. State defaults (`true`, `24.0`) and range (`1.0`–`512.0`); explain that guards abandon combat and return when farther than this distance.

- [ ] **Step 7: Run tests, resource validation, and build**

```powershell
.\gradlew.bat test --tests 'net.hfstack.rallyguard.combat.RallyCombatPolicyTest'
.\gradlew.bat test --tests 'net.hfstack.rallyguard.config.RallyConfigTest'
.\gradlew.bat test --tests 'net.hfstack.rallyguard.ResourceSchemaTest'
.\gradlew.bat build
```

Expected: all commands report `BUILD SUCCESSFUL` and the JAR is present under `build/libs/`.

- [ ] **Step 8: Smoke-test both retreat modes on main**

With retreat enabled, verify combat continues at 24 blocks and disengages beyond 24 blocks; verify navigation resumes and the existing 30-block formation teleport still controls teleporting. Disable retreat and verify a valid hostile target keeps the old fight-until-combat-ends behavior while a protected ally still forces disengagement.

- [ ] **Step 9: Commit retreat behavior**

```powershell
git add -- 'src/main/java/net/hfstack/rallyguard/config/RallyConfig.java' 'src/main/java/net/hfstack/rallyguard/event/RallyFormationTicker.java' 'src/main/resources/assets/rallyguard/lang/en_us.json' 'src/main/resources/assets/rallyguard/lang/pt_br.json' 'src/test/java/net/hfstack/rallyguard/config/RallyConfigTest.java'
git commit -m 'feat(rally): retreat when commander leaves combat'
```

### Task 4: Document and Verify the Canonical Implementation

**Files:**
- Modify: `README.md`
- Modify: `CHANGELOG.md`

**Interfaces:**
- Consumes: completed main implementation from Tasks 1-3.
- Produces: user-facing behavior/configuration documentation and a clean, buildable canonical commit range for ports.

- [ ] **Step 1: Update README behavior and configuration**

Document that hired guards never attack companions owned by the same commander, that rallied guards retreat beyond the configured distance, and that `rally.retreatEnabled`/`rally.retreatDistance` are conceptual names backed by MidnightLib fields `rallyRetreatEnabled`/`rallyRetreatDistance`.

- [ ] **Step 2: Add an unreleased changelog entry**

At the top of `CHANGELOG.md`, add an `Unreleased` section covering ally protection, automatic retreat defaulting to 24 blocks, and availability of a fight-until-combat-ends configuration mode. Do not choose or bump a release version in this plan.

- [ ] **Step 3: Run final canonical verification**

```powershell
.\gradlew.bat clean test build
git diff --check
git status --short
```

Expected: `BUILD SUCCESSFUL`, no whitespace errors, and only the intended README/changelog changes remain uncommitted.

- [ ] **Step 4: Commit documentation**

```powershell
git add -- 'README.md' 'CHANGELOG.md'
git commit -m 'docs: explain ally safety and rally retreat'
```

- [ ] **Step 5: Record the portable commit range**

Run `git log -4 --reverse --format='%H %s'`. Verify the output contains,
in order, exactly the policy, ally-enforcement, retreat, and documentation
commits from Tasks 1-4. Record those four full hashes; exclude the earlier
design/plan documentation commits from release-branch cherry-picks.

### Task 5: Port the Feature to `26.1`

**Files:**
- Port the exact production, test, resource, README, and changelog files from Tasks 1-4 into the `26.1` worktree.

**Interfaces:**
- Consumes: ordered portable commit hashes recorded in Task 4.
- Produces: behavior-equivalent Minecraft 26.1 release branch with identical tests and Mojang-mapped source.

- [ ] **Step 1: Confirm the release worktree is clean and current**

Run `git status --short --branch` and verify it names `26.1` with no local changes. Do not pull or rebase unless the user explicitly requests remote synchronization.

- [ ] **Step 2: Cherry-pick each feature commit in order**

Run `git cherry-pick` with the four full hashes recorded in Task 4, in this
exact order: policy, ally enforcement, retreat, documentation.

Expected: clean cherry-picks because `main` and `26.1` currently share the same 26.1 production source. Resolve only documented divergence; never take an entire file from another version blindly.

- [ ] **Step 3: Run release-branch verification**

```powershell
.\gradlew.bat clean test build
git diff --check
git status --short --branch
```

Expected: `BUILD SUCCESSFUL`, a clean worktree, and branch `26.1` ahead only by the cherry-picked feature commits.

- [ ] **Step 4: Run the 26.1 gameplay subset**

Verify wolf protection, melee and archer/projectile protection, protected manual-order rejection, 24-block retreat boundary, navigation return, corrective teleport, retreat disabled, and a ridden or directly leashed Happy Ghast. Confirm a merely harnessed/unridden/unleashed Happy Ghast is not claimed as owned.

### Task 6: Adapt and Port the Feature to `1.21.11`

**Files:**
- Port all files from Tasks 1-4 into the `1.21.11` worktree, adapting imports and mapped calls only.

**Interfaces:**
- Consumes: the behavior and tests from the canonical implementation.
- Produces: the same public Rally policy on Java 21/Yarn 1.21.11.

- [ ] **Step 1: Confirm the release worktree is clean and current**

Run `git status --short --branch`; require branch `1.21.11` and no local changes.

- [ ] **Step 2: Cherry-pick the policy commit and adapt Yarn ownership APIs**

Use `net.minecraft.entity.Entity`, `Tameable`, and `Leashable`. Extract persistent owner UUID from `Tameable.getOwnerReference().getUuid()` when present. Use `Entity.getUuid()`, passengers, and `Leashable.getLeashHolder()` for transient relationships. Preserve the exact pure method signatures and JUnit assertions except for mapped entity imports.

Run:

```powershell
.\gradlew.bat test --tests 'net.hfstack.rallyguard.combat.*PolicyTest'
```

Expected: `BUILD SUCCESSFUL`; then continue the cherry-pick if it was paused for conflict resolution.

- [ ] **Step 3: Cherry-pick ally enforcement and adapt Yarn combat APIs**

Use `setAttacking(false)`, `clearActiveItem()`, and `getNavigation().stop()` in `GuardCombatState`. Use `ServerTickEvents.END_WORLD_TICK`, `DamageSource.getAttacker()` as the causing entity, and `getSource()` as fallback. Preserve server-side attack-order validation and `GuardAllySafetyHandler.register()`.

Run `.\gradlew.bat test` and expect `BUILD SUCCESSFUL` before continuing.

- [ ] **Step 4: Cherry-pick retreat/configuration and adapt Yarn movement APIs**

Use `squaredDistanceTo`, `startMovingTo`, `setVelocity`, `clearActiveItem`, `getUuid`, and existing 1.21.11 world/entity query methods. Remove only the old combat-search-radius predicate from rallied formation selection; preserve every other predicate.

Run `.\gradlew.bat build` and expect `BUILD SUCCESSFUL` before continuing.

- [ ] **Step 5: Cherry-pick documentation and verify the complete branch**

```powershell
.\gradlew.bat clean test build
git diff --check
git status --short --branch
```

Expected: clean worktree and successful Java 21 build.

- [ ] **Step 6: Run the 1.21.11 gameplay subset**

Repeat the Task 5 gameplay subset, including ridden/directly leashed Happy Ghast coverage because Happy Ghasts exist in this version.

### Task 7: Adapt and Port the Feature to `1.21.1`

**Files:**
- Port all files from Tasks 1-4 into the `1.21.1` worktree, adapting imports and mapped calls only.

**Interfaces:**
- Consumes: the behavior and tests from the canonical implementation.
- Produces: the same public Rally policy on Java 21/Yarn 1.21.1, excluding Happy Ghast gameplay because that entity is absent.

- [ ] **Step 1: Confirm the release worktree is clean and current**

Run `git status --short --branch`; require branch `1.21.1` and no local changes.

- [ ] **Step 2: Cherry-pick the policy commit and adapt 1.21.1 ownership APIs**

Use `net.minecraft.entity.Tameable.getOwnerUuid()` for persistent animal/mount ownership and `Leashable.getLeashHolder()` for direct leash association. Use `Entity.getUuid()` and passengers for commander/vehicle association. Keep null owner UUID as unowned.

Run the two policy test classes and expect `BUILD SUCCESSFUL` before continuing.

- [ ] **Step 3: Cherry-pick ally enforcement and retreat commits**

Use the existing 1.21.1 Yarn equivalents already present in the branch: `setAttacking(false)`, `clearActiveItem()`, `startMovingTo`, `squaredDistanceTo`, `ServerTickEvents.END_WORLD_TICK`, `DamageSource.getAttacker()`, and `getSource()`. Preserve all behavior and defaults from `main`; omit only Happy Ghast-specific manual coverage, not the generic rider/leash policy.

- [ ] **Step 4: Cherry-pick documentation and verify the complete branch**

```powershell
.\gradlew.bat clean test build
git diff --check
git status --short --branch
```

Expected: clean worktree and successful Java 21 build.

- [ ] **Step 5: Run the 1.21.1 gameplay subset**

Verify wolf protection, owned horse/donkey protection, melee and archer/projectile protection, protected manual-order rejection, retreat boundary, navigation, corrective teleport, and retreat disabled. Record Happy Ghast as not applicable for this version.

### Task 8: Complete Cross-Version Acceptance Verification

**Files:**
- Create on `main`: `docs/testing/2026-10-05-allied-protection-and-rally-retreat-manual.md`

**Interfaces:**
- Consumes: successful builds and gameplay results from Tasks 4-7.
- Produces: one auditable acceptance matrix with commit hashes, build results, gameplay results, and any version-specific not-applicable cells.

- [ ] **Step 1: Write the verification matrix before filling results**

Create rows for every manual case in the spec and columns for `main/26.1`, `26.1`, `1.21.11`, and `1.21.1`. Include branch commit hash, Java version, Gradle command, and generated JAR name above the matrix.

- [ ] **Step 2: Record build evidence**

For every worktree, rerun `.\gradlew.bat clean test build` if the prior
successful output is no longer available. Record PASS only from a zero exit
code and `BUILD SUCCESSFUL`; do not infer one branch from another.

- [ ] **Step 3: Record gameplay evidence**

Fill every matrix cell from direct observation. Use `N/A — entity absent` only for Happy Ghast on 1.21.1. Any failure reopens the owning implementation task and must be fixed in `main` first when the bug is shared, then re-ported.

- [ ] **Step 4: Verify repository state across worktrees**

Run `git status --short --branch` in all four worktrees. Expected: each worktree is clean; no feature fix exists only on an older branch; all intended commits are present in branch order.

- [ ] **Step 5: Commit the acceptance record on main**

```powershell
git add -- 'docs/testing/2026-10-05-allied-protection-and-rally-retreat-manual.md'
git commit -m 'test: record ally safety and retreat verification'
```

- [ ] **Step 6: Final review gate**

Review the combined `main` diff from the Task 0 baseline and the equivalent diffs on each release branch. Confirm the acceptance criteria in the spec are all represented by code, automated tests, or a recorded manual result before declaring the feature complete.
