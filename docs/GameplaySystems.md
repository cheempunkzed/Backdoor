# Gameplay Systems Architecture (v1.5.0)

## Overview
Backdoor combines multiple interconnected gameplay systems into a unified simulation loop.

```
+------------------------+      +---------------------+      +---------------------+
| WorldSimulationEngine  | ---> |  SystemEventBus     | ---> |  LivingWorldEngine  |
+------------------------+      +---------------------+      +---------------------+
            |                              |                            |
            v                              v                            v
+------------------------+      +---------------------+      +---------------------+
|  VulnerabilityEngine   | <--- | LawEnforcementEngine| <--- | ProceduralMissions  |
+------------------------+      +---------------------+      +---------------------+
```

## System Interconnections

1. **Global Living AI (Milestone 1.5.0)**:
   - `WorldSimulationEngine` runs hourly simulation ticks controlling organization departments, dynamic CVE zero-day vulnerabilities, law enforcement heat tracking, employee resignations, and corporate budget shifts.

2. **Law Enforcement & Heat**:
   - Illegal port scans, exploit injections, data leaks, and dark market deals build Global Heat. High heat triggers active agency investigations, account flags, and darknet proxy route blocks.

3. **Organization AI & Patch Management**:
   - Security departments react to probing by activating IDS and raising firewall levels. Exploit occurrences force companies to schedule and execute software patches (`ServerPatched`).

4. **Procedural Missions V2**:
   - Mission contracts are dynamically generated from real world events (employee resignations, corporate data leaks, zero-day publications, red team pen testing).

5. **Abyss Web Matrix & Dark Layer**:
   - Websites, forums, and darknet rumor networks continuously reflect real simulation events (press advisories, leak threads, market price shifts).

6. **Save & State Persistence**:
   - `SaveManager` preserves VFS data, browser state, darknet state, world simulation state, vulnerabilities, and law enforcement heat across sessions.
