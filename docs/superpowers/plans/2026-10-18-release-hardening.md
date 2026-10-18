# Rally of the Guard 1.4.0 Release Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Harden route updates and Rally lifecycle behavior, reconcile release metadata and documentation, and leave 1.4.0 ready for human smoke testing.

**Architecture:** Keep the current client flows and introduce small pure policies at the server boundary. Route packets receive a fixed wire bound plus all-or-nothing semantic validation; Rally participation and cleanup become explicit policies coordinated by one lifecycle service. Documentation records automated evidence without claiming manual outcomes.

**Tech Stack:** Java 25, Fabric Loader/API for Minecraft 26.1, Fabric networking and lifecycle events, JUnit 5, Fabric server/client GameTest, Gradle.

**Spec:** `docs/superpowers/specs/2026-10-18-release-hardening-design.md`

## Global Constraints

- Target Rally of the Guard `1.4.0`, Minecraft `26.1`, Fabric Loader `0.18.4`, and Java `25`.
- Do not add post-1.4.0 gameplay, dependencies, loaders, or Minecraft versions.
- Never force-load chunks during validation or Rally cleanup.
- Preserve existing human-entered smoke-test checks; do not approve manual observations from automated evidence.
- Use test-first red-green-refactor for every production behavior change.
- Preserve the user's existing uncommitted edits in `docs/testing/26.1-1.4.0-remaining-smoke-test.md`.

## Review Focus

- A route packet declaring `Integer.MAX_VALUE` points must fail before collection allocation.
- A mixed route draft containing one stored point and one distant forged point must reject the whole request without mutation.
- A fixed-patrol guard defending a territory may join Rally, but an ordinary fixed-patrol guard may not.
- Losing the Rally effect while the commander remains online must release participants exactly once without touching independent followers.
- Disconnect cleanup must scan only already-loaded guards across dimensions and retain no unbounded commander state.

---

### Task 1: Bound the individual-route wire contract

**Files:**
- Modify: `src/main/java/net/hfstack/rallyguard/network/payload/GuardRouteUpdateC2SPayload.java`
- Create: `src/test/java/net/hfstack/rallyguard/network/GuardRouteUpdatePayloadTest.java`

**Interfaces:**
- Consumes: `RegistryFriendlyByteBuf`, the existing payload fields and point codec.
- Produces: `GuardRouteUpdateC2SPayload.MAX_POINTS = 10` and a decoder that rejects counts outside `0..10` before allocation.

- [ ] **Step 1: Write failing payload tests**

Add tests named `roundTripPreservesBoundedRoute`, `negativePointCountIsRejectedBeforeReadingPoints`, and `excessivePointCountIsRejectedBeforeAllocation`. Construct raw buffers and assert `IllegalArgumentException` for `-1`, `11`, and `Integer.MAX_VALUE`.

- [ ] **Step 2: Run the focused test and verify RED**

Run: `./gradlew.bat test --tests '*GuardRouteUpdatePayloadTest'`  
Expected: the bounded round trip passes and at least the excessive-count cases fail against the current decoder.

- [ ] **Step 3: Implement the fixed wire bound**

Add `public static final int MAX_POINTS = 10`; validate the decoded size before `new ArrayList<>(size)`. Keep the existing field order and label bound for protocol compatibility.

- [ ] **Step 4: Re-run focused tests and verify GREEN**

Run: `./gradlew.bat test --tests '*GuardRouteUpdatePayloadTest'`  
Expected: all payload tests pass.

- [ ] **Step 5: Commit the wire hardening**

```powershell
git add src/main/java/net/hfstack/rallyguard/network/payload/GuardRouteUpdateC2SPayload.java src/test/java/net/hfstack/rallyguard/network/GuardRouteUpdatePayloadTest.java
git commit -m "fix(network): bound individual route payloads"
```

### Task 2: Validate route mutations atomically on the server

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/network/GuardRouteUpdatePolicy.java`
- Create: `src/test/java/net/hfstack/rallyguard/network/GuardRouteUpdatePolicyTest.java`
- Modify: `src/main/java/net/hfstack/rallyguard/network/GuardCommandNetworking.java`
- Modify: `src/main/resources/assets/rallyguard/lang/en_us.json`
- Modify: `src/main/resources/assets/rallyguard/lang/pt_br.json`

**Interfaces:**
- Consumes: action ID, submitted points, stored points, player position, world min/max build height, world-border predicate, and squared interaction radius.
- Produces: `GuardRouteUpdatePolicy.validate(...) -> Validation`, where `Validation.accepted()` controls whether `handleRouteUpdate` may mutate the guard and `Validation.translationKey()` supplies rejection feedback.

- [ ] **Step 1: Write failing pure-policy tests**

Cover valid save/start/pause/clear; unknown action; start with fewer than two points; point count above ten; Y outside build height; point outside the world border; stored distant point preservation; new nearby point acceptance; new distant point rejection; and a mixed draft rejecting atomically.

- [ ] **Step 2: Run the policy test and verify RED**

Run: `./gradlew.bat test --tests '*GuardRouteUpdatePolicyTest'`  
Expected: compilation fails because `GuardRouteUpdatePolicy` does not exist.

- [ ] **Step 3: Implement `GuardRouteUpdatePolicy`**

Use an eight-block Euclidean capture radius. A submitted point is valid when it equals a stored point or is within that radius of the player; all points must also satisfy build-height and border predicates. Clear accepts only an empty list. Validation must inspect the complete request before returning success.

- [ ] **Step 4: Integrate validation before route mutation**

In `handleRouteUpdate`, construct immutable `BlockPos` and label lists only after the wire bound, call the policy using `GuardRoutes.get(guard).points()`, and return translated feedback on rejection. Do not clamp an invalid request into validity and do not partially write state.

- [ ] **Step 5: Add localized rejection feedback**

Add matching English and Brazilian Portuguese keys for invalid route requests and invalid/distant route points.

- [ ] **Step 6: Run focused and route regression tests**

Run: `./gradlew.bat test --tests '*GuardRouteUpdate*' --tests '*GuardRoutesTest' --tests '*ResourceSchemaTest'`  
Expected: all tests pass.

- [ ] **Step 7: Commit atomic route validation**

```powershell
git add src/main/java/net/hfstack/rallyguard/network/GuardRouteUpdatePolicy.java src/main/java/net/hfstack/rallyguard/network/GuardCommandNetworking.java src/main/resources/assets/rallyguard/lang/en_us.json src/main/resources/assets/rallyguard/lang/pt_br.json src/test/java/net/hfstack/rallyguard/network/GuardRouteUpdatePolicyTest.java
git commit -m "fix(routes): validate route coordinates on server"
```

### Task 3: Make Rally participation explicit

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/order/RallyParticipationPolicy.java`
- Create: `src/test/java/net/hfstack/rallyguard/order/RallyParticipationPolicyTest.java`
- Modify: `src/main/java/net/hfstack/rallyguard/item/ScrollOfRallyingItem.java`
- Modify: `src/test/java/net/hfstack/rallyguard/order/defend/DefendAreaTransitionPolicyTest.java`

**Interfaces:**
- Consumes: `boolean patrolling`, `boolean defendingArea`, `boolean waiting`, and `boolean routeActive`.
- Produces: `RallyParticipationPolicy.mayJoin(boolean, boolean, boolean, boolean) -> boolean`.

- [ ] **Step 1: Write the failing participation matrix**

Assert that idle guards may join; waiting and active-route guards never join; ordinary fixed patrol does not join; and fixed patrol originating from Defend Area remains eligible for the existing temporary replacement.

- [ ] **Step 2: Run the focused tests and verify RED**

Run: `./gradlew.bat test --tests '*RallyParticipationPolicyTest' --tests '*DefendAreaTransitionPolicyTest'`  
Expected: compilation fails because the new policy is absent.

- [ ] **Step 3: Implement and integrate the pure policy**

Make `ScrollOfRallyingItem` pass `guard.isPatrolling()`, `DefendAreas.active(guard).isPresent()`, `GuardOrders.isWaiting(guard)`, and `GuardRoutes.get(guard).active()` before evaluating extension eligibility. Keep ownership, radius, and public-policy checks unchanged.

- [ ] **Step 4: Re-run focused tests and verify GREEN**

Run: `./gradlew.bat test --tests '*RallyParticipationPolicyTest' --tests '*DefendAreaTransitionPolicyTest'`  
Expected: all participation tests pass.

- [ ] **Step 5: Commit Rally eligibility**

```powershell
git add src/main/java/net/hfstack/rallyguard/order/RallyParticipationPolicy.java src/main/java/net/hfstack/rallyguard/item/ScrollOfRallyingItem.java src/test/java/net/hfstack/rallyguard/order/RallyParticipationPolicyTest.java src/test/java/net/hfstack/rallyguard/order/defend/DefendAreaTransitionPolicyTest.java
git commit -m "fix(rally): preserve waiting and routed guards"
```

### Task 4: Centralize Rally lifecycle cleanup

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/event/RallyLifecycle.java`
- Create: `src/main/java/net/hfstack/rallyguard/event/RallyCleanupPolicy.java`
- Create: `src/test/java/net/hfstack/rallyguard/event/RallyCleanupPolicyTest.java`
- Modify: `src/main/java/net/hfstack/rallyguard/item/ScrollOfRallyingItem.java`
- Modify: `src/main/java/net/hfstack/rallyguard/event/RallyFormationTicker.java`
- Modify: `src/main/java/net/hfstack/rallyguard/RallyOfTheGuard.java`
- Modify: `src/gametest/java/net/hfstack/rallyguard/gametest/RallyGuardGameTests.java`

**Interfaces:**
- Consumes: commander UUID, optional online `ServerPlayer`, `MinecraftServer`, formation, and initiating scroll stack.
- Produces: `RallyLifecycle.start(ServerPlayer, RallyFormation, ItemStack)`, `RallyLifecycle.stop(MinecraftServer, UUID)`, `RallyLifecycle.reconcile(MinecraftServer)`, and `RallyCleanupPolicy.shouldRelease(boolean ownedByCommander, boolean rallied)`.
- Produces from ticker: `RallyFormationTicker.hasSession(UUID)` and `RallyFormationTicker.stopRally(UUID)`.

- [ ] **Step 1: Write failing cleanup-policy tests**

Assert release only for an owned Rally-tagged participant; reject unrelated owner, independent follower, and already-clean guard. Include repeated cleanup to pin idempotence.

- [ ] **Step 2: Write a failing server GameTest for participant isolation**

Create two owned guard entities: one Rally-tagged participant and one independently following guard. Invoke lifecycle cleanup and assert only the participant loses Rally/following state.

- [ ] **Step 3: Run focused tests and verify RED**

Run: `./gradlew.bat test --tests '*RallyCleanupPolicyTest'`  
Expected: compilation fails because the cleanup policy is absent.

- [ ] **Step 4: Implement the cleanup policy and lifecycle coordinator**

Track active commander UUIDs in a bounded set. `start` delegates anchor creation, applies the effect and active presentation state. `stop` removes the effect/anchor, scans loaded entities in existing server levels, releases only policy-approved participants, and clears `ACTIVE` from Rally scrolls in an online player's inventory. `reconcile` stops tracked sessions whose player is absent, dead, or lacks the effect; for online non-session players it clears stale scroll presentation.

- [ ] **Step 5: Route normal start/stop through the coordinator**

Replace the duplicated toggle code in `ScrollOfRallyingItem`; change ticker stop to accept UUID; register lifecycle reconciliation once from the common initializer. Do not force chunks or clear independent follower state.

- [ ] **Step 6: Run unit and server GameTests**

Run: `./gradlew.bat test --tests '*RallyCleanupPolicyTest' --tests '*RallyCombatPolicyTest' --tests '*GuardOrderStatusTest'`  
Then: `./gradlew.bat runGameTest`  
Expected: all unit tests and required GameTests pass.

- [ ] **Step 7: Commit lifecycle cleanup**

```powershell
git add src/main/java/net/hfstack/rallyguard/event/RallyLifecycle.java src/main/java/net/hfstack/rallyguard/event/RallyCleanupPolicy.java src/main/java/net/hfstack/rallyguard/event/RallyFormationTicker.java src/main/java/net/hfstack/rallyguard/item/ScrollOfRallyingItem.java src/main/java/net/hfstack/rallyguard/RallyOfTheGuard.java src/test/java/net/hfstack/rallyguard/event/RallyCleanupPolicyTest.java src/gametest/java/net/hfstack/rallyguard/gametest/RallyGuardGameTests.java
git commit -m "fix(rally): reconcile lifecycle and participants"
```

### Task 5: Align metadata and remove obsolete route UI paths

**Files:**
- Modify: `src/test/java/net/hfstack/rallyguard/ResourceSchemaTest.java`
- Modify: `src/main/resources/fabric.mod.json`
- Modify: `src/main/java/net/hfstack/rallyguard/network/NetworkConstants.java`
- Modify: `src/main/java/net/hfstack/rallyguard/network/GuardCommandNetworking.java`
- Modify: `src/main/resources/assets/rallyguard/lang/en_us.json`
- Modify: `src/main/resources/assets/rallyguard/lang/pt_br.json`

**Interfaces:**
- Consumes: repository `LICENSE`, metadata JSON, language JSON, and live command constants.
- Produces: CC0 metadata and no obsolete `ACTION_ROUTE_PLACEHOLDER`/`route_soon` resources.

- [ ] **Step 1: Write failing resource-contract assertions**

Assert `fabric.mod.json` uses `CC0-1.0`; the repository license begins with the CC0 legal code; neither language file contains `gui.rallyguard.command.route_soon`; and source text no longer declares `ACTION_ROUTE_PLACEHOLDER`.

- [ ] **Step 2: Run `ResourceSchemaTest` and verify RED**

Run: `./gradlew.bat test --tests '*ResourceSchemaTest'`  
Expected: license and obsolete-resource assertions fail.

- [ ] **Step 3: Correct metadata and delete the dead action path**

Change only the license field, remove the unused action constant/switch branch, and remove the two obsolete translations.

- [ ] **Step 4: Re-run resource tests and verify GREEN**

Run: `./gradlew.bat test --tests '*ResourceSchemaTest'`  
Expected: all resource assertions pass.

- [ ] **Step 5: Commit metadata cleanup**

```powershell
git add src/test/java/net/hfstack/rallyguard/ResourceSchemaTest.java src/main/resources/fabric.mod.json src/main/java/net/hfstack/rallyguard/network/NetworkConstants.java src/main/java/net/hfstack/rallyguard/network/GuardCommandNetworking.java src/main/resources/assets/rallyguard/lang/en_us.json src/main/resources/assets/rallyguard/lang/pt_br.json
git commit -m "fix(metadata): align license and remove route placeholder"
```

### Task 6: Reconcile active documentation and prepare the manual handoff

**Files:**
- Modify: `docs/MELHORIAS_TECNICAS.md`
- Modify: `ROADMAP.md`
- Modify: `CHANGELOG.md`
- Modify: `CURSEFORGE.md`
- Modify: `README.md`
- Modify carefully: `docs/testing/26.1-1.4.0-remaining-smoke-test.md`
- Modify: `docs/testing/26.1-1.4.0-smoke-test-observations.md`
- Test: `src/test/java/net/hfstack/rallyguard/ResourceSchemaTest.java`

**Interfaces:**
- Consumes: completed Tasks 1–5, current Gradle/dependency versions, and the user's existing checked smoke-test entries.
- Produces: factual 1.4.0 documentation with unresolved manual gates preserved.

- [ ] **Step 1: Add failing documentation-contract assertions**

Assert Java 25 and current dependency versions; implemented status prose for Command Post, Global Registry, and Raid Defense; documented route/Rally hardening; CC0 agreement; no claim that manual smoke tests or observations passed; and no stale `1.21.11` media branch in CurseForge copy.

- [ ] **Step 2: Run the documentation contract and verify RED**

Run: `./gradlew.bat test --tests '*ResourceSchemaTest'`  
Expected: the new documentation assertions fail against stale prose.

- [ ] **Step 3: Update technical and release documentation**

Mark resolved technical items with evidence and leave any genuinely unresolved recommendation open. Rewrite stale “future” prose for already implemented 1.4.0 systems. Keep CHANGELOG under `Unreleased — planned 1.4.0`. Expand CurseForge features/requirements for the implemented release and point media at a stable repository branch.

- [ ] **Step 4: Preserve manual ownership in testing documents**

Merge around the user's checked entries. Do not change any `[ ]` to `[x]`, any observation to `RESOLVIDO`, or the overall result to `APROVADO`. Add only notes distinguishing newly automated protection from still-required in-game verification.

- [ ] **Step 5: Re-run documentation/resource tests and inspect the diff**

Run: `./gradlew.bat test --tests '*ResourceSchemaTest'`  
Then: `git diff --check`  
Expected: tests pass; no whitespace errors; the user's pre-existing smoke-test approvals remain present.

- [ ] **Step 6: Commit documentation without swallowing unrelated edits**

Stage reviewed hunks explicitly. If the smoke-test file still contains inseparable user edits, leave it uncommitted and report that fact rather than committing ownership-ambiguous changes.

### Task 7: Complete automated release verification

**Files:**
- Inspect: `build/test-results/test/`
- Inspect: `build/run/clientGameTest/screenshots/`
- Inspect: `build/libs/rallyguard-fabric-26.1-1.4.0.jar`
- Modify only factual evidence fields if appropriate: `docs/testing/26.1-1.4.0-remaining-smoke-test.md`

**Interfaces:**
- Consumes: completed implementation and documentation tasks.
- Produces: verified automated evidence and a precise manual-test handoff.

- [ ] **Step 1: Run the clean automated smoke task**

Run: `./gradlew.bat clean automatedSmokeTest`  
Expected: `BUILD SUCCESSFUL`, zero JUnit failures, all required server GameTests pass, and the client GameTest exits successfully with screenshots.

- [ ] **Step 2: Verify repository and artifact integrity**

Run: `git diff --check`  
Run: `Get-FileHash ./build/libs/rallyguard-fabric-26.1-1.4.0.jar -Algorithm SHA256`  
Run: `git status --short`  
Expected: no whitespace errors; one candidate JAR/hash; only intentional or preserved user changes remain.

- [ ] **Step 3: Review warnings and generated evidence**

Record the Java 25/Mixin compatibility warning separately if it remains. Confirm screenshots exist and no raw translation keys or out-of-bounds widgets appear in automated client evidence.

- [ ] **Step 4: Run the completion review**

Compare every spec success criterion and Review Focus item with its test/evidence. Do not mark any manual smoke result. Report remaining sections by exact open-check count.

- [ ] **Step 5: Commit only final factual evidence, if changed**

Use a documentation-only commit and exclude any pre-existing user edits that cannot be cleanly separated.
