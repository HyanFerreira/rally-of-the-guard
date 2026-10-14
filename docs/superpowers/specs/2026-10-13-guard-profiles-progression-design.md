# Guard Profiles and Progression Design

## Objective

Turn each Guard Villager into a persistent individual with natural talent,
combat experience, moderate attribute growth, service statistics, and a hiring
value that reflects that history. The update should make guards memorable
without replacing Guard Villagers AI or turning them into overpowered units.

The server is authoritative for profile creation, combat contribution, XP,
attributes, statistics, and recruitment offers. Clients receive read-only
snapshots only when a screen needs them.

## Scope

This update includes:

- a versioned profile stored on each Guard Villager;
- Natural Quality, one-to-five-star presentation, and permanent natural rolls;
- levels 1 through 10 and ranks derived from level;
- moderate health, attack, and armor growth;
- server-side damage contribution, final-blow XP, kills, and assists;
- datapack tags for valid XP targets and threat categories;
- dynamic recruitment cost based on rank, level position, and quality;
- a Muster Roll item and read-only profile screen;
- safe initialization for new and existing worlds;
- public read APIs and guarded progression events;
- configuration, logging, automated tests, and dedicated-server validation.

The update does not include:

- traits;
- Captains or other offices;
- squads and squad assignment;
- promotions chosen by a player;
- additional formations or tactical orders;
- a global guard registry;
- raids survived, service time, memorials, or resurrection;
- arbitrary level caps, custom rank structures, or data-driven growth curves;
- editing a profile from the client.

Captain is deliberately deferred until Squads & Officers gives the office a
real command function. The profile format can gain offices through a later
schema migration without reserving an unused field now.

## Delivery Shape

The feature is one release developed as independently verifiable milestones:

```text
Profile Core
    -> Natural Identity
    -> Attribute Progression
    -> Combat Progression
    -> Dynamic Recruitment
    -> Muster Roll
    -> Hardening and Balance
```

The canonical implementation targets `main`, currently Minecraft 26.1. Ports
to maintained release branches follow after the canonical behavior and schema
are stable; version-specific adapters may change mappings and hooks but not the
rules in this specification.

## Persistent Profile

### Ownership and storage

The profile belongs to the Guard Villager and is stored with that entity under
the namespaced compound `rallyguard:profile`. A global UUID registry is not
introduced in this update.

Production code accesses data through `GuardProfileRepository`; it must not
read or write the compound directly. A small `GuardEntity` mixin implements an
internal `GuardProfileHolder` and bridges entity save/load methods. If the
target platform provides a mature entity-attachment API, the storage adapter
may use it without changing the repository or domain model.

### Schema version 1

The logical persisted structure is:

```text
rallyguard:profile {
    schema_version: 1,
    generation_version: 1,
    origin: "natural" | "legacy" | "command" | "other",
    total_xp_milli: long,
    natural_quality: int,
    health_roll: int,
    attack_roll: int,
    armor_roll: int,
    speed_roll: int,
    kills: long,
    assists: long
}
```

Constraints:

- `total_xp_milli`, `kills`, and `assists` are non-negative;
- `natural_quality` is between 0 and 10,000 inclusive;
- natural rolls are between -10,000 and 10,000 inclusive;
- `schema_version` governs structural migrations;
- `generation_version` identifies the natural-generation algorithm.

Profile Core temporarily wrote generation version `1` with neutral quality
and rolls. Natural Identity uses generation version `2`. The exact untouched
generation-1 legacy placeholder is upgraded once with UUID-seeded generation;
all other profiles preserve their stored natural values.

Level, rank, stars, attributes, XP-to-next-level, and hiring price are derived
and are not persisted. The entity UUID is already the profile identity and is
not duplicated in the compound.

XP uses integer thousandths:

```text
1 XP = 1000 XP milli
3.6 XP = 3600 XP milli
```

This prevents floating-point drift during repeated fractional awards.

### Initialization

New server-side spawns create a profile before normal gameplay can observe the
guard. The initializer classifies the origin, generates quality and rolls once,
chooses the eligible initial level, converts that level to cumulative XP, and
applies attributes.

An entity loaded without a profile is treated as `legacy`. It receives:

- level 1, represented by zero cumulative XP;
- newly generated quality and natural rolls;
- zero kills and assists.

This prevents an update from randomly promoting guards that already existed in
a world. Reloading never rerolls an existing field.

The full natural-level distribution applies only when a trustworthy spawn
reason identifies natural, village/structure, or non-exploitable Guard
Villagers generation. Legacy entities, commands, spawn eggs, spawners,
administrative tools, and unknown origins start at level 1. Ambiguous origins
use the conservative level-1 policy.

### Validation, repair, and migration

Loading clamps negative counters and out-of-range quality or rolls. Missing
fields in a known schema are filled through a deterministic migration. Existing
natural values are never rerolled merely because another field is missing.

An unreadable version-1 compound is rebuilt conservatively as a level-1 legacy
profile with new natural rolls and zero statistics. The repair is logged once
at `warn` level.

Migrations are explicit and sequential (`v1 -> v2 -> v3`). A schema version
newer than the running mod is not overwritten. Progression is suspended for
that guard, its Guard Villagers behavior continues normally, and the server
logs a warning.

Disabling progression or natural variation never deletes persisted data.

## Natural Identity

### Natural Quality

Quality is the average of two independent uniform samples from 0 through
10,000, rounded to the nearest integer:

```text
quality = roundHalfUp((sampleA + sampleB) / 2)
```

This triangular distribution makes middle values common and extremes rare.

| Quality | Stars | Approximate frequency |
|---:|---:|---:|
| 0-1999 | 1 | 8% |
| 2000-3999 | 2 | 24% |
| 4000-5999 | 3 | 36% |
| 6000-7999 | 4 | 24% |
| 8000-10000 | 5 | 8% |

Stars are visible in the Muster Roll before recruitment. They represent talent,
not experience.

### Attribute rolls

For each attribute, generate an independent noise value as the average of two
uniform samples from -1.0 through +1.0. Convert quality to a -1.0 through +1.0
bias and calculate:

```text
qualityBias = (quality - 5000) / 5000
roll = clamp(0.55 * qualityBias + 0.65 * attributeNoise, -1.0, 1.0)
storedRoll = roundHalfUp(roll * 10000)
```

Health, attack, armor, and speed use separate noise samples. High quality
improves the overall tendency but does not guarantee that every attribute is
positive; low-quality guards can still possess an individual strength.

Map the signed roll linearly to these asymmetric ranges:

| Attribute | Minimum | Maximum |
|---|---:|---:|
| Max health | -8% | +10% |
| Attack damage | -8% | +10% |
| Armor | -1 | +2 |
| Movement speed | -2% | +2% |

A roll of -0.5 maps halfway toward the negative bound; +0.5 maps halfway
toward the positive bound.

### Natural initial levels

Eligible natural guards use integer weights totaling 1,000:

| Level | Chance | Weight |
|---:|---:|---:|
| 1 | 45% | 450 |
| 2 | 25% | 250 |
| 3 | 14% | 140 |
| 4 | 8% | 80 |
| 5 | 4% | 40 |
| 6 | 2% | 20 |
| 7 | 1% | 10 |
| 8 | 0.6% | 6 |
| 9 | 0.3% | 3 |
| 10 | 0.1% | 1 |

The selected level is encoded by assigning exactly the cumulative XP threshold
for that level.

## Levels and Ranks

The fixed version-1 curve is:

| Level | XP for transition | Cumulative XP |
|---:|---:|---:|
| 1 | - | 0 |
| 2 | 20 | 20 |
| 3 | 30 | 50 |
| 4 | 45 | 95 |
| 5 | 60 | 155 |
| 6 | 80 | 235 |
| 7 | 105 | 340 |
| 8 | 135 | 475 |
| 9 | 170 | 645 |
| 10 | 210 | 855 |

Level is derived from cumulative XP. XP above 855 is retained while level is
capped at 10. One award may cross multiple levels. The progression API emits
each transition, while presentation may consolidate several transitions into
one message.

Ranks are stable enum values with localized display names:

| Level | Rank enum | English display |
|---:|---|---|
| 1-2 | `RECRUIT` | Recruit |
| 3-4 | `GUARDSMAN` | Guardsman |
| 5-6 | `SOLDIER` | Soldier |
| 7-8 | `VETERAN` | Veteran |
| 9-10 | `ELITE` | Elite |

## Attribute Progression

Level bonuses are cumulative:

| Level | Health | Attack | Armor |
|---:|---:|---:|---:|
| 1 | +0 | +0 | +0 |
| 2 | +1 | +0 | +0 |
| 3 | +1 | +0.25 | +0 |
| 4 | +2 | +0.25 | +1 |
| 5 | +2 | +0.50 | +1 |
| 6 | +3 | +0.75 | +1 |
| 7 | +4 | +1.00 | +2 |
| 8 | +5 | +1.25 | +2 |
| 9 | +6 | +1.50 | +3 |
| 10 | +8 | +2.00 | +3 |

Movement speed does not grow with level.

Stable Rally modifier identifiers distinguish natural and level contributions:

```text
rallyguard:natural_health
rallyguard:natural_attack
rallyguard:natural_armor
rallyguard:natural_speed
rallyguard:level_health
rallyguard:level_attack
rallyguard:level_armor
```

Natural health is percentage-based. Natural attack is percentage-based over
the effective attack attribute so equipped weapons remain relevant. Natural
speed is percentage-based over base speed. Natural armor and all level bonuses
are additive.

Applying a profile first removes every Rally modifier and then installs the
current set. Repeated application is idempotent. Guard Villagers base values
are never replaced.

The derived modifiers are stored as permanent entity attribute modifiers with
stable IDs, then rebuilt from the profile on load. Persisting the previous
maximum health lets the load callback preserve the guard's health percentage;
the profile remains authoritative and stale Rally modifiers are replaced.

Health adjustment depends on the cause:

- initialization, load, and configuration refresh preserve health percentage;
- level-up adds the positive max-health delta to current health;
- a reduced maximum clamps current health to the new maximum;
- applying an unchanged profile does not heal the guard.

The Muster Roll shows effective server-side values including equipment and
other active modifiers. Optional tooltips separate natural and level portions.

## Combat Contribution

### Eligibility and budgets

The mod supplies entity-type tags:

```text
#rallyguard:xp_targets
#rallyguard:no_xp
#rallyguard:xp/common
#rallyguard:xp/dangerous
#rallyguard:xp/raid
#rallyguard:xp/elite
#rallyguard:xp/raid_elite
#rallyguard:xp/boss
```

`no_xp` always wins. Players and Guard Villagers never grant XP. A target must
belong to `xp_targets`; passive entities are absent from the defaults. A target
without a category uses `common`. Category priority is highest budget first:
boss, raid elite, elite, raid, dangerous, common.

| Category | Base XP |
|---|---:|
| Common | 5 |
| Dangerous | 7 |
| Raid | 8 |
| Elite | 10 |
| Raid Elite | 18 |
| Boss | 30 |

The shipped datapack classifies supported vanilla targets explicitly. Modpack
authors opt other entities in through datapacks; the implementation does not
automatically reward every class implementing a hostile marker.

### Ledger

Each active victim has an in-memory server ledger containing its UUID, total
effective damage, last-damage game time, and per-guard effective damage with
last-damage time. The total includes all sources, not only guards.

Only health actually removed counts:

```text
effectiveDamage = max(0, healthBefore - healthAfter)
```

Blocked damage, cancelled damage, zero damage, and overkill beyond remaining
health do not count. Environmental damage contributes to the denominator but
has no guard participant.

Resolve a guard attacker from the causing entity first, then the projectile
owner, then a direct entity that is itself a guard. Unattributed fire, falling,
lava, and other environmental damage have no participant.

Every profiled guard may progress, whether hired or natural. A guard must still
be alive and loaded when the victim dies to receive an award. The update does
not create a global deferred-award registry.

The entire ledger expires after 30 seconds with no damage to the victim. It is
also removed on death, removal, unload, or server shutdown. Cleanup iterates
only active ledgers and never scans all entities or guards.

### XP distribution

When a guard delivers the final blow:

```text
contributionXP = budget * 0.80 * guardDamage / totalEffectiveDamage
finalBlowXP = budget * 0.20 for the final guard only
```

When the final blow does not belong to a guard, the final-blow portion returns
to contribution:

```text
contributionXP = budget * guardDamage / totalEffectiveDamage
```

The global XP multiplier applies to the budget before distribution. Individual
awards are calculated in XP milli and rounded down. The sum of awards must
never exceed the post-multiplier budget.

Example:

```text
Common target: 5 XP
Guard A: 15 of 20 effective damage
Guard B: 5 of 20 effective damage and final blow

Guard A = 5 * 0.80 * 0.75 = 3 XP
Guard B = 5 * 0.80 * 0.25 + 5 * 0.20 = 2 XP
```

### Kills and assists

A loaded profiled guard receives a kill when it is responsible for valid fatal
damage. The killer does not also receive an assist.

A different guard receives an assist only when both are true:

```text
effective guard damage >= 1.0
guard damage / total effective damage >= 0.05
```

Any positive contribution may earn fractional XP even when it does not qualify
as an assist.

The client never reports damage, XP, kills, assists, or level transitions.
Spawner farms and repeatable hostile encounters are not specially blocked in
version 1; servers can exclude problematic entities with `no_xp`.

## Dynamic Recruitment

### Price formula

Rank bands are:

| Rank | Minimum | Maximum |
|---|---:|---:|
| Recruit | 1 | 3 |
| Guardsman | 2 | 4 |
| Soldier | 4 | 6 |
| Veteran | 6 | 9 |
| Elite | 9 | 13 |

Within a two-level rank, the first level has position 0 and the second has
position 1. Quality is normalized from 0 through 1:

```text
progress = (levelPosition + naturalQuality / 10000) / 2
rawCost = rankMinimum + (rankMaximum - rankMinimum) * progress
cost = roundHalfUp(rawCost * dynamicHireCostMultiplier)
cost = clamp(cost, dynamicHireCostMinimum, dynamicHireCostMaximum)
```

Higher quality or level never lowers price. `economy.hireItem` remains the
currency. When dynamic cost is disabled, the existing `economy.hireCost` is
used unchanged and does not participate in the dynamic formula.

`GuardHiringValueService` is the single source for screens, presentation, and
recruitment. External recruitment policies remain able to deny or replace an
offer.

### Quote integrity

The server returns a quote containing the payment item, count, and a
fingerprint derived from guard UUID, profile XP, item, and cost. Confirmation
re-evaluates server policies and the current profile. If the result differs,
nothing is charged, the new quote is shown, and the player must confirm again.

The recruitment service exposes conceptually separate operations:

```text
quote(player, guard)
recruit(player, guard, expectedQuote)
```

Quoting may evaluate policies but cannot reserve transactions, consume items,
change ownership, or create permanent side effects. A changed quote produces
the controlled result `OFFER_CHANGED`.

## Muster Roll

### Item and interaction

The Muster Roll is permanent, non-consumable, and has no durability. Its
shapeless recipe uses a book, paper, and feather.

Using it on a Guard Villager asks the server to open that guard's profile. The
server verifies that the guard is alive, in the same dimension, no farther than
8 blocks, and that the player still holds a Muster Roll. Any player may inspect
an unowned guard, their guard, or another player's guard. Profiles are public
world information in this release.

The item inspects one targeted guard. A remote or global roster is deferred.

### Screen content

The screen shows:

- display name;
- stars;
- rank and level;
- XP within the current level and XP required;
- effective max health, attack, armor, and movement speed;
- kills and assists;
- service status;
- payment item and current offer for an unowned guard.

An owned guard shows `Under your command` for its owner or `Already in
service` for another player. Squad, role, Captain, raids survived, and other
future fields are omitted rather than shown as placeholders.

Display name uses custom name first, Guard Villagers name second, and a
localized generic Guard label last. The Muster Roll does not rename entities.

The Commander's Ledger may show compact level and rank, but XP, stars,
attributes, and service statistics remain exclusive to the Muster Roll.

### Server snapshot

The server sends a bounded, read-only `GuardProfileSnapshot` containing:

```text
entity id and UUID
display name
level, rank, and stars
XP into level and XP required
effective attributes
kills and assists
service status
optional hiring offer
```

It does not expose rolls, schema data, origin, generation version, or mutable
profile state.

While the screen is open, the server validates the session once per second and
sends a replacement snapshot only when its contents change. It closes the
screen if the entity dies, disappears, changes dimension, or moves beyond the
allowed distance. Network fields and text components have explicit bounds.

The built-in rank is a fallback presentation contribution. An explicit
external `GuardPresentationRegistry` provider may override it without mutating
the profile.

## Configuration

Logical configuration keys are:

```text
progression.enabled = true
progression.naturalVariationEnabled = true
progression.statGrowthEnabled = true
progression.advancedNaturalGuardsEnabled = true
progression.xpMultiplier = 1.0
progression.combatTimeoutSeconds = 30
progression.damageContributionShare = 0.80
progression.finalBlowShare = 0.20
progression.assistMinimumDamage = 1.0
progression.assistMinimumShare = 0.05

economy.dynamicHireCostEnabled = true
economy.dynamicHireCostMultiplier = 1.0
economy.dynamicHireCostMinimum = 1
economy.dynamicHireCostMaximum = 999
```

Public Java field names follow existing MidnightLib conventions; dotted names
describe categories. Values are clamped or replaced with safe defaults after
loading. Contribution and final-blow shares are normalized to sum to 1.0.
Invalid configuration produces one warning, not per-tick logs.

When progression is disabled, no XP is awarded and no level modifier is
applied. Natural Quality remains inspectable. Disabling natural variation or
stat growth removes only the matching modifiers while retaining profile data.
Re-enabling a feature reconstructs its state from the stored profile.

Version 1 does not expose maximum level, rank boundaries, XP thresholds, or
growth tables as independent settings because arbitrary combinations would
break invariants and multiply the migration surface.

## Public API

Public contracts are grouped into profile, progression, recruitment, and
presentation packages. `GuardProfileView` is immutable and read-only. Writes
go through `GuardProgressionService` operations such as awarding XP, recording
statistics, and refreshing attributes.

Events include:

```text
PROFILE_INITIALIZED
BEFORE_XP_AWARDED
AFTER_XP_AWARDED
LEVEL_CHANGED
COMBAT_STAT_RECORDED
HIRING_VALUE_CALCULATED
```

`BEFORE_XP_AWARDED` may allow, deny, or replace a proposed award. Every result
is validated before mutation. Other events are observational unless their
contract explicitly says otherwise. Listeners run on the logical server
thread, cannot access mutable persistence objects, and are isolated so an
exception does not crash the server.

## Components and Responsibilities

- `GuardProfile` is the validated persisted domain value.
- `GuardProfileRepository` creates, reads, migrates, and stores profiles.
- `GuardProfileInitializer` classifies spawn origin and initializes once.
- `NaturalAttributeModel` performs deterministic quality/roll conversion.
- `GuardLevelModel` derives level, rank, thresholds, and level bonuses.
- `GuardAttributeApplier` owns stable, idempotent attribute modifiers.
- `CombatContributionTracker` owns short-lived damage ledgers.
- `XpTargetPolicy` resolves tags, category, eligibility, and budget.
- `GuardProgressionService` awards XP and records statistics.
- `GuardHiringValueService` calculates and quotes recruitment offers.
- `GuardProfileSnapshotFactory` creates bounded screen DTOs.
- Muster Roll networking validates sessions and transmits snapshots.

No UI, network receiver, event callback, or mixin reproduces domain formulas.

## Runtime Flows

### Entity initialization

```text
Guard enters server world
        |
        v
Profile present? -- yes --> validate/migrate --> apply attributes
        |
        no
        |
        v
New trusted spawn? -- yes --> natural policy
        |
        no
        |
        v
Legacy level-1 policy
        |
        v
Persist profile --> apply attributes
```

### Combat reward

```text
Effective damage occurs
        |
        v
Update victim ledger
        |
        v
Victim dies --> valid XP target?
                  |          |
                 no         yes
                  |          |
               cleanup   resolve budget
                              |
                              v
                    divide contribution/final blow
                              |
                              v
                    award XP and record stats
                              |
                              v
                    apply level changes --> cleanup
```

### Recruitment

```text
Open hiring screen --> server quote --> display
                                           |
                                           v
                                      player confirms
                                           |
                                           v
                                 server recomputes quote
                                    |              |
                                 changed          same
                                    |              |
                               refresh UI     transact atomically
```

## Error Handling and Observability

Normal profile initialization, XP calculation, level-up, modifier refresh, and
offer calculation use `debug` logging. Profile repairs, invalid configuration,
future schemas, category conflicts, quote changes, and rejected API values use
`warn`. Migration or persistence failures and external-listener exceptions use
`error`.

Logs focus on transitions and exceptional conditions. No damage event or tick
produces normal-level log spam.

If profile functionality fails for one guard, the guard must retain normal
Guard Villagers AI. Network validation failures close or reject the relevant
screen operation rather than disconnecting the server.

## Performance and Threading

All mutation happens on the logical server thread. Combat state exists only
for active victims. Cleanup walks the active-ledger collection periodically;
it never performs a world-wide entity search. Damage does not force an
immediate disk write, and idle guards have no progression tick.

Muster Roll polling exists only for open screens, runs once per second, and
sends data only when the snapshot changes.

## Testing Strategy

### Unit tests

Pure tests cover:

- profile validation and each schema migration;
- quality and star boundaries;
- statistical sanity of the triangular quality distribution;
- roll-to-attribute conversion at minimum, zero, and maximum;
- natural-level weights and cumulative XP assignment;
- every XP threshold and rank boundary;
- multi-level awards and XP beyond level 10;
- modifier descriptions and idempotent decisions;
- target-category priority;
- XP division, rounding, and budget conservation;
- final blows by guards and non-guards;
- kill and assist boundary conditions;
- dynamic price monotonicity and exact band boundaries;
- configuration validation and fallback behavior;
- quote fingerprints and changed-offer decisions.

### Integration and game tests

Integration coverage includes:

- new and legacy entity initialization;
- save/reload without rerolling;
- corrupted-profile repair and future-schema preservation;
- attribute application and removal;
- health handling on load, level-up, and configuration change;
- melee and projectile attribution;
- blocked, cancelled, environmental, and overkill damage;
- ledger timeout, death, unload, and cleanup;
- datapack inclusion, exclusion, and reload;
- quotation, payment, rollback, ownership, and offer changes;
- Muster Roll distance, lifecycle, and snapshot refresh;
- dedicated-server startup without client classes.

### Manual validation

Before release, validate a new world and a copy of an older save in single
player and on a dedicated server. Exercise natural and legacy guards, melee and
ranged combat, multiple contributors, open Muster Roll screens during combat,
fixed and dynamic hiring, configuration toggles, custom tags, reloads, entity
unloads, deaths, and two-player ownership scenarios.

## Implementation Milestones

1. **Profile Core:** model, codec, holder, repository, initialization,
   validation, migrations, and tests.
2. **Natural Identity:** quality, rolls, stars, origins, natural levels, and
   tests.
3. **Attributes:** stable modifiers, health rules, configuration refresh, and
   tests.
4. **Combat Progression:** tags, ledgers, attribution, XP, statistics,
   level-up, API events, and tests.
5. **Dynamic Recruitment:** value service, quotes, fingerprints, atomic
   confirmation, API integration, and tests.
6. **Muster Roll:** item, recipe, screen handler, bounded payloads, snapshot
   refresh, localization, and tests.
7. **Hardening:** old-save and dedicated-server validation, datapack examples,
   documentation, changelog, performance checks, and initial balance pass.

Every milestone must end with the relevant automated tests, a full build, diff
review, and matching documentation. A milestone must not leave the main branch
knowingly uncompilable.

## Documentation Ownership

This specification is normative for Guard Profiles & Progression. The root
`ROADMAP.md` remains the long-term product direction. The older
`docs/ROADMAP.md` should be removed or clearly archived when implementation
documentation is updated so contributors cannot mistake it for the current
plan. `docs/MELHORIAS_TECNICAS.md` requires a separate accuracy pass because
several items describe an older Java/build/test state.

## Acceptance Criteria

The update is complete only when:

- every initialized guard has exactly one valid, versioned profile;
- existing saves load without manual migration and legacy guards start at
  level 1;
- natural values survive save/reload and are never unintentionally rerolled;
- repeated attribute refreshes never duplicate modifiers or heal guards;
- progression remains server-authoritative and clients cannot submit profile
  mutations;
- XP distribution never exceeds the configured victim budget;
- melee and supported projectile contribution resolve to the correct guard;
- level, rank, kills, and assists persist and derive consistently;
- target tags can include, categorize, and exclude entities through datapacks;
- higher level or quality never lowers a dynamic hiring price;
- recruitment never charges an offer different from the one confirmed;
- the Muster Roll works for owned and unowned guards and closes when its server
  validation fails;
- disabling features removes their runtime effects without deleting profiles;
- public APIs preserve profile invariants when listeners fail or return invalid
  data;
- unit, integration, and full Gradle builds pass;
- the dedicated-server and old-save manual matrices pass;
- English and Portuguese localization, user documentation, API documentation,
  and changelog are updated.
