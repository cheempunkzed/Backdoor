# Gameplay Systems Architecture (v1.4.0)

## Overview
Backdoor combines multiple interconnected gameplay systems into a unified simulation loop.

```
+-------------------+      +---------------------+      +---------------------+
| AbyssOS Manager   | ---> |  SystemEventBus     | ---> |  LivingWorldEngine  |
+-------------------+      +---------------------+      +---------------------+
          |                           |                            |
          v                           v                            v
+-------------------+      +---------------------+      +---------------------+
| WebContentEngine  | <--- | OnionNetworkEngine  | <--- | ShadowEconomyEngine |
+-------------------+      +---------------------+      +---------------------+
```

## System Interconnections

1. **Contracts Engine**:
   - Validates player actions (port scanning, terminal commands, server breaches, file downloads) against contract objectives in real time.

2. **Abyss Web Matrix (Milestone 1.3.0)**:
   - Living web ecosystem that responds to player breaches and corporate security level changes by dynamically updating website press advisories, security status pages, and forum posts.

3. **Underground Ecosystem (Milestone 1.4.0)**:
   - Living darknet with multiple player identities, autonomous NPC actors, PGP encrypted messaging, factions, rumor engine, and shadow exchange market.

4. **Shadow Economy**:
   - Market prices, hardware upgrades, dark market listings, and news service updates seamlessly feed into web pages, email notifications, and darknet forums.

5. **Save & State Persistence**:
   - `SaveManager` preserves VFS data, browser state, bookmarks, history, web content, economy, living world state, and darknet state (`darknetJson`) across sessions.
