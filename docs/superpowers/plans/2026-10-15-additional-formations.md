# Additional Formations — Implementation Plan

## 1. Formation model and geometry

Create a stable formation enum and pure layout calculator. Add tests first for
ID fallback, cycle order, Escort compatibility, Column, Line, Wedge, Square,
Circle, and Shield Wall geometry.

Commit: `feat(formation): define selectable formation layouts`

## 2. World-space slot integration

Refactor `RallyFormationSlots` to transform local offsets into safe world-space
slots while preserving the existing Escort entry points. Pass formation type
and group size through initial Rally placement and the formation ticker.

Commit: `feat(formation): apply layouts to rallied guards`

## 3. Scroll selection and persistence

Register a persistent formation data component. Make normal right-click cycle
the selection, keep Shift + right-click as the Rally toggle, update an active
Rally immediately, and expose selection in tooltip/status messages.

Commit: `feat(formation): persist scroll formation selection`

## 4. Configuration, localization, and documentation

Generalize spacing labels, add English and Brazilian Portuguese strings,
document controls and behavior, add the 26.1 smoke test, update changelog and
ROADMAP, and keep the release target at 1.4.0.

Commit: `docs(formation): finalize additional formations`

## 5. Final verification

Run `clean test build`, inspect generated 1.4.0 artifacts, check the worktree,
and review persistence and server-authoritative behavior before declaring the
roadmap item complete.
