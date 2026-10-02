# Rally of the Guard API

Rally exposes a small common-side API so integrations can issue tactical orders without simulating client packets or depending on networking implementation classes.

## Compatibility

- Mod version: `1.3.1`
- API contract version: `1`
- Entry point: `net.hfstack.rallyguard.api.RallyGuardApi`

Integrations should check `RallyGuardApi.apiVersion()` before relying on a newer contract. Additive changes may keep the same contract version; incompatible changes require a new integer version and a coordinated minimum mod dependency.

## Guard commands

Obtain the shared service with:

```java
GuardCommandService commands = RallyGuardApi.guardCommands();
```

The first contract exposes:

- `summon`
- `follow`
- `wait`
- `patrol`
- `stopPatrol`

Every method must run on the logical server thread. The service validates that the guard is alive, loaded in the commander's world and tactically owned by that commander. It returns a `GuardCommandResult` with a machine-readable outcome and localized player feedback.

Rally's own packet receiver uses this same service. Integrations must call the service directly on the server and must not construct Rally network payloads.

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
