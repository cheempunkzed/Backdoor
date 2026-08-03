# Backdoor System Architecture (v1.5.0)

## High-Level Architecture
Backdoor is built on an event-driven, reactive system architecture powered by Kotlin Coroutines, StateFlows, and Jetpack Compose.

```
+-------------------------------------------------------------------------+
|                              AbyssOS UI                                 |
|   (Desktop, WindowManager, App Apps, Terminal, System Monitor App)      |
+-------------------------------------------------------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                          WorldSimulationEngine                          |
|  (Master Coordinator: LivingWorldEngine, OrganizationAI, LawEnforcement)|
+-------------------------------------------------------------------------+
         |                          |                           |
         v                          v                           v
+------------------+     +--------------------+     +---------------------+
| Vulnerability    |     | LawEnforcement     |     | ProceduralMission   |
| Engine (CVEs)    |     | Engine (Heat)      |     | Generator (V2)      |
+------------------+     +--------------------+     +---------------------+
         |                          |                           |
         +--------------------------+---------------------------+
                                    |
                                    v
+-------------------------------------------------------------------------+
|                            SystemEventBus                               |
| (Events: ServerPatched, EmployeeResigned, HeatChanged, MissionCreated) |
+-------------------------------------------------------------------------+
                                    |
            +-----------------------+-----------------------+
            |                       |                       |
            v                       v                       v
+----------------------+  +-------------------+   +--------------------+
| CorporateRepository  |  | OnionNetworkEngine|   | ShadowEconomyEngine|
+----------------------+  +-------------------+   +--------------------+
```

## Core Modules
1. **WorldSimulationEngine**: Coordinates all living AI systems, hourly game ticks, law enforcement, dynamic vulnerabilities, and organization AI decisions.
2. **OrganizationAI**: Departmental AI (Security, HR, IT, Finance) managing firewalls, IDS, employee stress, resignations, patching, and budget allocation.
3. **LawEnforcementEngine**: Global Heat tracking (0-100%), agency surveillance tiers, and proxy route blocking.
4. **VulnerabilityEngine**: Dynamic CVE vulnerability lifecycle, zero-day generation, and patch validation.
5. **ProceduralMissionGenerator**: Context-aware mission generation based on real world simulation events.
6. **SaveManager**: Unified JSON persistence layer preserving complete VFS, darknet, economy, and world simulation state.
