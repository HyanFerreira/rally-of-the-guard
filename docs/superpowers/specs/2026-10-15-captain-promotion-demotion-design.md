# Captain Promotion & Demotion Design

**Date:** 2026-10-15  
**Status:** Implemented

## Goal

Allow a player to recognize a veteran guard's service by promoting that guard
to the persistent Captain role, without requiring a lucky natural Captain roll.
Also allow the owner to relieve a Captain from the role without changing the
guard's earned progression.

Natural and promoted Captains remain complementary:

- a natural Captain is a rare, immediately available leader with a hiring
  premium;
- a promoted Captain requires a player-owned Level 7 Veteran plus a separate
  promotion payment.

Captain remains independent from rank, Squad membership, and the optional
Officer assignment. Promotion never assigns an Officer automatically.

## Scope

This milestone includes:

- a dedicated, server-authoritative Captain service;
- promotion of an eligible owned guard;
- demotion of an owned Captain who is not an active Officer;
- configurable enablement, minimum level, and promotion cost;
- payment with the configured `economy.hireItem`;
- public API results and lifecycle events;
- server-authoritative Muster Roll action state;
- a minimal functional Muster Roll action pending the later GUI review;
- English and Brazilian Portuguese text;
- automated unit, contract, resource, and service tests;
- an updated manual smoke-test matrix.

This milestone excludes:

- automatic promotion from level gain;
- promotion through the Commander's Ledger or Squad screen;
- automatic Officer assignment or removal;
- leadership buffs;
- Captain-specific formations;
- refunds for promotion or demotion;
- a broader visual redesign of the existing GUIs.

## Domain Model

The existing `GuardProfile.captain` boolean remains the source of truth. The
profile schema stays at version 2 because the field is already persisted and
decoded safely.

`GuardProfile` gains a copy method that changes only the Captain flag. It must
preserve generation version, origin, XP, natural identity, combat statistics,
and every other profile field byte-for-byte semantically.

No existing profile is promoted retroactively. Natural generation continues to
be the only automatic source of the role.

## Captain Service

API contract version 5 exposes a dedicated service:

```java
CaptainService captains = RallyGuardApi.captains();
CaptainActionView view = captains.inspect(owner, guard);
CaptainMutationResult result = captains.promote(owner, guard);
CaptainMutationResult result = captains.demote(owner, guard);
```

The service is the only supported public write path for the Captain role.
Integrations must not edit profile attachments or call the profile repository.

`CaptainActionView` provides the current server-authoritative action state for
presentation:

- current Captain flag;
- available action: `NONE`, `PROMOTE`, or `DEMOTE`;
- whether that action is currently allowed;
- blocking outcome when disabled;
- configured minimum level;
- optional promotion offer containing payment item and cost.

`CaptainMutationResult` contains an outcome, localized feedback, and the
applied promotion offer when relevant. Outcomes cover at least:

- `SUCCESS`;
- `DISABLED`;
- `NOT_AVAILABLE`;
- `NOT_OWNER`;
- `UNSUPPORTED_PROFILE`;
- `ALREADY_CAPTAIN`;
- `NOT_CAPTAIN`;
- `LEVEL_TOO_LOW`;
- `INSUFFICIENT_PAYMENT`;
- `ACTIVE_OFFICER`;
- `PERSISTENCE_ERROR`;
- `DENIED`.

All service methods must run on the logical server thread. Null, removed,
dead, cross-dimension, or otherwise unavailable guards are rejected without
changing inventory or persistence.

## Promotion Rules

Promotion succeeds only when all of these conditions hold:

1. Captain promotion is enabled;
2. the guard is alive, loaded, and in the owner's dimension;
3. the guard is owned by the requesting player;
4. the profile exists in a supported writable form;
5. the guard is not already a Captain;
6. the derived guard level meets the configured minimum, default 7;
7. policy listeners allow the action;
8. the player's inventory contains the current promotion payment.

Promotion does not grant XP, levels, rank, attributes, Squad membership, or an
Officer assignment.

## Economy and Atomicity

The default promotion offer is four units of `economy.hireItem`. The amount is
configured by `economy.captainPromotionCost`, clamped from 0 through 999. A cost
of zero is valid. Invalid configured item identifiers retain the existing safe
fallback behavior of `RallyConfig.hireItem()`.

Promotion follows this transaction order:

1. validate the current state and construct the offer;
2. invoke the BEFORE policy event;
3. revalidate mutable guard state and the player's inventory;
4. remove the payment;
5. persist a profile copy with `captain = true`;
6. refund the full payment if persistence fails;
7. invoke the AFTER notification;
8. refresh the active Muster Roll snapshot.

The implementation reuses a focused inventory-payment helper rather than
duplicating recruitment's slot traversal. Refunding places the same item and
amount back into the inventory. AFTER-listener failures are logged but do not
roll back a completed promotion.

## Demotion Rules

Demotion succeeds only when the guard is available, owned by the requester,
has a supported writable profile, and is currently a Captain.

If `RallyGuardApi.squads().find(owner, guard).officer()` is true, demotion is
rejected with `ACTIVE_OFFICER`. The player must explicitly clear or replace the
Officer first. The Captain service never edits Squad state as a side effect.

Successful demotion changes only `captain` to false. It does not refund any
payment or change XP, level, rank, natural identity, attributes, statistics,
orders, or Squad membership. A demoted guard may be promoted again later by
meeting the current rules and paying the current cost again.

## Events

`CaptainEvents.BEFORE` is an ordered policy event. It receives an immutable
context containing player, guard, action, current profile view, and optional
promotion offer. The first denial stops evaluation and supplies localized
feedback. The base service remains responsible for every built-in validation.

`CaptainEvents.AFTER` is a notification emitted only after persistence
succeeds. Listener exceptions are isolated and logged so integrations cannot
turn a completed mutation into a misleading failure.

Events do not expose raw profile mutation and do not modify the configured
offer in this milestone.

## Muster Roll Integration

`GuardProfileSnapshot` carries an optional bounded Captain action view created
on the server. The payload never trusts client-side level, ownership, cost, or
Officer calculations.

The existing Muster Roll session is the authority for mutations. The new
serverbound payload carries only the requested action. Its receiver resolves
the guard from the player's active session, repeats all service validation,
executes the mutation, sends feedback, and immediately emits a fresh snapshot.
A forged packet without a valid nearby session and held Muster Roll is rejected.

The first functional UI adds one action button for an owned guard:

- eligible non-Captain: `Promote to Captain — 4 Emeralds`;
- ineligible non-Captain: disabled promotion action with server-provided reason;
- Captain who is not an Officer: `Relieve Captain`;
- active Officer: disabled relief action explaining that the Officer must be
  cleared first;
- unowned guard: no Captain management action.

The layout is intentionally minimal. Button placement, visual hierarchy, and
broader Muster Roll improvements remain open for the owner's later GUI notes;
that review must not move validation to the client.

## Configuration

The following MidnightConfig entries are added:

```text
progression.captainPromotionEnabled = true
progression.captainPromotionMinimumLevel = 7
economy.captainPromotionCost = 4
```

Promotion enablement is independent from natural Captain spawning. Disabling
promotion does not demote existing Captains or erase any data. Demotion remains
available while promotion is disabled so administrators cannot trap an
unwanted assignment; Officer safety still applies.

## Synchronization and Localization

Successful and rejected actions provide English and Brazilian Portuguese
feedback. Configuration labels and tooltips document defaults, bounds, and the
use of the configured hiring item.

After every accepted mutation, the server refreshes the active Muster Roll
snapshot immediately. The normal one-second session tick remains a fallback for
external mutations and inventory changes. Ledger and Squad screens receive the
new Captain state the next time their existing authoritative guard snapshot is
requested.

## Testing

Automated coverage includes:

- profile copy operations preserve every non-Captain field;
- promotion eligibility at levels 6, 7, and 10;
- disabled promotion and clamped configuration;
- ownership, availability, unsupported profile, and duplicate-action failures;
- exact payment across multiple inventory stacks;
- zero-cost promotion;
- no payment on validation or policy denial;
- full refund when profile persistence fails;
- successful promotion changes only the Captain flag;
- demotion changes only the Captain flag and never refunds;
- active-Officer demotion rejection;
- event ordering and listener failure isolation;
- API version and public contract shape;
- payload round-trip and active-session enforcement;
- snapshot action-state derivation;
- English and Brazilian Portuguese resource keys.

The manual smoke test covers promotion, insufficient payment, immediate Muster
Roll refresh, save/reload persistence, Officer blocking, demotion, and
re-promotion.

## Compatibility

Existing worlds, profiles, natural Captains, and Squad data require no
migration. The feature does not reroll identities or modify natural Captain
regional limits. Servers may disable future promotions without changing
existing Captains.

The API change is additive but increments the explicit contract to version 5
so integrations can detect the Captain mutation capability.
