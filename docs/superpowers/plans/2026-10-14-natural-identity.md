# Natural Identity Implementation Plan

**Status:** Implemented and automatically verified on 2026-10-14.

## Goal

Give every Guard Villager a persistent natural identity, derive level, rank,
stars, and XP progress, and distinguish trustworthy natural generation from
player-exploitable spawn paths.

## Compatibility decision

Profile Core wrote a temporary neutral legacy profile with generation version
`1`. Natural Identity uses generation version `2`. Only the exact untouched
legacy placeholder is eligible for migration. Its replacement is generated
from the entity UUID, saved once, and remains stable across every later load.
Profiles with statistics, XP, changed rolls, another origin, or a future schema
are never treated as placeholders.

## Delivered slices

- [x] Fixed XP thresholds, level derivation, natural-level weights, and ranks.
- [x] Triangular quality generation and five star bands.
- [x] Quality-biased independent health, attack, armor, and speed rolls.
- [x] Deterministic UUID-seeded generation and one-time placeholder migration.
- [x] Spawn-reason capture before entity-load fallback initialization.
- [x] Conservative level 1 for commands, spawn items, spawners, and unknown origins.
- [x] Read-only derived helpers in `GuardProfileView`.
- [x] English and Portuguese rank localization.
- [x] Unit, full build, diff, and dedicated-server startup verification.

## Verification boundary

Automated verification covers formulas, boundaries, deterministic migration,
serialization, the full regression suite, resource processing, and mixin
startup. An interactive old-save/new-world smoke pass remains required before
release to inspect actual guard identities and reload stability in-world.
