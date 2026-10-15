# Captain Promotion & Demotion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add server-authoritative, paid promotion of veteran guards to Captain and safe demotion of non-Officer Captains through a dedicated API and the Muster Roll.

**Architecture:** A dedicated `CaptainService` owns validation and mutation while `GuardProfile` remains the persisted source of truth. Pure policy and payment helpers isolate testable decisions; Muster Roll sessions provide the only client mutation path and always revalidate on the server.

**Tech Stack:** Java 25, Minecraft 26.1, Fabric API, MidnightLib configuration, JUnit 5, Gradle 9.5.1.

**Spec:** `docs/superpowers/specs/2026-10-15-captain-promotion-demotion-design.md`

## Global Constraints

- Keep mod version `1.4.0`; do not create a `1.5.0` release section.
- Keep `GuardProfile.CURRENT_SCHEMA_VERSION` at `2`; the Captain field already exists.
- Raise only the public API contract from `4` to `5`.
- Default promotion requirements are enabled, Level 7, and 4 units of `economy.hireItem`.
- Promotion never changes XP, rank, attributes, identity, Squad membership, or Officer assignment.
- Demotion never refunds payment and must reject an active Officer.
- Existing worlds and profiles must not be rerolled or retroactively promoted.
- All mutations and inventory changes run on the logical server thread.
- Client payloads never carry trusted ownership, level, price, or eligibility data.
- Broader GUI redesign is excluded; implement only the functional Muster Roll action.

## Review Focus

- A duplicated promotion packet must charge once; the second request returns `ALREADY_CAPTAIN` without touching inventory (Task 4 test).
- A future unsupported profile schema must return `UNSUPPORTED_PROFILE` and remain byte-for-byte untouched (Task 4 test).
- Payment split across inventory stacks must be counted and removed exactly, including zero-cost promotion (Task 3 tests).
- Changing configuration or inventory after a snapshot must be handled by fresh server validation, not stale client state (Task 6 test).
- Disabling promotion must not block demotion, but an active Officer must still be protected (Task 2 and Task 4 tests).

---

### Task 1: Captain API and profile copy operation

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/api/captain/CaptainAction.java`
- Create: `src/main/java/net/hfstack/rallyguard/api/captain/CaptainOffer.java`
- Create: `src/main/java/net/hfstack/rallyguard/api/captain/CaptainActionView.java`
- Create: `src/main/java/net/hfstack/rallyguard/api/captain/CaptainMutationResult.java`
- Create: `src/main/java/net/hfstack/rallyguard/api/captain/CaptainService.java`
- Modify: `src/main/java/net/hfstack/rallyguard/profile/GuardProfile.java`
- Test: `src/test/java/net/hfstack/rallyguard/api/captain/CaptainApiContractTest.java`
- Test: `src/test/java/net/hfstack/rallyguard/profile/GuardProfileTest.java`

**Interfaces:**
- Produces: `CaptainAction { NONE, PROMOTE, DEMOTE }`.
- Produces: `CaptainOffer(Identifier paymentItemId, int cost)` with non-negative cost.
- Produces: `CaptainActionView(boolean captain, CaptainAction action, boolean allowed, Optional<CaptainMutationResult.Outcome> blockingOutcome, int minimumLevel, Optional<CaptainOffer> offer)`.
- Produces: `CaptainMutationResult promote(ServerPlayer, GuardEntity)`, `demote(...)`, and `inspect(...)` on `CaptainService`.
- Produces: `GuardProfile.withCaptain(boolean captain)` preserving all other fields.

- [ ] **Step 1: Write the failing API and profile tests**

Add tests asserting the exact public method signatures, `CaptainOffer` validation, result success semantics, optional action-view fields, and that `withCaptain(true/false)` changes only the Captain flag across a nontrivial profile.

- [ ] **Step 2: Run the focused tests and verify RED**

Run: `.\gradlew.bat test --tests '*CaptainApiContractTest' --tests '*GuardProfileTest'`

Expected: FAIL because the Captain API types and `withCaptain` do not exist.

- [ ] **Step 3: Implement the API value types, service interface, and copy method**

Use the spec's complete outcome set: `SUCCESS`, `DISABLED`, `NOT_AVAILABLE`, `NOT_OWNER`, `UNSUPPORTED_PROFILE`, `ALREADY_CAPTAIN`, `NOT_CAPTAIN`, `LEVEL_TOO_LOW`, `INSUFFICIENT_PAYMENT`, `ACTIVE_OFFICER`, `PERSISTENCE_ERROR`, and `DENIED`. Reject construction of a failed result with `SUCCESS` and a successful result without its applied offer when the action is promotion.

- [ ] **Step 4: Run the focused tests and verify GREEN**

Run: `.\gradlew.bat test --tests '*CaptainApiContractTest' --tests '*GuardProfileTest'`

Expected: PASS.

- [ ] **Step 5: Commit the API foundation**

```powershell
git add -- src/main/java/net/hfstack/rallyguard/api/captain src/main/java/net/hfstack/rallyguard/profile/GuardProfile.java src/test/java/net/hfstack/rallyguard/api/captain src/test/java/net/hfstack/rallyguard/profile/GuardProfileTest.java
git commit -m "feat(captain): add promotion API contract"
```

### Task 2: Configuration and pure eligibility policy

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/captain/CaptainEligibilityPolicy.java`
- Modify: `src/main/java/net/hfstack/rallyguard/config/RallyConfig.java`
- Test: `src/test/java/net/hfstack/rallyguard/captain/CaptainEligibilityPolicyTest.java`
- Test: `src/test/java/net/hfstack/rallyguard/config/RallyConfigTest.java`

**Interfaces:**
- Consumes: `CaptainAction`, `CaptainMutationResult.Outcome` from Task 1.
- Produces: `CaptainEligibilityPolicy.evaluate(CaptainAction action, boolean promotionEnabled, boolean available, boolean owner, boolean writableProfile, boolean captain, int level, int minimumLevel, boolean officer)` returning the first blocking outcome or `SUCCESS`.
- Produces: `RallyConfig.captainPromotionEnabled()`, `captainPromotionMinimumLevel()`, and `captainPromotionCost()`.

- [ ] **Step 1: Write failing configuration and policy tests**

Cover defaults `true`, `7`, and `4`; clamp level to `1..10` and cost to `0..999`; verify promotion validation order; verify demotion remains available when promotion is disabled; verify Officer demotion returns `ACTIVE_OFFICER`.

- [ ] **Step 2: Run the focused tests and verify RED**

Run: `.\gradlew.bat test --tests '*CaptainEligibilityPolicyTest' --tests '*RallyConfigTest'`

Expected: FAIL because settings and policy are missing.

- [ ] **Step 3: Implement config entries, accessors, and the pure policy**

Add MidnightConfig fields under the existing `progression` and `economy` categories. Keep validation order deterministic so callers receive the most actionable outcome and ensure `DEMOTE` ignores only the promotion-enabled and minimum-level gates.

- [ ] **Step 4: Run the focused tests and verify GREEN**

Run: `.\gradlew.bat test --tests '*CaptainEligibilityPolicyTest' --tests '*RallyConfigTest'`

Expected: PASS.

- [ ] **Step 5: Commit configuration and eligibility**

```powershell
git add -- src/main/java/net/hfstack/rallyguard/captain/CaptainEligibilityPolicy.java src/main/java/net/hfstack/rallyguard/config/RallyConfig.java src/test/java/net/hfstack/rallyguard/captain/CaptainEligibilityPolicyTest.java src/test/java/net/hfstack/rallyguard/config/RallyConfigTest.java
git commit -m "feat(captain): define promotion eligibility"
```

### Task 3: Reusable inventory payment operations

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/economy/InventoryPayment.java`
- Modify: `src/main/java/net/hfstack/rallyguard/service/DefaultGuardRecruitmentService.java`
- Test: `src/test/java/net/hfstack/rallyguard/economy/InventoryPaymentTest.java`

**Interfaces:**
- Produces: `InventoryPayment.count(Container inventory, Item item)`, `remove(Container inventory, Item item, int amount)`, and `refund(Inventory inventory, Item item, int amount)`.
- Preserves: recruitment payment behavior while removing its private duplicate count/remove/refund methods.

- [ ] **Step 1: Write failing payment tests**

Using `SimpleContainer`, assert exact counting/removal across multiple stacks, unrelated items remain unchanged, oversized removal never underflows, and zero/negative requested amounts are no-ops.

- [ ] **Step 2: Run the focused test and verify RED**

Run: `.\gradlew.bat test --tests '*InventoryPaymentTest'`

Expected: FAIL because `InventoryPayment` does not exist.

- [ ] **Step 3: Implement the helper and migrate recruitment**

Accept `Container` for count/remove so behavior is unit-testable; keep refund on `Inventory.placeItemBackInInventory`. Clamp all requested amounts to non-negative values. Replace recruitment's private helpers without changing its transaction order.

- [ ] **Step 4: Run payment, recruitment, and full compile tests**

Run: `.\gradlew.bat test --tests '*InventoryPaymentTest' --tests '*GuardRecruitmentEventsTest' --tests '*RecruitmentDecisionTest'`

Expected: PASS.

- [ ] **Step 5: Commit the shared economy helper**

```powershell
git add -- src/main/java/net/hfstack/rallyguard/economy/InventoryPayment.java src/main/java/net/hfstack/rallyguard/service/DefaultGuardRecruitmentService.java src/test/java/net/hfstack/rallyguard/economy/InventoryPaymentTest.java
git commit -m "refactor(economy): share inventory payment operations"
```

### Task 4: Captain events and server service

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/api/captain/CaptainContext.java`
- Create: `src/main/java/net/hfstack/rallyguard/api/captain/CaptainDecision.java`
- Create: `src/main/java/net/hfstack/rallyguard/api/captain/CaptainEvents.java`
- Create: `src/main/java/net/hfstack/rallyguard/captain/DefaultCaptainService.java`
- Create: `src/main/java/net/hfstack/rallyguard/captain/CaptainMutationCoordinator.java`
- Modify: `src/main/java/net/hfstack/rallyguard/api/RallyGuardApi.java`
- Test: `src/test/java/net/hfstack/rallyguard/api/captain/CaptainEventsTest.java`
- Test: `src/test/java/net/hfstack/rallyguard/captain/CaptainMutationCoordinatorTest.java`
- Test: `src/test/java/net/hfstack/rallyguard/api/profile/GuardProfileAccessTest.java`

**Interfaces:**
- Consumes: Tasks 1-3 contracts and `RallyGuardApi.squads().find(owner, guard).officer()`.
- Produces: ordered `CaptainEvents.BEFORE` denial policy and isolated `CaptainEvents.AFTER` notification.
- Produces: `DefaultCaptainService` as `RallyGuardApi.captains()` and `RallyGuardApi.API_VERSION = 5`.
- Produces: package-private `CaptainMutationCoordinator` transaction seam for deterministic persistence/payment tests without constructing a live server.
- Produces: `CaptainMutationCoordinator.execute(Request request, PaymentPort payment, ProfileWriter profiles)`, where `Request` contains action, promotion-enabled state, availability, ownership, `GuardProfileReadResult`, minimum level, Officer state, and optional offer; `PaymentPort` exposes `canAfford`, `remove`, and `refund`, while `ProfileWriter` exposes `save(GuardProfile)`.

- [ ] **Step 1: Write failing event and coordinator tests**

Assert ordered first-denial behavior, AFTER ordering, exception isolation contract, exact validation-before-payment ordering, refund on save failure, no refund on successful demotion, repeat promotion returning `ALREADY_CAPTAIN`, unsupported profile preservation, and active-Officer rejection while promotion-disabled demotion remains possible.

- [ ] **Step 2: Run focused tests and verify RED**

Run: `.\gradlew.bat test --tests '*CaptainEventsTest' --tests '*CaptainMutationCoordinatorTest' --tests '*GuardProfileAccessTest'`

Expected: FAIL because events, coordinator, and API v5 are missing.

- [ ] **Step 3: Implement events, coordinator, runtime service, and API registration**

The coordinator receives focused collaborators for payment and profile persistence; it must not expose repository mutation publicly. `DefaultCaptainService.inspect` derives a current offer from `RallyConfig.hireItem()` and returns structural eligibility without trusting inventory as a permanent promise. Runtime `promote` revalidates after BEFORE and immediately before removing payment.

- [ ] **Step 4: Run focused tests and verify GREEN**

Run: `.\gradlew.bat test --tests '*CaptainEventsTest' --tests '*CaptainMutationCoordinatorTest' --tests '*GuardProfileAccessTest'`

Expected: PASS with API version 5.

- [ ] **Step 5: Commit the Captain domain service**

```powershell
git add -- src/main/java/net/hfstack/rallyguard/api/captain src/main/java/net/hfstack/rallyguard/captain src/main/java/net/hfstack/rallyguard/api/RallyGuardApi.java src/test/java/net/hfstack/rallyguard/api/captain src/test/java/net/hfstack/rallyguard/captain src/test/java/net/hfstack/rallyguard/api/profile/GuardProfileAccessTest.java
git commit -m "feat(captain): implement promotion service"
```

### Task 5: Muster snapshot and payload contracts

**Files:**
- Modify: `src/main/java/net/hfstack/rallyguard/muster/GuardProfileSnapshot.java`
- Modify: `src/main/java/net/hfstack/rallyguard/muster/GuardProfileSnapshotFactory.java`
- Modify: `src/main/java/net/hfstack/rallyguard/network/payload/MusterRollSnapshotS2CPayload.java`
- Create: `src/main/java/net/hfstack/rallyguard/network/payload/CaptainActionC2SPayload.java`
- Modify: `src/main/java/net/hfstack/rallyguard/network/NetworkBootstrap.java`
- Test: `src/test/java/net/hfstack/rallyguard/muster/GuardProfileSnapshotTest.java`
- Create: `src/test/java/net/hfstack/rallyguard/network/CaptainPayloadContractTest.java`

**Interfaces:**
- Consumes: `CaptainActionView` and `CaptainAction` from Tasks 1 and 4.
- Produces: optional Captain action data in `GuardProfileSnapshot` and its bounded stream codec.
- Produces: action-only `CaptainActionC2SPayload(CaptainAction action)` accepting only `PROMOTE` or `DEMOTE`.

- [ ] **Step 1: Write failing snapshot and codec tests**

Assert owned guards receive server action state, unowned/other-owned guards do not, snapshot normalization is bounded, both actions round-trip, and an unknown action ID decodes safely to `NONE` for server rejection.

- [ ] **Step 2: Run focused tests and verify RED**

Run: `.\gradlew.bat test --tests '*GuardProfileSnapshotTest' --tests '*CaptainPayloadContractTest'`

Expected: FAIL because snapshot fields and payload are missing.

- [ ] **Step 3: Implement snapshot derivation and codecs**

Call `RallyGuardApi.captains().inspect(viewer, guard)` only for `UNDER_YOUR_COMMAND`. Encode enums with explicit bounded IDs rather than ordinal trust; malformed IDs decode to `NONE`, which the session handler rejects without mutating anything.

- [ ] **Step 4: Run focused tests and verify GREEN**

Run: `.\gradlew.bat test --tests '*GuardProfileSnapshotTest' --tests '*CaptainPayloadContractTest'`

Expected: PASS.

- [ ] **Step 5: Commit snapshot and protocol support**

```powershell
git add -- src/main/java/net/hfstack/rallyguard/muster src/main/java/net/hfstack/rallyguard/network/payload src/main/java/net/hfstack/rallyguard/network/NetworkBootstrap.java src/test/java/net/hfstack/rallyguard/muster/GuardProfileSnapshotTest.java src/test/java/net/hfstack/rallyguard/network/CaptainPayloadContractTest.java
git commit -m "feat(captain): synchronize Muster action state"
```

### Task 6: Session-authorized mutation networking

**Files:**
- Modify: `src/main/java/net/hfstack/rallyguard/muster/MusterRollSessions.java`
- Modify: `src/main/java/net/hfstack/rallyguard/network/MusterRollNetworking.java`
- Create: `src/main/java/net/hfstack/rallyguard/muster/MusterCaptainActionHandler.java`
- Test: `src/test/java/net/hfstack/rallyguard/muster/MusterCaptainActionHandlerTest.java`

**Interfaces:**
- Consumes: `CaptainActionC2SPayload` and `RallyGuardApi.captains()`.
- Produces: `MusterRollSessions.guardFor(ServerPlayer)` that returns a guard only while the session, held item, range, dimension, and entity identity remain valid.
- Produces: handler feedback plus immediate snapshot refresh after every accepted mutation.

- [ ] **Step 1: Write failing authorization and stale-state tests**

Use a focused handler/session seam to assert missing session, wrong held item, out-of-range guard, stale entity, and `NONE` action never invoke the service. Assert a configuration or inventory change after the prior snapshot is observed by a fresh service call, and successful actions request immediate refresh.

- [ ] **Step 2: Run the focused test and verify RED**

Run: `.\gradlew.bat test --tests '*MusterCaptainActionHandlerTest'`

Expected: FAIL because the session action path is absent.

- [ ] **Step 3: Implement the session resolver, handler, and receiver**

The receiver schedules on the server thread, resolves only the active session guard, calls the requested service operation, sends result feedback, and refreshes or closes the session according to current validity. Do not accept an entity ID or UUID from the client.

- [ ] **Step 4: Run focused networking tests and verify GREEN**

Run: `.\gradlew.bat test --tests '*MusterCaptainActionHandlerTest' --tests '*CaptainPayloadContractTest'`

Expected: PASS.

- [ ] **Step 5: Commit secure mutation networking**

```powershell
git add -- src/main/java/net/hfstack/rallyguard/muster src/main/java/net/hfstack/rallyguard/network/MusterRollNetworking.java src/test/java/net/hfstack/rallyguard/muster/MusterCaptainActionHandlerTest.java
git commit -m "feat(captain): authorize Muster promotion actions"
```

### Task 7: Minimal Muster Roll controls and localization

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/screen/CaptainActionScreenModel.java`
- Modify: `src/client/java/net/hfstack/rallyguard/screen/MusterRollScreen.java`
- Modify: `src/main/resources/assets/rallyguard/lang/en_us.json`
- Modify: `src/main/resources/assets/rallyguard/lang/pt_br.json`
- Test: `src/test/java/net/hfstack/rallyguard/screen/CaptainActionScreenModelTest.java`
- Modify: `src/test/java/net/hfstack/rallyguard/ResourceSchemaTest.java`

**Interfaces:**
- Consumes: snapshot action state and `CaptainActionC2SPayload`.
- Produces: a pure screen model selecting visible/enabled action, label arguments, and blocking reason.
- Produces: one minimal Muster Roll button that sends only the approved action.

- [ ] **Step 1: Write failing screen-model and resource tests**

Cover hidden actions for unowned guards, enabled promotion with item/cost, disabled low-level promotion, enabled demotion, disabled Officer demotion, and exact presence of all English/Brazilian Portuguese feedback, button, and config keys.

- [ ] **Step 2: Run focused tests and verify RED**

Run: `.\gradlew.bat test --tests '*CaptainActionScreenModelTest' --tests '*ResourceSchemaTest'`

Expected: FAIL because model and localization are missing.

- [ ] **Step 3: Implement the functional button and translations**

Use vanilla widgets, rebuild their state whenever a new snapshot arrives, and keep all presentation derived from the server action view. Do not redesign unrelated panel layout; increase panel height only if required to avoid overlap.

- [ ] **Step 4: Run focused tests and compile the client**

Run: `.\gradlew.bat test --tests '*CaptainActionScreenModelTest' --tests '*ResourceSchemaTest'`

Expected: PASS and `compileClientJava` succeeds as part of the test lifecycle.

- [ ] **Step 5: Commit the functional GUI**

```powershell
git add -- src/main/java/net/hfstack/rallyguard/screen/CaptainActionScreenModel.java src/client/java/net/hfstack/rallyguard/screen/MusterRollScreen.java src/main/resources/assets/rallyguard/lang src/test/java/net/hfstack/rallyguard/screen/CaptainActionScreenModelTest.java src/test/java/net/hfstack/rallyguard/ResourceSchemaTest.java
git commit -m "feat(captain): add Muster promotion controls"
```

### Task 8: Documentation, compatibility contract, and release verification

**Files:**
- Modify: `docs/API.md`
- Create: `docs/testing/26.1-captain-promotion-smoke-test.md`
- Modify: `README.md`
- Modify: `CHANGELOG.md`
- Modify: `ROADMAP.md`
- Modify: `docs/superpowers/specs/2026-10-15-captain-promotion-demotion-design.md`
- Test: `src/test/java/net/hfstack/rallyguard/ResourceSchemaTest.java`

**Interfaces:**
- Consumes: completed Captain API v5 and user-facing behavior.
- Produces: integration documentation, manual smoke matrix, and completed roadmap state while retaining version 1.4.0.

- [ ] **Step 1: Write the documentation assertions before editing docs**

Extend `ResourceSchemaTest` to require API version 5 documentation, promotion defaults, both language keys, and the absence of `1.5.0` in tracked release metadata.

- [ ] **Step 2: Run the documentation test and verify RED**

Run: `.\gradlew.bat test --tests '*ResourceSchemaTest'`

Expected: FAIL until API, README, changelog, roadmap, and smoke-test documentation are aligned.

- [ ] **Step 3: Update documentation and mark the specification implemented**

Document `RallyGuardApi.captains()`, outcomes, events, logical-server requirement, config defaults, UI workflow, Officer restriction, and compatibility. Change the spec status to `Implemented`; mark the roadmap item complete only after all automated verification passes. Keep the changelog under `Unreleased — planned 1.4.0`.

- [ ] **Step 4: Run complete verification**

Run: `.\gradlew.bat clean test build`

Expected: `BUILD SUCCESSFUL`, zero failed tests, and distributable artifacts named with version `1.4.0`.

- [ ] **Step 5: Inspect the final diff and repository state**

Run: `git diff --check`  
Run: `git status --short`  
Run: `rg -n "1\.5\.0" gradle.properties CHANGELOG.md README.md docs src`

Expected: no whitespace errors, only intended Task 8 files uncommitted, and no `1.5.0` matches.

- [ ] **Step 6: Commit release documentation**

```powershell
git add -- docs/API.md docs/testing/26.1-captain-promotion-smoke-test.md README.md CHANGELOG.md ROADMAP.md docs/superpowers/specs/2026-10-15-captain-promotion-demotion-design.md src/test/java/net/hfstack/rallyguard/ResourceSchemaTest.java
git commit -m "docs(release): finalize captain promotion"
```

- [ ] **Step 7: Verify the committed tree**

Run: `git status --short --branch`  
Run: `.\gradlew.bat test`

Expected: clean working tree, branch ahead only by the intended commits, and `BUILD SUCCESSFUL`.
