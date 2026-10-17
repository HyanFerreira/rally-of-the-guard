# Rally of the Guard API

Rally exposes a small common-side API so integrations can issue tactical orders without simulating client packets or depending on networking implementation classes.

## Compatibility

- Mod version: `1.4.0`
- API contract version: `8`
- Entry point: `net.hfstack.rallyguard.api.RallyGuardApi`

Integrations should check `RallyGuardApi.apiVersion()` before relying on a newer contract. Additive changes may keep the same contract version; incompatible changes require a new integer version and a coordinated minimum mod dependency.

## Raid defense integration

Village Defense observes vanilla raid objects; it does not expose or persist a second raid definition. Opportunity and response payloads are implementation details rather than a public API contract. Server validation binds a response to the player's active 30-second session and repeats raid ID, dimension, center, 128-block distance, ownership, and loaded-guard checks before issuing orders.

Manual and automatic reactions reuse `GuardCommandService.summon` and `GuardCommandService.defendArea`, so registered eligibility and tactical-order policies remain authoritative. Only loaded guards in the commander's current dimension are candidates, and safe-post resolution never loads a chunk. The raid defense radius follows vanilla `Raid.VILLAGE_RADIUS_BUFFER` (16 blocks in Minecraft 26.1).

Report sessions are isolated by player and raid, expire after 30 minutes, and consume the existing combat-contribution ledger without estimating unavailable facts. Guard or relevant entity unloads produce a partial-data disclosure. No new public mutation surface or API version increment is introduced by this feature.

## Guard profiles

Obtain the read-only profile facade and look up a loaded guard with:

```java
Optional<GuardProfileView> profile = RallyGuardApi.profiles().find(guard);
```

`GuardProfileView` exposes schema and generation versions, origin, cumulative
XP, natural quality and rolls, kills, assists, and the persisted `captain()`
flag. It also derives `level()`,
`rank()`, `qualityStars()`, `xpIntoLevelMilli()`, and
`xpForNextLevelMilli()` without mutating the guard. XP uses integer thousandths
(`1000` XP milli equals `1` XP). Quality is bounded from `0` through `10,000`;
each natural roll is bounded from `-10,000` through `10,000`; XP, kills, and
assists are non-negative.

The lookup is side-effect free: it never initializes, repairs, migrates, or
persists a profile. It returns an empty result when no profile is attached or
when the stored schema is newer than this version of Rally. Rank values expose
stable enum names and translation keys; localized display names remain a
presentation concern.

Profiles use schema version `2`; schema-1 profiles migrate once with
`captain() == false`, so existing guards are never promoted by migration.
`captain()` is a default API method returning `false`, preserving compatibility
with existing third-party implementations of the view.

Natural identities use generation version `3`. Generation version `1` denotes
the neutral placeholder written by the earlier Profile Core milestone. Rally
migrates that exact placeholder once using a deterministic UUID-derived seed,
so old saves gain quality and natural rolls without changing again on reload.
Generation version `2` identifies natural profiles created before the Captain
roll was introduced; they remain non-Captains and are not rerolled.

The built-in rank is also used as the fallback rank label in Rally guard lists.
Captain is a separate persisted role, not a rank. Presentation providers still
take precedence for command-list labels without changing either field.

Natural and level attribute modifiers use stable `rallyguard:*` identifiers
and are reconstructed from the profile. They are implementation details rather
than a public mutation surface; integrations should continue treating
`GuardProfileView` as read-only.

## Captain promotion and demotion

API contract version 5 introduced the only supported Captain mutation path:

```java
CaptainService captains = RallyGuardApi.captains();
CaptainActionView view = captains.inspect(owner, guard);
CaptainMutationResult promoted = captains.promote(owner, guard);
CaptainMutationResult demoted = captains.demote(owner, guard);
```

These methods must run on the logical server thread. `inspect` returns the
current server-authoritative action, eligibility state, blocking outcome,
configured minimum level, and optional promotion offer. Integrations must not
edit profile attachments or call the internal profile repository to change the
Captain flag.

Promotion requires a living loaded guard in the owner's dimension, matching
ownership, a supported writable profile, the configured minimum level
(Level 7 by default), and the current payment (4 units of
`economy.hireItem` by default). The service revalidates after policies run,
removes payment immediately before persistence, and refunds the complete offer
when persistence fails. Demotion has no payment or refund and remains available
when promotion is disabled, but an active Squad Officer must be cleared first.

`CaptainMutationResult.Outcome` distinguishes `SUCCESS`, `DISABLED`,
`NOT_AVAILABLE`, `NOT_OWNER`, `UNSUPPORTED_PROFILE`, `ALREADY_CAPTAIN`,
`NOT_CAPTAIN`, `LEVEL_TOO_LOW`, `INSUFFICIENT_PAYMENT`, `ACTIVE_OFFICER`,
`PERSISTENCE_ERROR`, and `DENIED`. Promotion success includes its applied
`CaptainOffer`; demotion success does not.

`CaptainEvents.BEFORE` is an ordered policy event. The first denial stops
evaluation and supplies localized feedback. `CaptainEvents.AFTER` runs only
after successful persistence; observer exceptions are logged and isolated so
one integration cannot turn a completed mutation into a reported failure.

## Guard progression

Progression writes are available on the logical server through:

```java
GuardProgressionService progression = RallyGuardApi.progression();
long awarded = progression.awardXp(guard, proposedXpMilli);
progression.recordCombatStat(guard, CombatStat.ASSIST);
progression.refreshAttributes(guard);
```

`awardXp` returns the amount actually persisted. `recordCombatStat` and
`refreshAttributes` return `false` when the guard has no writable supported
profile. Callers should never edit profile attachments directly.

`GuardProgressionEvents.BEFORE_XP_AWARDED` receives the current proposed award
in registration order. A listener may allow it, replace it with another
non-negative milli-XP amount, or deny it. Listener failures are logged and
isolated. The observational `AFTER_XP_AWARDED`, `LEVEL_CHANGED`, and
`COMBAT_STAT_RECORDED` events run only after successful persistence; failures
in one observer do not prevent later observers.

Default combat XP eligibility and budgets are datapack-controlled through
`#rallyguard:xp_targets`, `#rallyguard:no_xp`, and the six
`#rallyguard:xp/*` category tags. `no_xp` takes precedence, and players and
Guard Villagers are always excluded.

## Muster Roll snapshots

The built-in Muster Roll uses `GuardProfileSnapshotFactory` on the logical
server to create a bounded, immutable display snapshot. It contains only the
guard identity, derived level/rank/stars, Captain status, XP progress, effective attributes,
kills, assists, viewer-relative service status, and an optional current hiring
offer and, for a guard under the viewer's command, an optional Captain action
view. Schema versions, origin, natural quality value, and attribute rolls are
deliberately absent from the wire contract.

Snapshots are refreshed at most once per second and sent only after their
contents change. The inspection session requires the guard to remain alive,
loaded, in the viewer's dimension and within eight blocks while the viewer
continues holding a Muster Roll. Integrations that need raw read-only profile
data should continue using `RallyGuardApi.profiles()` rather than depending on
the screen payload.

## Guard commands

Obtain the shared service with:

```java
GuardCommandService commands = RallyGuardApi.guardCommands();
```

The command contract exposes:

- `summon`
- `follow`
- `wait`
- `patrol`
- `stopPatrol`
- `moveTo`
- `defendArea`
- `setCombatStance`

Every method must run on the logical server thread. The service validates that the guard is alive, loaded in the commander's world and tactically owned by that commander. It returns a `GuardCommandResult` with a machine-readable outcome and localized player feedback.

API version 6 adds `moveTo(ServerPlayer, GuardEntity, BlockPos)` and
`GuardCommandType.MOVE_TO`. A successful call replaces a supported move order,
clears mutually exclusive tactical state, and persists the requested target.
An attachment written by a future schema returns `UNSUPPORTED_DATA` and remains
untouched; another explicit mutually exclusive order may still clear it.

API version 7 adds
`defendArea(ServerPlayer, GuardEntity, BlockPos center, BlockPos post, int radius)`
and `GuardCommandType.DEFEND_AREA`. The server integration chooses safe posts and
the accepted radius before calling the service. A successful call activates the
native patrol at the post, clears mutually exclusive tactical state, and stores
the territorial order. Future Defend Area schemas use the same
`UNSUPPORTED_DATA` preservation rule as Move Here.

API version 8 adds
`setCombatStance(ServerPlayer, GuardEntity, GuardCombatStance)` and
`GuardCommandType.SET_COMBAT_STANCE`. `GuardCombatStance` is a public enum in
`net.hfstack.rallyguard.api.command` with stable wire IDs for `DEFENSIVE`,
`AGGRESSIVE`, `PASSIVE`, `HOLD_POSITION`, and `PROTECT_COMMANDER`. The service
captures the server-side guard position as the `HOLD_POSITION` anchor. Future
stance schemas return `UNSUPPORTED_DATA` and remain untouched.

Rally's own packet receiver uses this same service. Integrations must call the service directly on the server and must not construct Rally network payloads.

## Squads and Officers

API version 4 exposes Squad operations through:

```java
SquadService squads = RallyGuardApi.squads();
List<SquadView> definitions = squads.list(player);
GuardSquadView assignment = squads.find(player, guard);
```

`SquadView` is immutable and contains the stable Squad UUID, display name, and
optional Officer guard UUID. `GuardSquadView` contains the guard's effective
Squad and whether that guard is its Officer. A stale assignment whose Squad no
longer exists is returned as Unassigned and is repaired when writable.

Mutations use `create`, `rename`, `delete`, `assign`, and `setOfficer`. They must
run on the logical server thread and return `SquadMutationResult`; callers must
not edit Fabric attachments directly. The default service enforces tactical
ownership, unique case-insensitive names, configured limits, supported schemas,
and the rule that an Officer is a living loaded Captain already assigned to the
same Squad.

Squad definitions persist on the player. A guard persists only its optional
Squad UUID, so this API does not provide a global member registry. `list` and
Ledger commands operate on guards currently loaded in the commander's dimension.
Deleting a Squad never deletes guards or changes their active tactical orders.

### Tactical-order events

Every validated command exposed by `GuardCommandService` passes through `GuardCommandEvents.BEFORE` before Rally mutates guard state. Policies run in registration order and may allow the order or deny it with localized feedback:

```java
GuardCommandEvents.BEFORE.register(context -> {
    if (!mayIssueOrder(context.commander(), context.guard(), context.command())) {
        return GuardCommandDecision.deny(Text.translatable("example.command.denied"));
    }
    return GuardCommandDecision.allow();
});
```

The immutable context identifies the commander, guard, command type, server world, original guard position and the optional target position used by positional orders. Invalid or unauthorized base requests are rejected before policies run.

`GuardCommandEvents.AFTER` runs only after the tactical state has changed successfully. It is intended for progression, reports and higher-level coordination. AFTER listeners must observe the completed order and must not apply it a second time. Listener failures are logged; a BEFORE failure rejects the order safely, while an AFTER failure does not roll back a completed tactical mutation.

Route, rally-group and attack-target operations will join this surface in later Phase 0 increments after their validation rules are separated from packet-specific input.

## Guard eligibility

Integrations may register a structured policy through `GuardEligibilityEvents.CHECK`. The context identifies the player, guard, server world and one of four operations:

- `RECRUIT`
- `JOIN_RALLY`
- `ISSUE_COMMAND`
- `SHOW_IN_COMMAND_LIST`

Command checks also expose the `GuardCommandType` and optional target position. Policies run in registration order; the first `GuardEligibilityDecision.Deny` stops evaluation and carries localized feedback:

```java
GuardEligibilityEvents.CHECK.register(context -> {
    if (!belongsToCommander'sSettlement(context)) {
        return GuardEligibilityDecision.deny(Text.translatable("example.guard.wrong_settlement"));
    }
    return GuardEligibilityDecision.allow();
});
```

Rally evaluates these callbacks only after its base entity and ownership checks. A policy exception fails closed for that guard and is logged. Without listeners, every guard that passes Rally's built-in rules remains eligible.

Visibility denials remove the guard from the command list. Rally denials exclude it from a newly activated rally. Recruitment and command denials return the supplied feedback to the player.

## Guard presentation

`GuardPresentationRegistry` accepts read-only `GuardPresentationProvider` implementations. Providers run on the logical server while Rally builds the command-list snapshot and may contribute localized rank and settlement labels:

```java
GuardPresentationRegistry.register(context -> GuardPresentation.of(
        Text.translatable("example.rank.captain"),
        Text.literal(settlementName(context.guard()))
));
```

Contributions are merged in registration order; the first non-empty value for each label wins. Provider failures are logged and ignored. The resolved `Text` values are encoded in the server payload and rendered below the guard name, preserving client-side localization and text style. No provider is required, and providers cannot mutate Rally tactical state through this interface.

## Recruitment

The shared recruitment service is available through:

```java
GuardRecruitmentService recruitment = RallyGuardApi.guardRecruitment();
```

API version 3 adds server-authoritative quotes:

```java
RecruitmentQuoteResult result = recruitment.quote(player, guard);
if (result.available()) {
    RecruitmentResult recruited = recruitment.recruit(
        player,
        guard,
        result.quote().orElseThrow()
    );
}
```

A quote fingerprints the guard UUID, profile XP, payment item, and final
policy-adjusted cost. Confirmation re-evaluates the current profile and every
policy. A mismatch returns `OFFER_CHANGED` before inventory payment or
transaction reservation. Callers should show the refreshed quote and require
another confirmation. The two-argument `recruit` method remains available for
trusted server-side flows that do not need an interactive confirmation gap.

The default offer comes from `RallyGuardApi.hiringValues()`. Dynamic pricing
uses rank, level position, and natural quality; disabling it restores the
configured static cost. `GuardHiringValueEvents.CALCULATED` observes each
calculation without replacing the offer. Recruitment BEFORE policies retain
authority to deny or replace that default offer.

`recruit(player, guard)` validates that the guard is alive, unowned, in the player's world and within interaction distance. Rally then builds the configured default `RecruitmentOffer`, runs every `GuardRecruitmentEvents.BEFORE` policy, reserves contributed transactions, collects the final personal inventory payment and assigns tactical ownership.

Before policies run in registration order. A listener may return a replacement offer for the next policy or deny recruitment with localized feedback:

```java
GuardRecruitmentEvents.BEFORE.register((context, offer) -> {
    if (!mayRecruit(context.player(), context.guard())) {
        return RecruitmentDecision.deny(Text.translatable("example.recruitment.denied"));
    }
    return RecruitmentDecision.allow(offer);
});
```

`GuardRecruitmentEvents.AFTER` runs only after successful ownership assignment. It is intended for settlement assignment, starting rank and military records. AFTER listeners must observe the completed recruitment and must not attempt to assign ownership again.

API version 2 adds `RecruitmentTransaction` for institutional payments and capacity reservations. A BEFORE policy returns `RecruitmentDecision.transactional(offer, transaction)`. Rally calls `reserve()` after final validation, `commit()` after ownership assignment, and `rollback()` in reverse policy order if reservation, payment, ownership or commit fails. Transactions must be single-use and restore partial reservations safely.

The context exposes the player, guard, default payment item identifier and cost, server world and immutable guard position. The applied offer is also supplied to AFTER listeners. Item identifiers are resolved against the server registry only after all policies accept the offer.
