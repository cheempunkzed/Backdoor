# AbyssNet Onion Network Architecture (v0.8.0)

## Overview
The Onion Network is the hidden, anonymous dark layer of AbyssNet in AbyssOS 0.8.0. It provides multi-hop encrypted communication routes between operator terminals and hidden services (`.onion` domain space).

## Key Components

### 1. Multi-Hop Relay Nodes (`RelayNode`)
All traffic inside the Onion Network is routed through a chain of 3 virtual relay nodes:
- **Entry Node (Guard)**: Receives packet stream from local AbyssOS terminal (`OnionRelay-Alpha`).
- **Middle Node**: Strips outer encryption layer and forwards to exit node (`OnionRelay-Bravo`).
- **Exit Node**: Decrypts payload and delivers request to `.onion` destination (`ExitNode-Delta`).

### 2. Indexed Hidden Services (`HiddenService`)
Hidden services operate on `.onion` virtual addresses without broadcasting geographic or IP location:
- `dir.onion`: Central directory of indexed darknet services.
- `abyss-forum.onion`: Underground cyber discussion forum.
- `blackvault.onion`: Classified leak repository and document dumps.
- `cipherroom.onion`: Encrypted multi-hop relay chat.
- `whistleblower.onion`: Corporate memo and blueprint upload portal.
- `darkmarket.onion`: Shadow exchange foundation (hardware schematics & exploit listings).
- `shadowblog.onion`: Technical write-ups and kernel mechanics blog.
- `zero-day.onion`: Closed elite research group.

### 3. Integration Hooks
The `OnionNetworkEngine` implements `DarkNetRoutingHook`, providing real-time circuit status and hidden service descriptors to the `OffensiveSecurityFramework`.
