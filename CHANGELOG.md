# Changelog

## Unreleased — planned 1.5.0

- Added persistent player-owned Squads with custom creation, rename, and deletion in the Commander's Ledger.
- Added All Guards, Unassigned, and named-Squad Ledger scopes with collective summon, follow, wait, patrol, and combat orders.
- Added persistent guard-to-Squad assignment with lazy repair when a referenced Squad no longer exists.
- Added one Captain Officer per Squad, with server-authoritative membership and Captain validation.
- Added configurable Squad, loaded-member, and Unicode name-length limits.
- Added read-only Squad views and server-side Squad mutation service in API contract version 4.
- Added English and Brazilian Portuguese Squad management text.
- Added a Squad column to the Ledger and immediate server-authoritative state refresh after individual or collective orders.

## 1.4.0

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
