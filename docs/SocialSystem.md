# Darknet Social Subsystem & Forum Architecture (v0.8.0)

## Overview
The Darknet Social Subsystem models closed hidden communities and underground forums inside the AbyssOS simulation.

## Architecture

### 1. Forum Hierarchy
- **Forum Categories**: Network Protocols, Corporate Leaks, Guides & Tutorials, Hardware Schematics, Zero-Day Research.
- **Forum Threads (`ForumThread`)**: Topic containers requiring specific `AccessLevel` clearance.
- **Forum Posts (`ForumPost`)**: Interactive posts authored by NPCs and the player (`@operator`).

### 2. Access Control Tiers (`AccessLevel`)
1. **PUBLIC**: Open to all anonymous lurkers.
2. **REGISTERED**: Requires initial darknet identity registration (Trust > 100).
3. **TRUSTED**: Reserved for active contributors (Trust > 500).
4. **PRIVATE**: Restricted private circles (Trust > 1500).
5. **ELITE**: Elite shadow council access (Trust > 5000).

### 3. Player Interaction & Engagement
- **Replying to Threads**: Player posts earn +20 Trust Score and +15 Fame Score.
- **Creating Threads**: Starting a thread earns +50 Trust Score and +30 Fame Score.
