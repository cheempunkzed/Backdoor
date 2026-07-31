# Darknet Reputation System (v0.8.0)

## Overview
The Reputation System measures the player's standing, influence, and trace risk within the virtual darknet underground.

## Metrics & Parameters

1. **Trust Score (`trust`)**: Accumulated through verified security audit contributions, forum thread creation, and technical assistance.
2. **Fame Score (`fame`)**: Measures notoriety across darknet forums and market listings.
3. **Suspicion Metric (`suspicion`)**: Tracks network trace risk and perimeter alarms triggered during security operations.
4. **Community Rank (`rank`)**:
   - `NOVICE` (Score 0 - 299)
   - `MEMBER` (Score 300 - 799)
   - `OPERATOR` (Score 800 - 1999)
   - `CYPHERPUNK` (Score 2000 - 4999)
   - `SHADOW_ADMIN` (Score 5000+)

## Formula
$$\text{Reputation Score} = \max(0, \lfloor \text{Trust} \times 1.5 + \text{Fame} \times 0.8 - \text{Suspicion} \times 2.0 \rfloor)$$

## Unlocks
Higher reputation ranks unlock access to restricted `.onion` hidden services, private research subforums, and elite seller directories on the Shadow Exchange Market Foundation.
