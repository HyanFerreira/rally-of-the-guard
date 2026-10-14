# Combat Progression Implementation Plan

Date: 2026-10-14

## Goal

Award persistent guard XP from effective combat contribution, record kills and
assists, refresh level attributes, and expose safe integration hooks.

## Implemented

- Capture health before accepted damage and measure only health actually removed.
- Keep short-lived per-target ledgers with total and per-guard contribution.
- Resolve melee, direct guard, and guard-owned projectile damage.
- Expire ledgers after the configurable timeout and clear them on unload/stop.
- Resolve eligible targets and XP categories through datapack entity-type tags.
- Apply the XP multiplier before deterministic milli-XP distribution.
- Split guard final blows between contribution and final-blow shares; return the
  final-blow portion to contribution when a non-guard kills the target.
- Record kills only for guard final blows and assists only at both configured
  minimum boundaries.
- Persist progression through `GuardProgressionService` and refresh attributes
  immediately when a level changes.
- Expose XP policy and observational progression events through the common API.

## Verification

- Unit tests cover category priority, effective damage, overkill, timeouts,
  contribution division, rounding, budget conservation, assist boundaries,
  configuration fallback, and persisted statistic saturation.
- Resource tests cover every shipped combat progression tag.
- Full build and dedicated startup remain release gates.
