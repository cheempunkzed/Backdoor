# AbyssOS & Backdoor System Architecture

## Overview
Backdoor is structured as an offline-first modular OS simulation built on modern Android, Jetpack Compose, Kotlin Flow, and Clean Architecture.

```
+-----------------------------------------------------------------+
|                        Jetpack Compose UI                       |
|  [DesktopScreen] [TerminalApp] [NetworkApp] [BrowserApp]        |
+-----------------------------------------------------------------+
                                |
                                v
+-----------------------------------------------------------------+
|                       AbyssOS Kernel Manager                    |
|  [AbyssOSManager] [ProcessManager] [WindowManager]              |
+-----------------------------------------------------------------+
             |                                     |
             v                                     v
+--------------------------+          +---------------------------+
|    AbyssFS Subsystem     |          |    AbyssNet Subsystem     |
| [VirtualFileSystem]      |          | [AbyssNetworkEngine]      |
| [VfsNode] [Permissions]  |          | [InMemoryNetworkRepo]     |
+--------------------------+          | [DomainResolver]          |
                                      | [PacketRouter]            |
                                      | [LatencySimulator]        |
                                      +---------------------------+
                                                   |
                                                   v
                                      +---------------------------+
                                      |     Terminal Commands     |
                                      | ping, traceroute, netstat,|
                                      | ipconfig, arp, nslookup,  |
                                      | whois, route              |
                                      +---------------------------+
```

## Core Modules
1. **Core Kernel (`com.example.backdoor.game`)**: Manages boot sequence, process stack, notification pipeline, system ticker, and app state.
2. **Virtual File System (`com.example.backdoor.filesystem`)**: Hierarchical tree node model (`VfsNode`) with owner permissions and persistent JSON state.
3. **Virtual Network Subsystem (`com.example.backdoor.network`)**: Reactive event-driven networking stack (`AbyssNetworkEngine`) handling nodes, links, DNS resolution, synthetic ping latency, and traceroute hop calculation.
4. **Terminal Engine (`com.example.backdoor.terminal`)**: Lexical command parser, context executor, auto-complete engine, built-in POSIX/Net utilities.
5. **Persistence (`com.example.backdoor.save`)**: Encrypted & memory fallback save manager storing user profiles, VFS states, and network topology snapshots across app reboots.

- **Shadow Economy (v0.9.0)**: Contracts, Market, Wallet, Inventory, Mail, News.
