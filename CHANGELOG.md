# Changelog

## Village Defense & Raids — 2026-10-16

- Added server-side detection of nearby active vanilla raids for players with loaded hired guards, with per-player deduplication and no forced chunk loading.
- Added a compact translated alert for rallying guards, defending the village with deterministic safe posts, or ignoring the opportunity.
- Added persistent per-player `Normal`, `Rally to Village`, and `Rally to Commander` behavior settings through the Quick Command panel.
- Revalidated raid identity, dimension, center, distance, session, ownership, and loaded availability before every manual response.
- Added isolated multiplayer raid reports for deployed guards, observed guard-assisted defeats, confirmed-kill MVP, loaded guard losses, and partial-data disclosure after unloads or expiry.
- Added automated policy/session/report coverage and a Minecraft 26.1 smoke-test checklist.
- Improved safe-post selection to accept non-colliding village vegetation and dirt paths, reject fluids and known hazards, and search deterministic alternatives before reporting that no safe post exists.
- Made raid-issued defense orders temporary: guards consistently run to their posts at follow catch-up speed, then resume their exact suspended route, patrol, movement, follow, wait, or defense task when that raid ends, including after a temporary chunk unload.
- Kept manual opportunities, temporary defense orders, and reports bound to the same active raid when vanilla adjusts its center after the alert opens.

## Global Guard Registry — 2026-10-16

- Added a persistent per-player registry of hired guards with last known name, dimension, position, profile, Squad and order.
- Reconciled loaded guards on load, unload, join, ownership changes and a bounded periodic pass without forcing chunks.
- Removed confirmed deaths and recruitment rollbacks while preserving unloaded guards indefinitely.
- Added a server-paged, read-only Global Registry view to the Commander's Ledger with loaded state and same-dimension distance.
- Added operator diagnostics, schema-future protection, codec/editor tests and a Minecraft 26.1 smoke-test checklist.

## Command Post — 2026-10-16

- Added the player-owned Command Post block for persistent territorial defense with configurable name, active state, radius, and formation.
- Added a responsive two-column screen showing guard names, Squad membership, current assignments, and confirmed transfers from other posts.
- Added server-authoritative updates and guard assignment with distance, ownership, entity, and formation validation.
- Added origin-aware defend orders so disabling a post clears only its own orders and later manual orders are not overwritten.
- Added a survival recipe, block drop, persistence coverage, formation-slot tests, and a Minecraft 26.1 smoke-test checklist.

## Unreleased — planned 1.4.0

- Hardened individual route updates with a pre-allocation wire limit, explicit action checks, configured-size enforcement, atomic validation, world-height/border checks, and an 8-block capture radius for new points.
- Preserved stored distant route points for rename, removal, pause, and restart workflows while rejecting forged new distant coordinates.
- Prevented waiting, active-route, and ordinary fixed-patrol guards from being pulled into Rally.
- Centralized Rally lifecycle reconciliation for toggle-off, effect loss, death, dimension changes, disconnect, and login; cleanup now releases only tagged participants and leaves independent followers unchanged.
- Aligned published metadata with the repository's CC0-1.0 license and removed the obsolete route-placeholder action and translations.
- Added focused payload, route-policy, Rally-participation, Rally-cleanup, resource-contract, and participant-isolation GameTest coverage.
- Added compact translated Ledger action labels and an automated fit assertion so tactical buttons no longer clip at 854×480.

- Added persistent player-owned Named Routes with Unicode names, dimensions, wait times, ordered points, configurable limits, and future-schema protection.
- Added a route hub and responsive Saved Routes manager with two-column guard assignment, cross-route confirmation, and reuse of individual guard routes as templates.
- Added server-authoritative create, update, delete, assign, unassign, dimension validation, loaded-guard synchronization, and lazy repair of removed route references.
- Migrated individual guard route tags from v1 to v2 while preserving legacy points and progress and adding an optional Named Route identity.

- Redesigned the Commander's Ledger around a selectable roster and one contextual action panel, separating individual orders from scope-wide orders.
- Replaced repeated per-row Squad controls with a two-column membership workflow that shows each guard's current Squad and confirms cross-Squad transfers.
- Made Villager Names and Collective required dependencies so guard identities remain readable throughout command and organization screens.
- Reworked Quick Command into a compact two-column action panel with a dimmed background, consistent spacing, and full crosshair coverage.
- Added persisted suspended-task snapshots and a `Dismiss` order that restores each guard's route, fixed patrol, movement, defense, follow, wait, or neutral routine after temporary Summon, Follow, Move Here, and Defend Area orders.
- Added Infantry/Ranged identification to the Ledger, reverse scope cycling with `Shift`, scope-wide Named Route assignment, and direct Squad-selection tabs.
- Replaced scope-wide fixed Patrol and Quick Command Patrol with the more useful Named Routes and Dismiss workflows.
- Advanced the public guard-command contract to API version 9 with `dismiss` and `DISMISS`.

- Added a remappable `G` Quick Command panel for Follow, Wait, Dismiss, Attack, Move Here, and Defend Area.
- Reused existing server-authoritative All Guards commands, combat selection, and world-targeting flows without adding a parallel command protocol.
- Added a responsive non-pausing native-button layout with keyboard focus, narration, and English and Brazilian Portuguese labels.

- Added persistent Defensive, Aggressive, Passive, Hold Position, and Protect Commander combat stances for the current Ledger scope.
- Added server-authoritative stance selection, configurable engagement radii, deterministic target selection, and protected-ally filtering.
- Added a translated combat-stance screen with descriptive tooltips and made Passive guards reject explicit attack orders.
- Advanced the public guard-command contract to API version 8 with `setCombatStance` and `SET_COMBAT_STANCE`.

- Added persistent server-authoritative Defend Area orders with configurable radius, safe UUID-ordered posts, scope and unit-category filtering.
- Kept Guard Villagers in control of native patrol and combat while bounding targets to the defended territory and returning guards that leave it without teleporting.
- Added the translated `Defending` Ledger status and a shared single-click targeting session for Move Here and Defend Area.
- Advanced the public guard-command contract to API version 7 with `defendArea` and `DEFEND_AREA`.

- Added server-authoritative Move Here orders for All Guards, Unassigned guards, or a selected Squad, combined with All, Infantry, or Ranged filtering.
- Added deterministic UUID-ordered safe destinations around the clicked block face, with partial handling for unavailable slots and no forced chunk loading or corrective teleport.
- Added persistent move destinations that survive reloads, pause during combat, resume afterward, and finish in Waiting.
- Added a 15-second translated client targeting mode with cancellation on Esc, item swap, death, disconnect, or expiry.
- Added public `moveTo` and `MOVE_TO` contracts in API version 6.

- Added a responsive, non-pausing Formation Selection Hub with native keyboard focus, narration, translated tooltips, and direct access to every Rally formation.
- Added strict server-authoritative formation selection that validates the hand, Scroll of Rallying, and formation identifier before persisting or updating an active Rally.
- Added selectable Escort, Column, Line, Wedge, Square, Circle, and Shield Wall Rally formations.
- Added persistent per-scroll formation selection with live server-authoritative switching during an active Rally.
- Preserved existing combat, retreat, pathfinding, safe-slot, and corrective teleport behavior across every formation.
- Refined Square into a stable grid and increased Circle's configurable minimum protection radius to 3.25 blocks.
- Smoothed moving formations with continuous navigation, short look-ahead, and bounded sprint catch-up speeds.
- Added a configurable lateral heading margin and gradual turning so minor commander drift no longer abruptly rotates the formation.
- Added near-slot velocity matching with bounded positional correction, while retaining pathfinding for separated guards and terrain recovery.
- Kept guard body and head rotation aligned smoothly with direct formation movement.
- Made decisive commander turns realign immediately and strengthened bounded recovery when guards are left ahead of their new rear slots.
- Added bounded sprint assistance to the native Follow order while leaving walking, combat, Rally, and mounted travel behavior untouched.

- Added server-authoritative promotion of owned Level 7 Veteran guards to the persistent Captain role.
- Added configurable Captain promotion enablement, minimum level, and a default cost of 4 units of the configured hiring item.
- Added Captain demotion without refunds or progression changes, with protection against demoting an active Squad Officer.
- Added atomic inventory payment with full refund when Captain profile persistence fails.
- Added Captain API contract version 5 with inspection, mutation results, ordered BEFORE policies, and isolated AFTER notifications.
- Added secure Muster Roll promotion networking bound to the active nearby inspection session.
- Added a minimal synchronized Muster Roll promotion/demotion control with English and Brazilian Portuguese feedback.

- Added persistent player-owned Squads with custom creation, rename, and deletion in the Commander's Ledger.
- Added All Guards, Unassigned, and named-Squad Ledger scopes with collective summon, follow, wait, dismiss, Named Route, and combat orders.
- Added persistent guard-to-Squad assignment with lazy repair when a referenced Squad no longer exists.
- Added one Captain Officer per Squad, with server-authoritative membership and Captain validation.
- Added configurable Squad, loaded-member, and Unicode name-length limits.
- Added read-only Squad views and server-side Squad mutation service in API contract version 4.
- Added English and Brazilian Portuguese Squad management text.
- Added a Squad column to the Ledger and immediate server-authoritative state refresh after individual or collective orders.

- Added deterministic natural quality, attribute rolls, initial levels, ranks, and star ratings for guards.
- Added persistent Natural Captains with configurable spawn chance, minimum level, loaded-region control, Muster Roll display, and hiring-cost bonus.
- Fixed village guard batches evaluating Captain limits before earlier guards had entered the world.
- Migrated schema-1 profiles to schema 2 as non-Captains without rerolling existing identities.
- Migrated Profile Core neutral placeholders once without rerolling identities on later reloads.
- Classified trustworthy natural and structure spawns separately from commands, spawn items, and spawners.
- Applied stable natural and level attribute modifiers without replacing Guard Villagers base values.
- Added progression toggles and health-preserving attribute refresh behavior.
- Added server-side combat contribution XP with final-blow weighting, kill and assist statistics, and timeout cleanup.
- Added datapack-controlled XP target and threat-category tags plus configurable XP and assist thresholds.
- Added public progression operations and isolated XP/stat/level events for integrations.
- Added rank-, level-, and quality-based dynamic hiring costs with configurable multiplier and bounds.
- Added server-authoritative recruitment quotes that detect changed offers before charging the player.
- Updated the hiring screen to display the synchronized server quote instead of local static configuration.
- Added the permanent Muster Roll item, recipe, and read-only guard profile screen.
- Added bounded server snapshots with live level, rank, stars, XP, attributes, combat record, service status, and hiring offer.
- Added one-second change-only profile updates and automatic session closure when inspection becomes invalid.
- Added operator-only `/rallyguard xp add` and `/rallyguard xp get` commands for testing nearby or explicitly selected guards without combat grinding.
- Refined profile and command screen alignment, including a wider Muster Roll, centered hiring offer, and vertically centered guard status.
- Fixed Guard Villagers melee guards continuously backpedaling when fighting without a shield while preserving shield-based retreat behavior.
- Corrected Guard Villagers crossbow projectile speed to the vanilla mob value so guards can hit shorter targets reliably.
- Kept guards immobile during Rally hiring and Muster Roll interactions, releasing them safely when the last viewer closes or disconnects.
- Prevented hired guards from targeting or damaging companions owned by the same commander.
- Rejected manual combat orders against commander-owned companions.
- Added automatic Rally retreat when a fighting guard moves more than 24 blocks from the commander.
- Added configuration to disable automatic retreat for fight-until-combat-ends behavior without disabling ally protection.

## Recipe visibility hotfix - 2026-10-02

- Fixed the Commander's Ledger and Scroll of Rallying recipes not appearing in JEI, JEB, or other recipe viewers.
- Moved the recipes to Minecraft's recognized `data/rallyguard/recipe` directory.
- Updated the crafting category and ingredient syntax for Minecraft 26.1.
- Kept the original ingredients and crafting patterns unchanged.
