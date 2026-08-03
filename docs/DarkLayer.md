# Abyss Dark Layer Architecture (v1.4.0)

## Overview
The Dark Layer represents the living underground ecosystem of AbyssOS. Extended in Milestone 1.4.0, it integrates digital identities, autonomous NPC life simulation, encrypted PGP messaging, underground factions, a dynamic rumor engine, and an expanded shadow market into a single reactive subsystem.

## Key Architectures

### 1. OnionNetworkEngine (`com.example.backdoor.darknet.engine.OnionNetworkEngine`)
- **Single Source of Truth**: Manages darknet connectivity, active circuit relays (`RelayNode`), player reputation (`UserReputation`), digital identities (`DigitalIdentity`), faction standings (`Faction`), encrypted inbox (`EncryptedMessage`), and registered `.onion` hidden services (`HiddenService`).
- **Circuit Routing**: Simulates multi-hop encrypted relay paths (Entry → Middle → Exit) with realistic latency and encryption protocols (RSA-4096 / AES-256-GCM).
- **Reputation & Heat**: Tracks `trust`, `fame`, `suspicion`, criminal heat level (`criminalHeat`), and `CommunityRank` (`NOVICE`, `MEMBER`, `OPERATOR`, `CYPHERPUNK`, `SHADOW_ADMIN`).

### 2. Digital Identity System (`DigitalIdentity.kt`)
- Allows operators to maintain multiple digital identities with unique PGP fingerprints, aliases, criminal heat metrics, and hidden profiles.
- Supports switching active identity on the fly via terminal command (`onion identity switch <alias>`) or UI.

### 3. Faction System (`Faction.kt`)
- Factions (`CYPHER_SYNDICATE`, `VOID_SECT`, `AEGIS_RED`, `GHOST_NETWORK`) operate distinct hidden services and forums.
- Tracks per-faction trust tiers (`UNTRUSTED`, `NEUTRAL`, `ACCEPTED`, `INNER_CIRCLE`) and reputation.

### 4. Rumor Engine & Leaks (`RumorEngine.kt` & `Rumor.kt`)
- Autonomous engine that generates leaks, rumors, and underground events (`UndergroundEvent`).
- Propagates rumors across darknet forums and injects fake/real corporate leaks into forum threads.

### 5. Encrypted Private Messaging (`EncryptedMessage.kt`)
- PGP-signed messaging system between NPCs and player identities.
- Terminal interface (`onion pm inbox`, `onion pm read`, `onion pm send`) and UI tab in `DarkNetApp`.

### 6. Shadow Exchange Marketplace (`DarkMarketListing`)
- Category-indexed listings with seller ratings, credit pricing, dynamic inventory stock, and buy actions linked to player balance.
