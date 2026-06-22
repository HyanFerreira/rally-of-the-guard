# Rally of the Guard

**Rally of the Guard** is a lightweight tactical expansion for
[Guard Villagers (Fabric/Quilt)](https://www.curseforge.com/minecraft/mc-mods/guard-villagers-fabric).
It lets you hire guards, command them from anywhere, send them on patrol, and rally your squad on demand
without needing the Hero of the Village effect.

Built for players who want their hired guards to feel like a real squad instead of passive village decoration.

---

## What's New in 1.2.5

### Minecraft 1.21.11 Support

This release ports Rally of the Guard to **Minecraft 1.21.11** for Fabric.

### More Reliable Rally Behavior

The **Scroll of Rallying** now does more than simply teleport guards and enable follow mode.
When a rally starts, each summoned guard has its current combat/navigation task cleared, movement reset,
AI re-enabled, and follow mode refreshed so they react immediately after being called.

This should greatly reduce cases where a guard arrives near the player but keeps wandering until the player
gets close or opens the guard inventory.

### Updated Compatibility

Updated for the current 1.21.11 Fabric toolchain and Guard Villagers dependency.

---

## Core Features

### Hire Guards

- Right-click an unowned guard to open the hire screen.
- Pay **3 emeralds** to recruit the guard.
- Hired guards are linked to your player UUID.
- Newly hired guards receive a gold-colored display name and introduce themselves in chat.
- Hired guards can follow you without requiring Hero of the Village.

### Scroll of Rallying

Use the **Scroll of Rallying** to gather nearby hired guards into formation.

- **Shift + Right-click** to toggle Rally.
- When enabled, nearby non-patrolling hired guards are teleported near you and set to follow.
- When disabled, rallied guards stop following.
- Patrolling guards are not pulled away from their posts.
- While rallied, your hired guards are protected from your own attacks.

### Commander's Ledger

The **Commander's Ledger** opens a portable guard command panel with no keybind required.

From the panel, you can:

- View your hired guards.
- Summon a selected guard to your position in the same dimension.
- Set a guard to patrol your current position.
- Stop an active patrol.

### Patrol Orders

Patrol commands store the guard's patrol position directly, allowing the guard to walk to the ordered location
and hold that post until told otherwise.

### Friendly Fire and Neutrality

- Your rallied guards are protected from your melee and projectile damage.
- If you attack a guard you do not own, your hired guards will stay neutral and will not help you fight other guards.

---

## Items

### Scroll of Rallying

Used to start or end a rally.

Recipe:

```text
Spruce Slab | Spruce Planks | Spruce Slab
Black Wool  | Iron Sword    | Black Wool
Empty       | Spruce Slab   | Empty
```

### Commander's Ledger

Used to open the guard command panel.

Recipe:

```text
Book    | Scroll of Rallying | Empty
Emerald | Empty              | Empty
Empty   | Empty              | Empty
```

---

## Requirements

- **Minecraft:** 1.21.11
- **Mod loader:** Fabric
- **Java:** 21 or newer
- **Fabric API**
- **Guard Villagers (Fabric/Quilt)**

---

## Notes and Limits

- Guard commands work only within the same dimension.
- Guards in unloaded chunks may not pathfind until their chunk is loaded.
- If a guard is too far away, summon them first, then assign a patrol position.
- Rally only affects hired guards that are not currently patrolling.

---

## Previous Highlights

### Version 1.2.3

- Added the **Commander's Ledger**.
- Added the paginated guard command panel.
- Added summon and patrol controls for hired guards.
- Improved hire feedback messages.
- Added gold display names for hired guards.

### Version 1.2.0

- Added rally-only friendly fire protection.
- Added selective rally behavior so patrolling guards stay at their posts.
- Improved rally dismiss behavior.
- Added neutrality logic when attacking non-owned guards.
- Added automatic Guard Villagers config patch for `followHero=false`.

---

## License

This mod is available under the **CC0-1.0 license**.
You may use, modify, distribute, and build on it freely.

---

## Feedback and Contributions

Found a bug or have an idea?
Visit the GitHub repository:

https://github.com/HyanFerreira/rally-of-the-guard
