# Rally of the Guard API

Rally exposes a small common-side API so integrations can issue tactical orders without simulating client packets or depending on networking implementation classes.

## Compatibility

- Mod version: `1.3.0`
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

Route, rally-group and attack-target operations will join this surface in later Phase 0 increments after their validation rules are separated from packet-specific input.

## Recruitment

The shared recruitment service is available through:

```java
GuardRecruitmentService recruitment = RallyGuardApi.guardRecruitment();
```

`recruit(player, guard)` validates that the guard is alive, unowned, in the player's world and within interaction distance. Rally then builds the configured default `RecruitmentOffer`, runs every `GuardRecruitmentEvents.BEFORE` policy, collects the final personal inventory payment and assigns tactical ownership.

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

The context exposes the player, guard, default payment item identifier and cost, server world and immutable guard position. The applied offer is also supplied to AFTER listeners. Item identifiers are resolved against the server registry only after all policies accept the offer.
