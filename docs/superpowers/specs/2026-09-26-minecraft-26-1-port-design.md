# Rally of the Guard: Minecraft 26.1 Port

## Objective

Port the current Rally of the Guard `1.21.11` codebase to Minecraft `26.1` while preserving its existing behavior and public extension points. The `26.1` branch must produce one Fabric JAR that runs on Minecraft `26.1`, `26.1.1`, and `26.1.2` with Guard Villagers `2.1.3-26.1`.

## Branch and repository scope

- Develop on a dedicated `26.1` branch created from the current `1.21.11` branch.
- Modify only the Rally of the Guard repository.
- Treat the sibling GuardVillagers repository and its extracted `guardvillagers-2.1.3-26.1.jar` as technical references.
- Do not require absolute paths or files outside this repository for a reproducible release build.

## Target platform

The port will align with the published Guard Villagers artifact:

- Minecraft `26.1`
- Java 25
- Fabric Loader `0.18.4`
- Fabric API `0.144.3+26.1`
- Fabric Loom compatible with the 26.1 toolchain
- Official Mojang mappings
- Guard Villagers `2.1.3-26.1`
- MidnightLib version compatible with Guard Villagers and Minecraft 26.1

The Rally metadata will declare Minecraft compatibility as `~26.1`. The archive name will identify the target as `rallyguard-fabric-26.1`.

## Build and dependency design

The Gradle build will move from Yarn-named Minecraft APIs to the official mapping namespace used by Guard Villagers 26.1. Dependency versions and repositories will be updated for the target platform.

The build may support a repository-local Guard Villagers JAR override for development, but normal builds must resolve a published dependency. A missing optional local override must therefore fall back to the published artifact rather than fail because of a machine-specific path.

The generated mod metadata must express the actual minimum Java and loader requirements and use `~26.1` for the Minecraft dependency. No separate builds or source sets will be introduced for patch releases.

## Source migration

All existing production and test sources will be migrated to the official mapping namespace. API changes between Minecraft 1.21.11 and 26.1 will be handled internally, including changes affecting:

- entities, players, worlds, positions, registries, items, and effects;
- data components, persistence, and serialization;
- Fabric events and lifecycle hooks;
- custom payload registration, codecs, and client/server handlers;
- screens, menu handlers, key bindings, and client rendering hooks;
- mixin targets and descriptors;
- recipes, item models, and other versioned resource formats.

The migration will preserve the existing Rally public API wherever the target platform permits. Necessary signature changes caused by upstream types will be documented and kept as narrow as possible.

## Guard Villagers integration

The port will compile and run against Guard Villagers `2.1.3-26.1`. The implementation will preserve:

- guard recruitment and ownership;
- summon, follow, wait, and patrol commands;
- rally formations and fixed routes;
- combat targeting and friendly-fire behavior;
- hired-guard neutrality;
- guard presentation and eligibility extension points;
- Guard Villagers configuration adjustment;
- spawn-egg compatibility behavior.

The published 26.1 JAR is the runtime contract. Source from the Guard Villagers commit immediately before its 26.2 port may be used to understand that contract, but Rally will not rely on unpublished implementation changes.

## Compatibility behavior

One compiled Rally JAR will be used unchanged on Minecraft `26.1`, `26.1.1`, and `26.1.2`. There will be no patch-version detection unless testing demonstrates an actual binary or behavioral incompatibility.

If a patch release introduces an incompatibility despite the `~26.1` contract, Rally will contain a small internal compatibility boundary that selects the required behavior without changing user-facing features or producing separate artifacts.

## Error handling

Existing user-facing validation and command results will be retained. Integration failures must fail safely: invalid or missing guards, worlds, owners, routes, or network targets must not crash the game or mutate unrelated state. Version-specific compatibility failures should be isolated and logged with enough context to diagnose them.

Build configuration must report unresolved required dependencies clearly. It must not silently compile against an unintended Guard Villagers version.

## Verification

Verification will proceed in layers:

1. Run all existing unit tests after their mapping migration.
2. Run the complete Gradle build and inspect the generated JAR and expanded metadata.
3. Verify mixin application and start Fabric development client and server on Minecraft 26.1.
4. Exercise recruitment, ownership, commands, formations, routes, combat, screens, persistence, and spawn-egg behavior with Guard Villagers 2.1.3-26.1.
5. Install the same built JAR with matching dependencies on Minecraft 26.1.1 and 26.1.2 and repeat targeted smoke tests.

The port is complete when the build and tests pass, client and server start successfully, the feature checks pass on 26.1, and the same artifact passes smoke testing on 26.1.1 and 26.1.2.

## Non-goals

- Supporting Minecraft 26.2 from this branch.
- Maintaining 1.21.11 and 26.1 in one Gradle build.
- Modifying or republishing Guard Villagers.
- Adding new gameplay features unrelated to compatibility.
- Refactoring working subsystems unless required by a changed upstream API.
