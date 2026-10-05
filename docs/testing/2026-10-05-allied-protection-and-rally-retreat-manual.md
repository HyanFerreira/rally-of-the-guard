# Allied Protection and Rally Retreat — Acceptance Record

Date: 2026-10-05

## Automated build evidence

| Branch | Commit | Java | Command | Artifact | Result |
| --- | --- | --- | --- | --- | --- |
| `main` (Minecraft 26.1) | `1472b4e86223350feb3b02d3ab0663964c81d7db` | 25 host / release 21 | `.\gradlew.bat clean test build` | `rallyguard-fabric-26.1-1.3.2.jar` | PASS — `BUILD SUCCESSFUL` |
| `26.1` | `ce36e815508cab49dc43760832635be38cca79b4` | 25 host / release 21 | `.\gradlew.bat clean test build` | `rallyguard-fabric-26.1-1.3.2.jar` | PASS — `BUILD SUCCESSFUL` |
| `1.21.11` | `d0d55b72fd9c9ba220fe26047736d9b55019767a` | 25 host / release 21 | `.\gradlew.bat clean test build` | `rallyguard-fabric-1.21.11-1.3.1.jar` | PASS — `BUILD SUCCESSFUL` |
| `1.21.1` | `cfe6dac748bbd630e1ed976d8a984ddab7552245` | 25 host / release 21 | `.\gradlew.bat clean test build` | `rallyguard-fabric-1.21.1-1.3.1.jar` | PASS — `BUILD SUCCESSFUL` |

The automated suites cover ownership relationship decisions, retreat decision precedence and boundary behavior, ally-safety event decisions, and retreat configuration defaults/clamping. They do not replace the in-game checks below.

## Manual gameplay matrix

`NOT RUN` means the case still requires direct observation in a launched Minecraft client. No gameplay cell is inferred from compilation or unit tests.

| Case | `main` / 26.1 | `26.1` | `1.21.11` | `1.21.1` |
| --- | --- | --- | --- | --- |
| Hitting the commander's wolf does not make hired guards target or damage it | NOT RUN | NOT RUN | NOT RUN | NOT RUN |
| A protected companion involved in combat is not retaliated against | NOT RUN | NOT RUN | NOT RUN | NOT RUN |
| Manual attack order against a protected companion is rejected | NOT RUN | NOT RUN | NOT RUN | NOT RUN |
| Manual attack order against a valid unprotected target still works | NOT RUN | NOT RUN | NOT RUN | NOT RUN |
| Another player's owned entity is not treated as the commander's companion | NOT RUN | NOT RUN | NOT RUN | NOT RUN |
| Guard continues fighting at or within 24 blocks | NOT RUN | NOT RUN | NOT RUN | NOT RUN |
| Guard disengages and returns after the commander moves beyond 24 blocks | NOT RUN | NOT RUN | NOT RUN | NOT RUN |
| Retreating guard navigates back when below formation teleport distance | NOT RUN | NOT RUN | NOT RUN | NOT RUN |
| Existing corrective teleport works beyond formation teleport distance | NOT RUN | NOT RUN | NOT RUN | NOT RUN |
| With teleport disabled, retreat uses navigation without teleporting | NOT RUN | NOT RUN | NOT RUN | NOT RUN |
| With retreat disabled, valid hostile combat continues | NOT RUN | NOT RUN | NOT RUN | NOT RUN |
| Ally protection remains active while retreat is disabled | NOT RUN | NOT RUN | NOT RUN | NOT RUN |
| Wait, patrol, route, and existing Rally orders remain unaffected | NOT RUN | NOT RUN | NOT RUN | NOT RUN |
| Owned rideable animal (horse or donkey) is protected | NOT RUN | NOT RUN | NOT RUN | NOT RUN |
| Ridden Happy Ghast is protected | NOT RUN | NOT RUN | NOT RUN | N/A — entity absent |
| Directly leashed Happy Ghast is protected | NOT RUN | NOT RUN | NOT RUN | N/A — entity absent |
| Harnessed but unridden and unleashed Happy Ghast remains unclassified | NOT RUN | NOT RUN | NOT RUN | N/A — entity absent |

## In-game procedure

Use a fresh test world for each supported version with Rally of the Guard and its matching dependencies. Test both melee and bow/crossbow guards for companion protection so the direct and projectile damage paths are exercised. For retreat boundaries, place the guard at a measured distance from the commander: verify combat at exactly 24 blocks and disengagement only above 24 blocks with the default configuration.

After each observed run, replace `NOT RUN` with `PASS` or `FAIL` and add a short note for failures. Any shared failure must be fixed on `main` first and then ported in order to `26.1`, `1.21.11`, and `1.21.1`.
