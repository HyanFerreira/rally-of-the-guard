# Rally of the Guard

![Rally of the Guard Banner](https://raw.githubusercontent.com/HyanFerreira/rally-of-the-guard/refs/heads/1.21.1/docs/media/rally-of-the-guard-banner.png)

[CurseForge](https://www.curseforge.com/members/thecyber27/projects) |
[GitHub](https://github.com/HyanFerreira) |
[Issues](ISSUES_LINK_HERE)

---

## 📖 About

**Your village guards are no longer decorations. They are your squad.**

Rally of the Guard is a lightweight tactical expansion for
[Guard Villagers (Fabric/Quilt)](https://www.curseforge.com/minecraft/mc-mods/guard-villagers-fabric).
Hire guards, command them from anywhere, rally them before a fight, and build patrol routes around your village,
base, fortress, or modpack settlement.

The mod is designed to work with the existing Guard Villagers AI instead of replacing it. Guards still move, fight,
react, and defend using their native behavior. Rally of the Guard simply gives players better tools to command them.

---

## 📚 Features

### 🪙 Hiring Guards

![Hire Guard](https://raw.githubusercontent.com/HyanFerreira/rally-of-the-guard/refs/heads/1.21.1/docs/media/hire-guard.png)

Recruit guards directly in-game and turn them into your personal squad.

- Right-click an unclaimed guard to open the hiring screen.
- The default cost is **3 emeralds**.
- Hiring cost and hiring item can be changed through config.
- Hired guards are bound to their commander.

---

### 📒 Commander's Ledger

![Commander's Ledger](https://raw.githubusercontent.com/HyanFerreira/rally-of-the-guard/refs/heads/1.21.1/docs/media/commanders-ledger.png)

The Commander's Ledger is your portable command panel.

- View your hired guards in one place.
- See each guard's current order state.
- Summon a selected guard to your position.
- Order guards to follow, wait, patrol, or run a route.
- Open combat orders for the target you are looking at.
- Manage your squad without needing to find every guard manually.

Current guard states include **Idle**, **Following**, **Waiting**, **On patrol**, and **On route**.

---

### ⚔️ Escort Formation

![Rally Formation](https://raw.githubusercontent.com/HyanFerreira/rally-of-the-guard/refs/heads/1.21.1/docs/media/rally-formation.png)

Rallied guards can form an escort around you.

```text
  P
x   x
x   x
x   x
```

When the rally starts, guards are placed into two escort columns around the player. While the rally remains active,
they try to keep their formation slots. If combat starts, they are allowed to break formation and fight.

---

### 🎯 Combat Orders

![Combat Orders](https://raw.githubusercontent.com/HyanFerreira/rally-of-the-guard/refs/heads/1.21.1/docs/media/combat-orders.png)

Look at a target and send your rallied squad after it.

- Press **R** by default to open Combat Orders.
- The keybind can be changed in Minecraft's Controls screen.
- **All** sends every matching rallied guard.
- **Infantry** sends melee guards.
- **Archers** sends guards carrying bows or crossbows.

Combat orders affect hired guards currently following you. Guards assigned to wait, patrol, or run a route keep their
orders and are not pulled into the attack.

---

### 🧭 Patrol Routes

![Patrol Route Editor](https://raw.githubusercontent.com/HyanFerreira/rally-of-the-guard/refs/heads/1.21.1/docs/media/patrol-route-editor.png)

Build real patrol routes for your guards.

- Create routes with a configurable point limit.
- Add points using your current position.
- Choose how long guards wait at each point.
- Start, pause, clear, and edit routes from the route screen.
- Routes loop until paused or cleared.

Once a route is active, the guard walks to the current point, waits, then receives the next point as its patrol
position. If hostile mobs appear along the way, the guard's normal combat AI can still take over.

---

### 📜 Scroll of Rallying

![Scroll of Rallying](https://raw.githubusercontent.com/HyanFerreira/rally-of-the-guard/refs/heads/1.21.1/docs/media/scroll-of-rallying.png)

When danger arrives, rally your squad.

- Shift + right-click to toggle the rally.
- Nearby hired guards are pulled into formation around you.
- Rallied guards follow you without requiring Hero of the Village.
- Patrolling and waiting guards keep their current orders.
- Friendly fire protection helps prevent accidental hits during the rally.

---

### ✨ Items & Effects

![Items and Effects](https://raw.githubusercontent.com/HyanFerreira/rally-of-the-guard/refs/heads/1.21.1/docs/media/items-and-effects.png)

Rally of the Guard adds a small set of focused tools built around command and coordination.

- **Scroll of Rallying:** starts and ends the rally.
- **Commander's Ledger:** opens the guard command panel.
- **Rally Commander:** marks the active rally while your squad is gathered.

---

### 🛡️ Patrol Posts

Need a guard at the gate? At the farm? Near the storage room?

Use the Commander's Ledger to assign a fixed patrol position. The guard will move to that location and hold the post
using Guard Villagers' native patrol behavior.

---

## 🖼️ Media

![Patrol In Action](https://raw.githubusercontent.com/HyanFerreira/rally-of-the-guard/refs/heads/1.21.1/docs/media/patrol-in-action.png)

![Guard Inventory](https://raw.githubusercontent.com/HyanFerreira/rally-of-the-guard/refs/heads/1.21.1/docs/media/guard-inventory.png)

---

## 📦 Installation

➡️ **Required on client and server**

➡️ Requires **Fabric**

➡️ Requires **Fabric API**

➡️ Requires **Guard Villagers (Fabric/Quilt)**

➡️ Requires **MidnightLib**

➡️ Requires **Java 21+**

---

## ⚙️ Configuration

Rally of the Guard uses MidnightLib config integration, so compatible config screens can expose the settings directly
from Mod Menu.

For the best in-game configuration experience, install:

➡️ **Mod Menu**

➡️ **Configured**

You can customize:

- **Economy:** guard hiring cost and hiring item, including items from other mods.
- **Rally:** rally radius, max rallied guards, teleport behavior, and rally effect timer.
- **Formation:** escort formation spacing, return speed, and teleport distance.
- **Combat:** target range, guard search radius, passive targets, and player targets.
- **Routes:** point limit, wait time, movement speed, and stuck teleport behavior.

Manual config files are generated by MidnightLib in the Minecraft config folder.

---

## 🧩 Notes

- Guard commands work only while the guard is loaded in the current world.
- Patrol routes pause naturally if the guard's chunk is unloaded.
- Routes are stored on the guard and survive world reloads.
- This mod does not replace Guard Villagers combat AI; it builds command tools around it.

---

## 🏆 Credits

Created by **Hyan Ferreira**.

Built as a tactical expansion for **Guard Villagers (Fabric/Quilt)**.
