# Dynamic Recruitment Implementation Plan

Date: 2026-10-14

## Goal

Price guards from persisted level and natural quality while preserving atomic
payment, policy replacement, and server authority.

## Implemented

- Calculate rank-band prices with half-up rounding, multiplier, and configured
  minimum/maximum bounds.
- Fall back to the original static hiring cost when dynamic pricing is disabled
  or a supported profile is unavailable.
- Centralize default offers in `GuardHiringValueService` and expose an isolated
  observational calculation event.
- Create server quotes fingerprinted from guard UUID, profile XP, payment item,
  and final policy-adjusted cost.
- Synchronize the quoted cost and payment item through the hiring menu.
- Re-evaluate eligibility, policies, profile state, and price on confirmation.
- Return `OFFER_CHANGED` without charging or reserving transactions, refresh the
  visible offer, and require a second confirmation.
- Preserve the existing reservation, payment refund, ownership rollback, and
  AFTER-event behavior after quote validation succeeds.

## Verification

- Unit tests cover every rank boundary, monotonic level/quality behavior,
  half-up rounding, multipliers, clamps, configuration fallbacks, and quote
  fingerprint sensitivity.
- Full build, resource validation, and dedicated initialization remain release
  gates; in-world changed-offer behavior remains a manual smoke test.
