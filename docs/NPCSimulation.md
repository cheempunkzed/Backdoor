# Darknet NPC Simulation System (v1.4.0)

## Overview
NPCs in AbyssOS v1.4.0 act as living darknet entities with individual reputations, PGP fingerprints, personalities, and faction affiliations.

## NPC Identities
- **ghost_operator**: Shadow Admin & Elite Researcher (`[GHOST]`)
- **cipher_fox**: Cypher Syndicate Leaker (`[CYPHER]`)
- **void_phantom**: Void Sect Market Dealer (`[VOID]`)
- **aegis_sentinel**: Aegis Red Security Mercenary (`[AEGIS]`)
- **black_lotus**: Zero-Day Broker (`[GHOST]`)
- **nexus_whistleblower**: Corporate Insider & Whistleblower (`[CYPHER]`)

## Autonomous Behavior
- **Forum Posting**: NPCs post threads and replies on hidden service forums during background simulation ticks.
- **Messaging**: NPCs send encrypted PGP messages (`EncryptedMessage`) to player identities containing rumors, tips, or market offers.
- **Rumor Spreading**: NPCs generate and amplify rumors via `RumorEngine`.
