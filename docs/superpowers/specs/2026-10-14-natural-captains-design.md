# Natural Captains Design

## Goal

Complete Guard Profiles & Progression by giving newly initialized natural guards a persistent, region-limited Captain role that affects level, hiring value, API presentation, and the Muster Roll without introducing the future Squads/Officers system.

## Profile and migration

- Add a persisted `captain` boolean and expose it through `GuardProfileView`.
- Advance the profile schema from 1 to 2.
- Decode schema 1 profiles as schema 2 profiles with `captain = false` and mark them for canonical rewrite.
- Preserve future schemas unchanged.
- Never reroll or retroactively promote an existing profile.

## Selection

- Only spawn reasons already classified as natural-level eligible may produce a natural Captain.
- Captain generation is independently configurable and disabled when advanced natural guards are disabled.
- Default base chance: 5%.
- Default minimum Captain level: 5. A selected Captain below that level starts exactly at the configured minimum.
- Count already-profiled Captains among loaded guards within a configurable 128-block radius:
  - zero nearby: full base chance;
  - one nearby: half base chance;
  - two or more nearby: zero chance.
- The default regional limit is two nearby Captains. This is deliberately a loaded-entity approximation; persistent territorial leadership belongs to Squads/Officers or the future Global Guard Registry.
- UUID-seeded generation remains deterministic once the nearby-Captain count and configuration inputs are fixed.

## Configuration

Add progression settings for Captain generation enabled, base chance, minimum level, regional radius, and regional limit. Add an economy setting for a fixed Captain hiring bonus, defaulting to four payment items. Clamp all externally editable values.

## Hiring and presentation

- Apply the Captain bonus after rank/quality/multiplier calculation and before the configured final min/max clamp.
- Carry `captain` in the Muster Roll snapshot and payload.
- Render a centered gold `Captain` role below the guard name, moving the remaining content down only when needed.
- Add localized English and Brazilian Portuguese labels.

## Compatibility and scope

- Do not implement Captain promotion, squad ownership, leadership bonuses, or officer commands.
- Command-spawned, converted, and legacy guards do not become natural Captains.
- Existing profiles remain byte-for-byte semantically stable except for canonical schema migration with `captain = false`.
- Record only the Minecraft 26.1 manual checks explicitly confirmed by the user; do not relabel the historical 1.3.1 cross-version matrix.
