# NPC Architecture & Living World Integration (v1.3.0)

## Overview
NPCs in Backdoor are active agents within the cyberpunk simulation. They have distinct personalities, security roles, affiliations, and behaviors that directly influence web content, darknet forums, corporate grids, and mail systems.

## Integration Architecture

### 1. LivingWorldEngine (`com.example.backdoor.simulation.engine.LivingWorldEngine`)
- Simulates corporate employee routines, security incidents, market updates, and news publication.
- Links corporate employees (`Employee`) with workplace status, security access levels, and department roles.

### 2. Anonymous Identities (`AnonymousIdentity`)
- Anonymous handles in darknet forums (`@zero_cool`, `@shadow_reaper`, `@cipher_queen`, `@hex_ghost`).
- Dynamic forum post generation reacting to server breaches, corporate leaks, and market changes.

### 3. Mail & Social System Integration
- Direct communication with NPCs for contracts, warnings, tips, and leaked access keys.
- Real-time event notifications delivered directly to the player's inbox.
