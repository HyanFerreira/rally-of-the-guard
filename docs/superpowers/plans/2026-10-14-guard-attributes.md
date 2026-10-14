# Guard Attribute Application Plan

**Status:** Implemented and automatically verified on 2026-10-14.

## Goal

Apply natural identity and level progression to Guard Villagers without
replacing Guard Villagers base attributes, duplicating modifiers, or healing a
guard merely because its profile was refreshed.

## Delivered slices

- [x] Stable natural and level modifier identifiers.
- [x] Percentage-based natural health, attack, and movement speed.
- [x] Additive natural armor and cumulative level bonuses.
- [x] Remove-and-rebuild idempotence on entity load and configuration refresh.
- [x] Percentage-preserving health adjustment for initialization, load, and refresh.
- [x] Positive maximum-health delta on level-up and clamping on reductions.
- [x] Independent progression, natural variation, stat growth, and advanced-spawn toggles.
- [x] Loaded-guard refresh entry point for server-side configuration integrations.
- [x] Built-in localized rank fallback with external presentation precedence.
- [x] Pure boundary tests, full build, diff validation, and dedicated startup check.

## Persistence decision

Attribute modifiers are permanent entity modifiers with stable Rally IDs. This
allows the entity's saved health percentage to remain meaningful before the
load callback reconstructs modifiers. The profile remains the source of truth:
every application removes all known Rally modifiers and installs the current
derived set, repairing stale derived state without touching unrelated mods.

## Verification boundary

An interactive old-save and new-world pass remains required before release to
observe effective values, damage, movement, health across reload, and config
toggle refreshes in-game.
