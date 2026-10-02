# Minecraft 26.1 Port Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce one Rally of the Guard Fabric JAR that preserves the current feature set and runs with Guard Villagers 2.1.3 on Minecraft 26.1, 26.1.1, and 26.1.2.

**Architecture:** Keep a single 26.1 source tree on the existing `26.1` branch, migrate it from Yarn names to the official Mojang namespace, and compile against the published Guard Villagers 26.1 artifact. Keep patch-version handling out of production code unless runtime testing proves that a compatibility boundary is necessary.

**Tech Stack:** Java 25, Gradle 9.5.1, Fabric Loom 1.15.5, Fabric Loader 0.18.4, Fabric API 0.144.3+26.1, MidnightLib 1.9.2+26.1-fabric, Guard Villagers 2.1.3-26.1, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-09-26-minecraft-26-1-port-design.md`

## Global Constraints

- Build against Minecraft `26.1` with official Mojang mappings and Java 25.
- Resolve Guard Villagers from `curse.maven:guard-villagers-fabric-571503:7863507`; a root-local `guardvillagers-2.1.3-26.1.jar` may override it for development.
- Declare runtime compatibility as `minecraft: ~26.1`, `java: >=25`, and `fabricloader: >=0.18.4`.
- Generate archive base name `rallyguard-fabric-26.1` from one source set; do not create patch-specific builds.
- Preserve recruitment, ownership, commands, formations, routes, combat, screens, persistence, extension APIs, config adjustment, and spawn-egg behavior.
- Do not modify or publish the sibling GuardVillagers repository.

## Review Focus

- The local Guard Villagers JAR is absent: dependency resolution must fall back to Curse Maven file `7863507`, not another release.
- `guardvillagers.json` is missing, already patched, or malformed: initialization must create/update safely and log failures without crashing.
- A saved route tag is malformed or contains out-of-range progress: parsing must return a bounded valid state or the empty state.
- A network request names a missing guard, a guard owned by another player, or an invalid target: the server must reject it without mutating guard state.
- Minecraft is 26.1.1 or 26.1.2: the exact JAR built against 26.1 must load and pass smoke checks unchanged.

---

### Task 1: Establish the 26.1 build contract

**Files:**
- Modify: `build.gradle`
- Modify: `gradle.properties`
- Modify: `src/main/resources/fabric.mod.json`
- Modify: `src/main/resources/rallyguard.mixins.json`
- Modify: `src/client/resources/rallyguard.client.mixins.json`
- Delete: `src/main/java/net/hfstack/rallyguard/mixin/ExampleMixin.java`
- Delete: `src/client/java/net/hfstack/rallyguard/mixin/client/ExampleClientMixin.java`

**Interfaces:**
- Consumes: Curse Maven project `571503`, file `7863507`.
- Produces: a Java 25/Minecraft 26.1 Gradle classpath in the official namespace and expanded Fabric metadata for `~26.1`.

- [ ] **Step 1: Pin the target platform in `gradle.properties`**

Set `minecraft_version=26.1`, `loader_version=0.18.4`, `loom_version=1.15.5`, `fabric_version=0.144.3+26.1`, `midnightlib_version=1.9.2+26.1-fabric`, `guardvillagers_jar=guardvillagers-2.1.3-26.1.jar`, `guardvillagers_file_id=7863507`, and `archives_base_name=rallyguard-fabric-26.1`. Keep `mod_version=1.3.1` unless release policy supplies a later Rally version.

- [ ] **Step 2: Replace Yarn configuration with the official mapping build**

In `build.gradle`, remove the Yarn mapping dependency, use Fabric Loom `1.15.5`, retain the optional root-local Guard Villagers JAR with Curse Maven fallback, add the MidnightDust release repository and the 26.1 MidnightLib compile dependency, remove incompatible optional development-only UI mods, and set Java source, target, and compiler release to 25.

- [ ] **Step 3: Update metadata and remove no-op mixins**

Set Fabric dependency ranges to the Global Constraints, set both mixin compatibility levels to `JAVA_25`, remove `ExampleMixin` and `ExampleClientMixin` from their JSON arrays, then delete the two empty/example Java classes. Remove a mixin config from `fabric.mod.json` if its array becomes empty.

- [ ] **Step 4: Verify resource processing and dependency identity**

Run: `./gradlew.bat --refresh-dependencies processResources dependencies --configuration modImplementation`

Expected: `BUILD SUCCESSFUL`; the dependency report contains `curse.maven:guard-villagers-fabric-571503:7863507`; `build/resources/main/fabric.mod.json` contains `"minecraft": "~26.1"`, `"java": ">=25"`, and `"fabricloader": ">=0.18.4"`.

- [ ] **Step 5: Record the expected migration failure**

Run: `./gradlew.bat compileJava`

Expected: FAIL only because existing Rally sources still import Yarn-named `net.minecraft.*` types; dependency or toolchain resolution errors must be fixed before Task 2.

- [ ] **Step 6: Commit**

```bash
git add build.gradle gradle.properties src/main/resources/fabric.mod.json src/main/resources/rallyguard.mixins.json src/client/resources/rallyguard.client.mixins.json src/main/java/net/hfstack/rallyguard/mixin src/client/java/net/hfstack/rallyguard/mixin
git commit -m "build: target Minecraft 26.1"
```

### Task 2: Port common code and extension APIs to official mappings

**Files:**
- Modify: `src/main/java/net/hfstack/rallyguard/**/*.java`
- Modify: `src/test/java/net/hfstack/rallyguard/**/*.java`
- Create: `src/test/java/net/hfstack/rallyguard/order/GuardRoutesTest.java`
- Create: `src/test/java/net/hfstack/rallyguard/util/GuardVillagersConfigPatcherTest.java`

**Interfaces:**
- Consumes: official 26.1 Minecraft classes and `dev.sterner.guardvillagers.common.entity.GuardEntity` from Task 1.
- Produces: compiling server/common code; unchanged Rally API semantics expressed with official types such as `ServerPlayer`, `ServerLevel`, `BlockPos`, `Component`, and `Identifier`.

- [ ] **Step 1: Add route corruption tests**

Expose `GuardRoutes.parse(String tag)` with package-private visibility. In `GuardRoutesTest`, assert that malformed numeric fields return an empty inactive route, negative counters are clamped to zero, an index beyond the point list resets to zero, and points beyond `RallyConfig.routeMaxPoints()` are discarded.

- [ ] **Step 2: Add config patcher tests**

Extract package-private `GuardVillagersConfigPatcher.PatchResult patchFollowHeroConfig(Path file)` with results `UPDATED`, `UNCHANGED`, and `FAILED`; keep public `patchFollowHeroConfig()` as the Fabric config-directory adapter. Test a missing file, `followHero=true`, `followHero=false`, and malformed JSON in a JUnit temporary directory; malformed JSON must return `FAILED` without escaping an exception.

- [ ] **Step 3: Run the focused tests to verify migration failure**

Run: `./gradlew.bat test --tests "net.hfstack.rallyguard.order.GuardRoutesTest" --tests "net.hfstack.rallyguard.util.GuardVillagersConfigPatcherTest"`

Expected: FAIL because the new test interfaces and official-mapping imports are not implemented yet.

- [ ] **Step 4: Port all common/server sources**

Translate imports and changed method calls across `api`, `component`, `config`, `contract`, `effect`, `event`, `item`, `network`, `order`, `screen`, `service`, `util`, and `RallyOfTheGuard.java`. Preserve public API names and behavior; only upstream Minecraft types may change. Use the Guard Villagers 26.1 source at commit `dc43e85^` and the published JAR as the contract for guard ownership, following, patrolling, patrol positions, registry fields, and spawn-egg data.

- [ ] **Step 5: Implement the two safety seams**

Implement `GuardRoutes.parse(String)` and `GuardVillagersConfigPatcher.patchFollowHeroConfig(Path)` exactly as exercised in Steps 1-2. The public config adapter logs `FAILED` and never interrupts mod initialization.

- [ ] **Step 6: Port the existing API tests**

Update only mapping-dependent types in the six existing test classes. Retain every existing assertion covering policy order, denial short-circuiting, transactions, offer validation, and presentation merging.

- [ ] **Step 7: Verify common code and tests**

Run: `./gradlew.bat compileJava test`

Expected: `BUILD SUCCESSFUL`; all existing tests plus `GuardRoutesTest` and `GuardVillagersConfigPatcherTest` pass.

- [ ] **Step 8: Verify no old Yarn package imports remain in common code**

Run: `rg -n "net\.minecraft\.(entity|item|registry|screen|server\.network|server\.world|text|util\.math|world\.World)" src/main/java src/test/java`

Expected: no matches.

- [ ] **Step 9: Commit**

```bash
git add src/main/java src/test/java
git commit -m "feat: port common code to Minecraft 26.1"
```

### Task 3: Port client networking and screens

**Files:**
- Modify: `src/client/java/net/hfstack/rallyguard/RallyOfTheGuardClient.java`
- Modify: `src/client/java/net/hfstack/rallyguard/client/ClientHooks.java`
- Modify: `src/client/java/net/hfstack/rallyguard/screen/*.java`

**Interfaces:**
- Consumes: payload IDs/codecs and screen-handler types compiled in Task 2.
- Produces: a compiling 26.1 client entrypoint with the current key binding, command screens, route editor, combat target selection, and hire screen.

- [ ] **Step 1: Capture the client compilation failure**

Run: `./gradlew.bat compileClientJava`

Expected: FAIL only on Yarn-named client types or client API signatures.

- [ ] **Step 2: Port client entrypoints and screens**

Convert the client imports and 26.1 API calls for `Minecraft`, GUI graphics, screens, buttons, text, player inventory, hit results, key mappings, and handled-screen registration. Keep widget labels, actions, layout values, payload contents, and key behavior unchanged.

- [ ] **Step 3: Verify client compilation and the full automated suite**

Run: `./gradlew.bat compileClientJava test`

Expected: `BUILD SUCCESSFUL` with all tests passing.

- [ ] **Step 4: Verify no old Yarn package imports remain in client code**

Run: `rg -n "net\.minecraft\.(client\.MinecraftClient|client\.gui\.DrawContext|client\.gui\.screen|client\.gui\.widget|client\.option|client\.util|entity|text|util\.hit|util\.math)" src/client/java`

Expected: no matches.

- [ ] **Step 5: Commit**

```bash
git add src/client/java
git commit -m "feat: port client UI to Minecraft 26.1"
```

### Task 4: Validate resources and the release artifact

**Files:**
- Modify as required: `src/main/resources/data/rallyguard/recipes/*.json`
- Modify as required: `src/main/resources/assets/rallyguard/items/*.json`
- Modify as required: `src/main/resources/assets/rallyguard/models/item/*.json`
- Modify as required: `src/main/resources/assets/guardvillagers/models/item/guard_spawn_egg.json`
- Create: `src/test/java/net/hfstack/rallyguard/ResourceSchemaTest.java`

**Interfaces:**
- Consumes: registered item IDs `rallyguard:scroll_of_rallying`, `rallyguard:commanders_ledger`, and Guard Villagers spawn egg identifiers.
- Produces: valid 26.1 resources and `build/libs/rallyguard-fabric-26.1-1.3.1.jar`.

- [ ] **Step 1: Add resource contract tests**

In `ResourceSchemaTest`, load the two recipe JSON files and assert their result IDs, load both item-definition JSON files and assert their model IDs, and assert that every referenced model and texture exists under `src/main/resources`.

- [ ] **Step 2: Run the resource test**

Run: `./gradlew.bat test --tests "net.hfstack.rallyguard.ResourceSchemaTest"`

Expected: PASS if current formats remain valid; if 26.1 requires a schema change, first capture the failing build/runtime diagnostic and then update only the affected JSON.

- [ ] **Step 3: Build and inspect the JAR**

Run: `./gradlew.bat clean build`

Expected: `BUILD SUCCESSFUL`; one remapped release JAR exists at `build/libs/rallyguard-fabric-26.1-1.3.1.jar` alongside its sources JAR.

- [ ] **Step 4: Inspect packaged metadata and resources**

Expand the release JAR to a temporary directory and assert that `fabric.mod.json` contains version `1.3.1`, Minecraft `~26.1`, Java `>=25`, and Guard Villagers as a required dependency; assert both recipes, both Rally item definitions/models/textures, languages, icon, and the Guard Villagers spawn-egg model are present.

- [ ] **Step 5: Commit**

```bash
git add src/main/resources src/test/java/net/hfstack/rallyguard/ResourceSchemaTest.java
git commit -m "test: validate 26.1 packaged resources"
```

### Task 5: Exercise the 26.1 runtime integration

**Files:**
- Create: `docs/testing/26.1-smoke-test.md`
- Modify as failures require: files owned by Tasks 2-4

**Interfaces:**
- Consumes: the exact release JAR from Task 4 and Guard Villagers `2.1.3-26.1`.
- Produces: a recorded client/server smoke result for Minecraft 26.1 and fixes for any discovered API, mixin, or runtime integration errors.

- [ ] **Step 1: Start the dedicated server development runtime**

Run: `./gradlew.bat runServer`

Expected: Fabric reaches the server-ready state without missing-class, mixin, registry, payload, or entrypoint errors. Stop it cleanly after startup verification.

- [ ] **Step 2: Start the client development runtime**

Run: `./gradlew.bat runClient`

Expected: the title screen loads with Rally, Guard Villagers, Fabric API, and MidnightLib enabled and without startup exceptions.

- [ ] **Step 3: Execute the feature smoke matrix on 26.1**

Record in `docs/testing/26.1-smoke-test.md`: guard recruitment and ownership persistence; summon/follow/wait/patrol; formation and fixed route progression; hostile targeting and friendly-fire protection; command, route, combat, and hire screens; spawn egg; config patch; save/reload persistence. Include the game version, dependency versions, JAR filename, SHA-256, and pass/fail for every row.

- [ ] **Step 4: Exercise rejected network actions**

During the 26.1 smoke run, attempt commands for a missing guard, another player's guard, a missing/dead target, and a disallowed target. Expected: translated feedback is shown, no unrelated entity state changes, and no server exception is logged.

- [ ] **Step 5: Fix and repeat until clean**

For each runtime failure, add the narrowest automated regression test available, correct the owning Task 2-4 file, then rerun `./gradlew.bat clean build runServer` and the affected client smoke row.

- [ ] **Step 6: Commit**

```bash
git add src docs/testing/26.1-smoke-test.md
git commit -m "test: verify Minecraft 26.1 runtime"
```

### Task 6: Prove one-JAR patch compatibility

**Files:**
- Modify: `docs/testing/26.1-smoke-test.md`
- Modify only if incompatibility is proven: `src/main/java/net/hfstack/rallyguard/compat/**`

**Interfaces:**
- Consumes: the unchanged Task 4 release JAR, identified by one SHA-256 value.
- Produces: compatibility evidence for Minecraft 26.1.1 and 26.1.2 or a narrowly scoped internal compatibility adapter if evidence requires one.

- [ ] **Step 1: Preserve the candidate artifact**

Copy `build/libs/rallyguard-fabric-26.1-1.3.1.jar` outside `build/`, compute its SHA-256, and record it in the smoke document. Do not rebuild between patch-version runs.

- [ ] **Step 2: Test the unchanged artifact on Minecraft 26.1.1**

Install the preserved Rally JAR with Fabric Loader, Fabric API, Guard Villagers 2.1.3-26.1, and required libraries for 26.1.1. Verify startup plus recruitment, one command, one route, combat targeting, a UI interaction, and save/reload; record results and the same SHA-256.

- [ ] **Step 3: Test the unchanged artifact on Minecraft 26.1.2**

Repeat Step 2 on 26.1.2. Expected: every smoke row passes and the Rally SHA-256 is identical to the 26.1 and 26.1.1 runs.

- [ ] **Step 4: Add compatibility code only if a patch fails**

If and only if a binary or behavioral difference is reproduced, create a focused class under `net.hfstack.rallyguard.compat`, add a regression test for version-independent behavior, route the affected call through it, rebuild once, and rerun all three version matrices with that one new artifact.

- [ ] **Step 5: Run final verification**

Run: `./gradlew.bat clean build`

Expected: `BUILD SUCCESSFUL`, all tests pass, metadata still declares `~26.1`, and the final smoke document has passing rows for 26.1, 26.1.1, and 26.1.2 using one artifact hash.

- [ ] **Step 6: Commit**

```bash
git add docs/testing/26.1-smoke-test.md src/main/java src/test/java
git commit -m "docs: verify 26.1 patch compatibility"
```
