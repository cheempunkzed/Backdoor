# World Simulation Engine Architecture (v1.5.0)

## Overview
The `WorldSimulationEngine` (`com.example.backdoor.simulation.engine.WorldSimulationEngine`) orchestrates the living AI digital civilization in Backdoor. The simulation runs autonomously in the background via event-driven triggers and hourly in-game ticks without requiring player intervention.

## Core Capabilities
- **Global Clock & Ticks**: Coordinates `GameClock` (1 hour in-game time per background tick) with priority-cached updates.
- **Autonomous Subsystems**:
  - `LivingWorldEngine`: Employee shifts, incident generation, NPC social engine, darknet forum simulation.
  - `OrganizationAI`: Departmental decision-making (CEO, Security, HR, IT, Finance, Legal).
  - `VulnerabilityEngine`: Dynamic CVE publication, zero-day discovery, patch lifecycle.
  - `LawEnforcementEngine`: Global player heat tracking, agency surveillance levels, proxy route blocking.
  - `ProceduralMissionGenerator`: Event-driven contract generation based on real world incidents.
  - `OnionNetworkEngine`: Darknet market, rumor engine, identity switching.
  - `WebContentEngine`: Dynamic corporate website advisories, press releases, security status updates.
- **Save State Persistence**: Complete state serialization (`serializeToJson` / `deserializeFromJson`) stored in `SaveManager`.
