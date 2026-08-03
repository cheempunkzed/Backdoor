# Law Enforcement & Heat Engine (v1.5.0)

## Overview
The `LawEnforcementEngine` (`com.example.backdoor.simulation.engine.LawEnforcementEngine`) models global cyber surveillance, law enforcement investigations, and criminal heat tracking.

## Heat Levels (`HeatLevel`)
- **NORMAL** (0% - 24%): Low / clear background surveillance.
- **SUSPICION** (25% - 49%): Elevated monitoring by Aegis Counter-Intel. Target firewalls increase sensitivity.
- **ACTIVE_INVESTIGATION** (50% - 74%): Investigation initiated by Global Cyber Command (`InvestigationStarted`). Target servers active IDS mode.
- **CRITICAL_RAID_RISK** (75% - 100%): High alert / INTERPOL Cyber Taskforce deployment. Proxy routes blocked (`isProxyRouteBlocked = true`).

## Heat Triggers & Decay
- **Heat Gain**:
  - Network Scan: +2.5%
  - Security Scan: +4.0%
  - Exploit Injection: +15.0%
  - Corporate Data Leak: +10.0%
  - Server Intrusion: +12.0%
  - Dark Market Purchase: +3.0%
- **Heat Decay**: Heat naturally decays over time (-1.5% per simulation tick) when player operates stealthily or uses anonymizing darknet identities.
