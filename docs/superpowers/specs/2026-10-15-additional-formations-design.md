# Additional Formations — Design

**Status:** Implemented

## Goal

Expand the existing Rally escort layout into a selectable family of formations
without replacing Guard Villagers combat AI or coupling the feature to the
planned GUI redesign.

## Formation set

The Scroll of Rallying supports these stable formation identifiers:

- `escort` — the existing two-column layout and compatibility default;
- `column` — one guard per row;
- `line` — one centered horizontal rank;
- `wedge` — a leading center slot followed by widening paired rows;
- `square` — a compact, centered rectangular grid sized for the rallied group;
- `circle` — guards distributed evenly around the commander;
- `shield_wall` — a tighter horizontal rank offset from the commander.

Unknown or missing persisted identifiers resolve to `escort` so old scrolls and
future-version data never break item loading.

## Selection and persistence

The selected formation belongs to each Scroll of Rallying and is persisted as a
data component.

- Right-click cycles to the next formation and reports the new selection.
- Shift + right-click preserves the existing Rally toggle.
- Starting a Rally captures the scroll's selected formation.
- Changing the selected formation on the active scroll updates the live Rally
  immediately on the server.

No new screen is introduced in this milestone. A later GUI revision may expose
the same selection without changing the persisted model.

## Slot calculation

Formation geometry is a pure, deterministic calculation from:

- formation type;
- stable guard index;
- total rallied guards;
- configured column and row spacing.

The result is a local offset (`right`, `forward`) relative to the Rally anchor.
World rotation and safe-ground adjustment remain in `RallyFormationSlots`.

The existing anchor behavior remains intact: initial activation may place slots
in front of the player, while movement makes trailing formations settle behind
the commander. Circle remains centered on the commander.

## Runtime behavior

Combat priority, protected-ally disengagement, safe-ground selection, and
corrective teleport behavior remain authoritative across every formation.

Following playtest feedback, a moving commander keeps guards navigating toward
a short predicted slot instead of repeatedly stopping inside a dead band.
Walking, sprinting, and catch-up use bounded speed tiers. The native Follow
order remains authoritative outside Rally and only receives bounded assistance
when a sprinting, unmounted commander pulls more than three blocks ahead.

Guard ordering remains stable by entity ID. Layout changes therefore do not
randomly reshuffle guards on every tick.

## Compatibility

- Existing scrolls default to Escort.
- `rally.formationEnabled = false` retains vanilla-style following and ignores
  the selected geometry without deleting it.
- Existing spacing settings are reused and relabeled as general formation
  spacing.
- The implementation remains in version 1.4.0.

## Verification

Automated tests cover identifiers, cycling, legacy fallback, cardinal layout
geometry, symmetry, finite coordinates, and group-size edge cases. A manual
Minecraft smoke test covers item persistence, live switching, obstacles,
combat interruption, regrouping, and save/reload behavior.
