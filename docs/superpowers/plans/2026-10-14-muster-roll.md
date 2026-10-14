# Muster Roll Implementation Plan

Date: 2026-10-14

## Goal

Provide a focused, read-only guard profile screen backed entirely by bounded,
server-authoritative snapshots.

## Implemented

- Add a permanent, single-stack Muster Roll with a shapeless book, paper, and
  feather recipe.
- Prioritize Muster Roll interaction over hiring and Guard Villagers' normal
  interaction for owned and unowned guards.
- Validate a living, loaded guard in the same dimension within eight blocks and
  require the player to continue holding the item.
- Build bounded snapshots containing identity, derived progression, effective
  attributes, combat record, service status, and an optional current offer.
- Keep profile schema, generation, origin, quality value, and natural rolls out
  of the wire contract.
- Open a client-only read-only screen and refresh it once per second only when
  snapshot contents change.
- Close and clean up the session when the player closes the screen or validation
  fails; clear all sessions when the server stops.
- Preserve localized built-in rank labels while allowing an explicit external
  presentation override.

## Verification

- Unit tests cover snapshot bounds and the absence of internal profile fields.
- Resource tests cover the item definition, model, texture resolution, and
  shapeless recipe.
- Full build and dedicated initialization remain release gates; visual and
  live-session behavior remain manual smoke tests.
