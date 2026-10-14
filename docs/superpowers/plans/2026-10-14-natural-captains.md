# Natural Captains Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add persistent, region-limited natural Captains and finish the Guard Profiles & Progression milestone.

**Architecture:** Extend the versioned immutable profile with a Captain flag, keep selection math in a pure policy, and let the entity-aware initializer supply the nearby-Captain count. Thread the read-only flag through hiring and Muster Roll presentation without implementing future officer behavior.

**Tech Stack:** Java 25, Fabric API, Minecraft 26.1, JUnit 5, Sponge Mixin.

**Spec:** `docs/superpowers/specs/2026-10-14-natural-captains-design.md`

## Global Constraints

- Preserve unsupported future profile schemas without rewriting them.
- Existing profiles migrate with `captain = false`; no retroactive roll.
- Only newly initialized natural guards are eligible.
- Default selection: 5% chance, minimum level 5, 128-block radius, zero/half/blocked chance at 0/1/2 nearby Captains.
- Default hiring bonus: +4, applied before the final configured clamp.
- Do not add Squads, promotion, officer commands, or leadership bonuses.
- Do not create Git commits unless the user explicitly requests them.

## Review Focus

- Schema-1 worlds must migrate without a Captain reroll or data loss.
- Future schemas must remain unavailable and untouched after schema 2 ships.
- Disabled/invalid configuration must not accidentally create Captains or prices outside bounds.
- A Captain promoted to the minimum level must receive the exact cumulative XP for that level.
- Snapshot encode/decode order must remain symmetrical after adding the flag.

---

### Task 1: Versioned Captain persistence and public view

**Files:**
- Modify: `src/main/java/net/hfstack/rallyguard/profile/GuardProfile.java`
- Modify: `src/main/java/net/hfstack/rallyguard/profile/GuardProfileCodec.java`
- Modify: `src/main/java/net/hfstack/rallyguard/api/profile/GuardProfileView.java`
- Modify: `src/test/java/net/hfstack/rallyguard/profile/GuardProfileCodecTest.java`
- Modify dependent profile fixtures under `src/test/java`

**Interfaces:**
- Produces: `boolean GuardProfileView.captain()` and schema-2 `GuardProfile` factories preserving the flag through progress updates.

- [x] Write failing round-trip, schema-1 migration, progress-preservation, approved-key, and future-schema tests.
- [x] Run `./gradlew.bat test --tests net.hfstack.rallyguard.profile.GuardProfileCodecTest --tests net.hfstack.rallyguard.profile.GuardProfileProgressionTest` and confirm failure from the missing Captain field/schema behavior.
- [x] Implement schema-2 storage and conservative schema-1 migration.
- [x] Update compile-time fixtures to pass the explicit Captain value.
- [x] Re-run the targeted tests and confirm they pass.

### Task 2: Captain selection, regional context, and configuration

**Files:**
- Create: `src/main/java/net/hfstack/rallyguard/profile/NaturalCaptainPolicy.java`
- Create: `src/test/java/net/hfstack/rallyguard/profile/NaturalCaptainPolicyTest.java`
- Modify: `src/main/java/net/hfstack/rallyguard/profile/GuardProfileGenerator.java`
- Modify: `src/main/java/net/hfstack/rallyguard/profile/GuardProfileInitializer.java`
- Modify: `src/main/java/net/hfstack/rallyguard/config/RallyConfig.java`
- Modify: `src/test/java/net/hfstack/rallyguard/profile/GuardProfileGeneratorTest.java`
- Modify: `src/test/java/net/hfstack/rallyguard/config/RallyConfigTest.java`

**Interfaces:**
- Consumes: `GuardProfile.captain()` and schema-2 factory from Task 1.
- Produces: pure `NaturalCaptainPolicy.adjustedChance(...)`, `isSelected(...)`, and `minimumLevel(...)`; generator inputs include eligibility and nearby-Captain count.

- [x] Write failing policy tests for full/half/zero chance, feature disablement, clamping, and exact minimum-level promotion.
- [x] Run the policy/config/generator tests and confirm the expected failures.
- [x] Implement clamped Captain configuration and pure policy.
- [x] Extend deterministic generation without changing non-Captain random attributes.
- [x] Count loaded nearby Captains in the initializer and pass that context to generation.
- [x] Re-run the targeted tests and confirm they pass.

### Task 3: Captain hiring value

**Files:**
- Modify: `src/main/java/net/hfstack/rallyguard/recruitment/GuardHiringValueModel.java`
- Modify: `src/main/java/net/hfstack/rallyguard/service/DefaultGuardHiringValueService.java`
- Modify: `src/test/java/net/hfstack/rallyguard/recruitment/GuardHiringValueModelTest.java`

**Interfaces:**
- Consumes: `GuardProfileView.captain()` and `RallyConfig.captainHireCostBonus()`.
- Produces: `GuardHiringValueModel.cost(..., int captainBonus, ...)` with final clamp after the bonus.

- [x] Write failing tests for +4 Captain cost, non-Captain stability, multiplier ordering, overflow safety, and maximum clamp.
- [x] Run the hiring model tests and confirm failure.
- [x] Add the bonus to the pure model and wire the service/config value.
- [x] Re-run the targeted tests and confirm they pass.

### Task 4: Muster Roll transport and presentation

**Files:**
- Modify: `src/main/java/net/hfstack/rallyguard/muster/GuardProfileSnapshot.java`
- Modify: `src/main/java/net/hfstack/rallyguard/muster/GuardProfileSnapshotFactory.java`
- Modify: `src/main/java/net/hfstack/rallyguard/network/payload/MusterRollSnapshotS2CPayload.java`
- Modify: `src/client/java/net/hfstack/rallyguard/screen/MusterRollScreen.java`
- Modify: `src/main/resources/assets/rallyguard/lang/en_us.json`
- Modify: `src/main/resources/assets/rallyguard/lang/pt_br.json`
- Modify: `src/test/java/net/hfstack/rallyguard/muster/GuardProfileSnapshotTest.java`
- Modify: `src/test/java/net/hfstack/rallyguard/ResourceSchemaTest.java`

**Interfaces:**
- Consumes: `GuardProfileView.captain()`.
- Produces: bounded immutable snapshot and symmetrical wire encoding carrying `boolean captain`.

- [x] Write failing snapshot/resource contract tests for the Captain flag and both translations.
- [x] Run the snapshot/resource tests and confirm failure.
- [x] Thread the flag through factory, payload, and screen; render a centered gold role label without overlap.
- [x] Re-run the targeted tests and confirm they pass.

### Task 5: Documentation, milestone status, and full verification

**Files:**
- Modify: `ROADMAP.md`
- Modify: `CHANGELOG.md`
- Modify: `README.md`
- Modify: `docs/API.md`
- Modify: `docs/testing/26.1-smoke-test.md`

**Interfaces:**
- Consumes: completed behavior from Tasks 1-4.
- Produces: current user/developer documentation and accurate Minecraft 26.1 test evidence.

- [x] Document Captain configuration, API field, hiring bonus, UI, migration, and the crossbow fix.
- [x] Record the user-confirmed Minecraft 26.1 manual checks without changing historical 26.1.1/26.1.2 results.
- [x] Mark Natural Captains and Guard Profiles & Progression complete only after all automated verification passes.
- [x] Run `./gradlew.bat clean build` and confirm zero failures.
- [x] Run `./gradlew.bat runServer` and confirm Rally and all required Mixins initialize before the expected local EULA stop.
- [x] Run `git diff --check`, inspect the complete scoped diff, and report any pre-existing warnings separately.
