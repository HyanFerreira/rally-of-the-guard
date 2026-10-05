# Allied Protection and Rally Retreat Design

## Objective

Prevent guards hired by a player from attacking living entities owned by that
same player, and make rallied guards abandon combat to follow their commander
when the commander leaves the battlefield.

This is a focused safety and Rally-behavior update. It will be implemented and
validated across the supported Minecraft branches before work begins on Guard
Profiles & Progression.

## Scope

The update includes:

- a central server-side policy for deciding whether an entity is a protected
  ally of a commander;
- rejection of manual attack orders against protected allies;
- removal of protected allies selected by a hired guard's native AI;
- cancellation of damage from a hired guard to a protected ally as a final
  safety barrier;
- automatic combat disengagement for rallied guards that fall too far behind
  their commander;
- configurable retreat behavior and retreat distance;
- Portuguese and English configuration text;
- automated coverage for pure decision logic and manual gameplay verification;
- ports to the `26.1`, `1.21.11`, and `1.21.1` branches.

The update does not include:

- an explicit Retreat command or new command screen;
- health-based retreat;
- cross-dimensional or portal following;
- protection based on vanilla teams, guilds, or allied players;
- a complete combat-stance system;
- Profiles & Progression.

## Protected Ally Policy

The server will expose one central decision equivalent to:

```text
isProtectedAlly(commanderUuid, target)
```

A living target is protected when at least one of the following is true:

1. The target is the commander.
2. The target is a Rally guard whose owner UUID equals the commander UUID.
3. The target is a tameable or ownable living entity whose owner UUID equals
   the commander UUID.
4. The target has a direct, observable association with the commander that the
   vanilla entity API exposes at that moment: the commander is riding it, or
   the entity is leashed directly to the commander.

This is a relationship rule, not a species allowlist. Persistent ownership is
intended to cover wolves, cats, parrots, horses, donkeys, mules, llamas, and
future vanilla living entities that expose compatible ownership information.
Transient riding/leash association covers entities such as Happy Ghasts while
the commander is riding or directly leading them.

Vanilla Happy Ghasts do not expose persistent player ownership merely because
they wear a harness. A Happy Ghast that is neither ridden nor leashed therefore
cannot be distinguished reliably from another player's Happy Ghast in this
update. Persistently assigning such entities to a player would require a new
Rally-owned data system and is deferred.

Untamed entities and entities owned by another player are not protected by this
policy. Protection for vanilla teams or shared commanders is deferred until the
multiplayer shared-command feature.

Each Minecraft branch may require different mapping names or ownership APIs.
The branch implementation may use version-specific adapters, but all branches
must satisfy the same policy and gameplay behavior.

## Defense in Depth

The policy will be enforced at three boundaries.

### Manual attack orders

The server-side attack-order handler will reject a protected target before it
assigns that target to any guard. The player will receive the existing invalid
target feedback. Client input is never trusted as the source of this decision.

### Native AI target correction

While processing a hired guard, Rally will clear the target when the selected
living target is protected for that guard's commander. It will also clear the
guard's aggressive/attacking state and stop combat navigation as required by
the version-specific Guard Villagers API.

This correction applies to hired guards even when they are not currently in a
Rally. A player's guard must never retaliate against or hunt that same player's
owned companions merely because native AI selected them.

### Damage cancellation

A server-side living-entity damage event will cancel damage when:

- the attacker can be resolved to a Rally guard;
- the guard has a commander;
- the victim is protected for that commander.

Direct melee damage and damage whose source exposes the guard as its causing
entity are covered. Damage with no resolvable guard attacker is outside this
update. This barrier prevents harm if AI target correction is bypassed or a
target is reacquired between ticks.

## Automatic Rally Retreat

Retreat behavior applies only to guards marked as rallied. Waiting guards,
fixed patrol guards, route guards, and ordinary non-rallied guards retain their
current behavior.

Two configuration values will be introduced:

```text
rally.retreatEnabled = true
rally.retreatDistance = 24.0
```

The public/configuration names must follow the existing MidnightLib naming
conventions used by `RallyConfig`; the dotted names above describe their logical
categories rather than requiring literal field names.

For each rallied guard in the commander's current dimension:

1. If its current target is protected, disengage immediately regardless of
   distance.
2. If it has a live, non-protected target and is no farther than the configured
   retreat distance from the commander, allow native combat to continue.
3. If it has a live target and is farther than the configured retreat distance,
   disengage from combat and resume formation behavior during the same update.
4. If it has no live target, continue existing formation behavior.

Disengaging means:

- clear the combat target;
- clear aggressive/attacking state;
- stop active item use when necessary;
- stop combat navigation;
- allow formation navigation to be calculated immediately.

Once disengaged, the existing formation movement and corrective teleport rules
remain authoritative. `formationTeleportDistance` still controls teleporting;
the new retreat distance does not introduce a second teleport threshold.

When `retreatEnabled` is false, a rallied guard with a valid hostile target may
continue fighting as it does today. Protected-ally rules remain mandatory and
cannot be disabled by this setting.

The retreat distance is measured between the guard and commander, not between
the target and commander. The configured value must be positive and should
default below the existing formation teleport distance, so normal movement is
attempted before teleport correction.

## Runtime Flow

```text
Rallied guard update
        |
        v
Does it have a live target?
   |                 |
  no                yes
   |                 |
   v                 v
Formation       Is target protected?
                     |          |
                    yes         no
                     |          |
                     v          v
                Disengage   Retreat enabled and
                            commander too far away?
                                 |          |
                                yes         no
                                 |          |
                                 v          v
                            Disengage   Continue combat
                                 |
                                 v
                              Formation
```

## Components and Responsibilities

### Protected-ally policy

A focused policy class owns commander/target relationship checks. Network,
event, and formation code must call this policy rather than reproduce ownership
logic.

### Combat-state helper

A small helper performs a complete, version-appropriate disengagement. Manual
commands, protected-target correction, and automatic retreat should share it so
that clearing a target cannot leave stale attacking or navigation state.

### Target-safety handler

A server tick or appropriate entity lifecycle callback corrects protected AI
targets for hired guards. The implementation should avoid unbounded global
scans: it should reuse existing loaded-world guard iteration or another bounded
server-side mechanism available in each supported version.

### Damage-safety handler

A Fabric living-entity damage callback provides the final no-damage guarantee.
It resolves the responsible guard from the damage source and consults the
protected-ally policy.

### Rally formation ticker

The current early return for any live target will be replaced with a decision:
continue combat, or disengage and calculate the formation slot. A successful
retreat therefore restores navigation or corrective teleport during the same
formation update.

### Configuration and localization

`RallyConfig` owns the enabled flag, retreat distance, clamping, and public
accessors. Both `en_us.json` and `pt_br.json` describe that the distance controls
when rallied guards abandon combat to follow the commander.

## Multi-Version Delivery Strategy

`main` is the canonical development branch and currently targets Minecraft
26.1. The release branches are `26.1`, `1.21.11`, and `1.21.1`.

Delivery proceeds feature slice by feature slice:

```text
Implement and test on main
        |
        v
Port to 26.1
        |
        v
Adapt to 1.21.11
        |
        v
Adapt to 1.21.1
        |
        v
Verify all supported builds before the next feature slice
```

Separate git worktrees should be used for the four branches to avoid repeated
checkout operations and accidental cross-version edits. Ports should preserve
behavior and tests while adapting mappings, event names, registry access,
entity methods, and Guard Villagers API calls.

Changes should be grouped into small portable commits:

1. policy, pure decision logic, and tests;
2. manual-order, AI-target, and damage enforcement;
3. Rally retreat behavior and configuration;
4. localization and documentation.

The exact commit boundaries may be combined when compilation requires it, but a
port must not leave a branch in a knowingly uncompilable state.

Profiles & Progression starts only after this update has been validated on all
three supported Minecraft versions.

## Testing Strategy

### Automated tests

Pure logic should cover at least:

- commander's own guard is protected;
- commander's own tameable/ownable entity is protected;
- an entity currently ridden or directly leashed by the commander is
  protected;
- an entity owned by a different player is not protected;
- an untamed entity is not protected;
- retreat disabled preserves combat with valid targets;
- retreat enabled preserves combat at or below the boundary;
- retreat enabled disengages above the boundary;
- a protected target always forces disengagement;
- dead or absent targets fall through to formation behavior.

Where Minecraft entity construction makes direct unit tests impractical, the
relationship extraction may remain version-specific while the resulting
decision table is represented by a small pure function that is unit tested.

### Build verification

Each branch must pass its normal Gradle test/build command independently.
Build success on `main` does not count as verification for a release branch.

### Manual gameplay matrix

On every supported Minecraft version, verify:

1. Accidentally hitting the commander's wolf does not cause hired guards to
   target or damage it.
2. A wolf or other protected companion that becomes involved in combat is not
   retaliated against by the commander's guards.
3. Manual attack orders against a protected companion are rejected.
4. Manual attack orders against a valid non-protected entity still work.
5. An entity owned by another player is not silently classified as the
   commander's protected companion.
6. A rallied guard continues fighting while within retreat distance.
7. Moving the commander beyond retreat distance clears combat and returns the
   guard to formation.
8. A retreating guard navigates normally when close enough.
9. A retreating guard uses the existing corrective teleport when beyond the
   configured formation teleport distance and teleporting is enabled.
10. With teleporting disabled, the guard disengages and attempts navigation
    without teleporting.
11. With retreat disabled, valid hostile combat retains the current behavior.
12. Waiting, patrol, and route orders remain unaffected.

Representative ownership tests should include a wolf, one rideable owned
animal, and a ridden or directly leashed Happy Ghast when that entity is
available in the target Minecraft version. A harnessed Happy Ghast with no
current rider or leash holder is explicitly expected to remain unclassified.
Species absent from an older version are not required for that branch.

## Acceptance Criteria

The update is complete only when:

- a hired guard cannot be manually ordered to attack the commander's protected
  living entity;
- native AI target selection cannot make a hired guard continue attacking that
  protected entity;
- resolved direct damage from the hired guard to that entity is cancelled;
- a rallied guard farther than the configured retreat distance abandons a valid
  combat target and resumes formation behavior;
- disabling automatic retreat preserves the former fight-until-combat-ends
  behavior without disabling ally protection;
- existing Rally, wait, patrol, route, and combat-order behavior remains valid;
- English and Portuguese configuration text is present;
- automated tests and Gradle builds pass on `main`, `26.1`, `1.21.11`, and
  `1.21.1`;
- the manual gameplay matrix has been completed for all supported versions.
