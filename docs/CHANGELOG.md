# AbyssOS Changelog

All notable changes to the Backdoor project will be documented in this file.

## [1.2.0] - 2026-08-03
### Development Milestone 1.2.0: Abyss Contracts Engine
#### Added
- **Validated Objective System (`Objective` & `ObjectiveType`)**: Modular task objectives embedded into contracts tracking network scans, service discovery, security assessment, file downloads/deletions, terminal commands, darknet onion visits, forum posts, wallet transactions, and market purchases.
- **Event-Driven Observer (`SystemEventBus`)**: Decoupled gameplay observer in `ContractManager` listening to real-time `SystemEvent` emissions across all OS subsystems.
- **Completion Guard (`CompletionValidator`)**: Strict validator ensuring rewards are granted ONLY when all contract objectives, state requirements, and expiration rules are satisfied.
- **Dynamic Multi-Step Contracts**: Dynamic contract generator producing multi-objective corporate, security, network, darknet, and economic missions.
- **Enhanced Contracts UI (`ContractsApp.kt`)**: Interactive objective checklists with live progress counts (`✓` completed, `✗` pending), percentage counters, and protected reward claim controls.
- **Mission Persistence**: Full JSON serialization of active contracts, objective progress, and mission history in `ShadowEconomyEngine` & `SaveManager`.

## [1.1.0] - 2026-08-01
### Development Milestone 1.1.0: Abyssos Hybrid Interface
#### Added
- **Application Display Modes (`ApplicationDisplayMode`)**: Full support for `FULLSCREEN`, `WINDOWED`, and `MINIMIZED` application display modes across all 14 OS applications.
- **Mobile Cyber Header & Back Navigation**: Dedicated mobile control header for fullscreen applications with back/minimize controls, app icon badge, and mode switcher.
- **Hybrid Window Manager Extensions**: Extended `WindowManager` and `AbyssOSManager` to seamlessly switch display modes while retaining process state (`AppState`) without app recreation.
- **Enhanced Mobile Task Dock**: Expanded `BottomDock` taskbar with running app indicators, background process status, and contextual app menus ("Open Fullscreen", "Open Windowed", "Pin to Dock", "Close App").
- **Window Title Bar Controls**: Added maximize/fullscreen and minimize buttons to floating `WindowFrame` components in windowed mode.

## [1.0.0] - 2026-07-31

## [0.8.1] - 2026-07-31
### Development Milestone 8.1: System Integration
#### Added
- **System Event Bus (`SystemEventBus`)**: Centralized event-driven architecture allowing decoupled communication between OS components (NetworkStatusChanged, OnionRouteEstablished, FileDownloaded, AppRequested, NotificationTriggered).
- **Application State Preservation (`AppState`)**: Abstracted UI states into `AppState` objects attached to `OSProcess` entries. Applications now retain their full state (input fields, tabs, navigation history) even when their windows are minimized, restored, or closed.
- **Process Manager Expansion**: Integrated `AppState` into `OSProcess`. Implemented seamless mapping between UI windows and background processes without restarting applications upon window focus changes.
- **Terminal Integration (`SystemEventBus`)**: `onion` command now automatically pushes an `OnionRouteEstablished` event and launches Abyss Browser directly via event bus.

## [0.8.0] - 2026-07-31
### Development Milestone 8: Dark Layer (Onion Network)
#### Added
- **Onion Network Architecture (`OnionNetworkEngine`)**: Multi-hop encrypted circuit simulation, relay node management (Alpha, Bravo, Charlie, Delta, Echo), active state flow, and `DarkNetRoutingHook` implementation.
- **Hidden Services Infrastructure (`HiddenService`)**: 8 indexed `.onion` hidden services (`dir.onion`, `abyss-forum.onion`, `blackvault.onion`, `cipherroom.onion`, `whistleblower.onion`, `darkmarket.onion`, `shadowblog.onion`, `zero-day.onion`) with access clearance levels (`PUBLIC`, `REGISTERED`, `TRUSTED`, `PRIVATE`, `ELITE`).
- **Underground Forum Subsystem**: `ForumThread` & `ForumPost` engine with thread creation, post replies, upvote tracking, category filtering, and real-time state recomposition.
- **NPC Digital Identity System (`AnonymousIdentity`)**: 5 simulated darknet personalities (`@v0id_walker`, `@cypher_ghost`, `@k3rnel_panic`, `@shadow_weaver`, `@root_daemon`) with knowledge domains, affiliations, and community ranks.
- **Reputation & Trace Metric Engine (`UserReputation`)**: Tracks Trust Score, Fame Score, Suspicion metric, and calculates dynamic Community Ranks (`NOVICE`, `MEMBER`, `OPERATOR`, `CYPHERPUNK`, `SHADOW_ADMIN`) unlocking restricted `.onion` portals.
- **Dark Market Foundation**: `DarkMarketListing` directory on `darkmarket.onion` displaying verified hardware schematics, exploit kits, and corporate network maps with seller ratings and credit pricing.
- **Abyss Browser Onion Subsystem**: Full `.onion` domain navigation support, quick bookmark bar chips, onion-routed circuit status banner, interactive forum viewer/posting UI, dark market listing view, and hidden node connector.
- **Terminal Integration (`OnionCommand`)**: `onion` / `darknet` / `relays` command for inspecting 3-hop relay nodes, hidden services directory, player reputation card, and node connections.
- **Save System Integration**: `SaveManager` extension with `getDarknetJson()` and `saveDarknetJson()` for full darknet state & discovered addresses persistence.
- **Comprehensive Documentation Suite**: Added `/docs/OnionNetwork.md`, `/docs/SocialSystem.md`, `/docs/NPCArchitecture.md`, and `/docs/ReputationSystem.md`.

## [0.7.0] - 2026-07-31
### Development Milestone 7: Offensive Security Framework
#### Added
- **Central Security Framework Engine (`OffensiveSecurityFramework`)**: Module registration, asynchronous task execution via Coroutines, research session tracking, and saved state serialization.
- **Port Scanner Engine (`PortScannerEngine`)**: Non-destructive TCP/UDP port probing, banner grabbing, response latency calculation, and open service indexing across corporate servers and network nodes.
- **Service Configuration Scanner (`ServiceScanner`)**: Evaluates patch level compliance %, configuration ratings (Grade A to F), firewall defense grades (Hardened, Standard, Exposed), and security clearance tiers.
- **In-Game Knowledge Database (`KnowledgeDatabase`)**: Technical Wiki containing articles on router architectures, directory services, directory traversal, protocol handshakes, and security tiers, accessible via Browser (`wiki.abyss`) and Terminal (`sec wiki`).
- **Markdown Security Report Generator (`SecurityReportGenerator`)**: Formats and automatically exports structured markdown audit reports to `/home/operator/reports/` on AbyssFS.
- **Network Application Security Control Center**: Tab 4 (SECURITY) featuring target selection, analysis module grid, live execution console with progress bar, and real-time security posture metrics (0-100 gauge).
- **System Monitor Integration**: Added Offensive Security Telemetry Engine card displaying active tasks, running job progress, and generated report counters.
- **Terminal Integration (`SecurityCommand`)**: `security` / `sec` / `scan` command for running scans, listing modules, querying the knowledge database, and inspecting session history.
- **Milestone 8 Preparedness**: Added `DarkNetRoutingHook` interface for seamless future DarkNet onion routing integration.

## [0.6.0] - 2026-07-31
### Development Milestone 6: Corporate Grid
#### Added
- **Corporate Grid Subsystem**: Added `CorporateModels` (`IndustryType`, `ServerType`, `DataCenter`, `CorporateServer`, `Organization`, `NetworkTopologyType`) and `CorporateGridRepository`.
- **Global Corporate Entity Generation**: 105 distinct corporate organizations across 11 industry sectors operating 2,500+ server nodes and 200+ data center facilities with domains, employee counts, and security clearance levels.
- **Network Engine & DNS Integration**: All corporate nodes and primary domains (`aegis-corp.com`, `apex-financial.net`, etc.) are auto-registered with `AbyssNetworkEngine` and `DomainResolver`.
- **Network App Corporate Grid GUI**:
  - Tab 1: Local Subnet Topology Map.
  - Tab 2: Corporate Grid Hierarchical Tree (Industry -> Organization -> Data Center -> Server Node).
  - Tab 3: Organizations Directory with search bar, Industry filter chips, Security Tier filters, and Organization Inspector panel.
  - Tab 4: Network Interfaces Monitor (`ifconfig`, `netstat`).
- **Corporate Web Browser**: Visiting corporate domains renders styled Corporate Web Portals with mission statements, live server lists, and security clearance indicators.
- **System Monitor Integration**: Added Network Infrastructure telemetry monitor card displaying active orgs, total server count, data centers, and subnets.
- **SaveManager Persistence**: Added `getCorporateGridJson()` and `saveCorporateGridJson()` for corporate grid session state.
- **Documentation**: Updated `/docs/Roadmap.md`, `/docs/Architecture.md`, `/docs/Networking.md`, `/docs/VersionHistory.md`, `/docs/CHANGELOG.md`, and added `/docs/Organizations.md` and `/docs/Infrastructure.md`.
#### Fixed
- **Terminal Input & Cursor UX Fix**: Fixed cursor positioning using native `cursorBrush` property in `BasicTextField` without altering Prompt Engine or Prompt styles.

## [0.5.0] - 2026-07-31
### Stage 6: Network Engine (AbyssNet Core)
#### Added
- **AbyssNet Core Subsystem**: Engine (`AbyssNetworkEngine`), repository (`InMemoryNetworkRepository`), DNS resolver (`DomainResolver`), latency simulator (`LatencySimulator`), packet router (`PacketRouter`), and network scanner (`NetworkScanner`).
- **Network Terminal Commands**: `ping`, `traceroute`, `netstat`, `ipconfig`/`ifconfig`, `arp`, `nslookup`, `whois`, `route`.
- **Network App GUI**: Subnet topology map view, host search & filter table, device detail inspector, and active interface monitor.
- **Browser App**: DNS-backed web navigation rendering local router portal (`router.local`), developer dashboard (`localhost`), network diagnostic hub (`about:network`), and host pages.

## [0.4.0] - 2026-07-30
### Stage 5: Terminal Core + Command Engine & Extensibility
#### Added
- **Command Registry & Command Pattern**: Extensible command registry (`CommandRegistry`) supporting dynamic registration, search, autocomplete, and man page inspection without massive switch/when logic.
- **Advanced Command Parser**: Shell parser (`CommandParser`) supporting single-char flags (`-la`), long options (`--opt=val`), positional arguments, double-quote escaping, and pipelines (`|`, `&&`, `||`).
- **Terminal Session & Custom Prompts**: Environment variables (`PATH`, `HOME`, `USER`, `HOSTNAME`, `PWD`, `SHELL`), aliases, and custom prompt styles (`DEFAULT`, `SHORT`, `MINIMAL`, `CYBER`).
- **Comprehensive Terminal Commands**:
  - Filesystem: `ls`, `cd`, `pwd`, `cat`, `touch`, `mkdir`, `rm`, `mv`, `cp`, `find`, `tree`, `rename`, `chmod`, `stat`.
  - Utility & System: `help`, `man`, `clear`, `echo`, `history`, `whoami`, `hostname`, `date`, `time`, `version`, `exit`, `open`.
- **System Logging**: Automatic logging of command execution and terminal events into `/logs/terminal.log`.
- **Terminal UI Polish**: Built-in settings modal (cursor blink toggle, font size slider, prompt style selector), quick command toolbar, autocomplete suggestion chips, and selection/copy support.
- **Centralized Localization Engine**: `StringManager` supporting multi-language system strings (English & Russian).
- **Future Extensibility Architecture**: Decoupled interface contracts for `JobManager`, `PackageManager`, `NetworkService`, `ScriptInterpreter`, and `CronScheduler`.
- **Save State Compatibility**: Persists terminal command history and custom terminal settings safely across sessions.

## [0.3.0] - 2026-07-30
### Stage 4: Desktop Environment 2.0 + Process Manager
#### Added
- **True Immersive Fullscreen**: Hidden Android status bar & gesture navigation using `WindowInsetsControllerCompat`.
- **Process Manager Engine**: Real virtual process kernel (`ProcessManager`) tracking PIDs, status, CPU %, RAM MB, uptime, and daemon protection.
- **Multi-Window Support**: Window Manager (`WindowManager`) supporting multiple open windows simultaneously with zIndex stacking, focus, minimize, restore, and drag-repositioning.
- **Redesigned Files App**: Dual-pane file manager with Top Inspector & Action Bar (Open, Read, Edit, Copy, Move, Rename, Delete) and Bottom Tree Sidebar + Folder Grid.
- **Redesigned Bottom Dock**: Larger, customizable dock with pin/unpin functionality, active running indicators, and persistent state (`saveDockPinnedAppsJson`).
- **Redesigned Desktop Environment**: 4x6 grid layout with auto-align, persistent icon positioning (`saveDesktopPositionsJson`), and smooth scale/alpha animations.
- **Custom Top Bar**: Redesigned minimalist top status bar featuring a soft pulsing shield icon, real-time CPU/RAM meters, network indicator, battery percentage, and clock/date display.
- **Context Menus**: Long-press popups for Files, Desktop wallpaper, and App icons.
- **Save Compatibility**: Fully compatible with previous save files, maintaining seamless upgrades.

## [0.2.0] - 2026-07-30
### Stage 3: Virtual File System (AbyssFS)
#### Added
- **AbyssFS Architecture**: Implemented full persistent Virtual File System engine (`InMemoryVirtualFileSystem`).
- **Standard Linux Hierarchy**: Created system root directories: `/`, `/bin`, `/boot`, `/dev`, `/etc`, `/home`, `/tmp`, `/usr`, `/var`, `/logs`, `/network`, `/desktop`, `/downloads`, `/documents`, `/darknet`, `/system`.
- **User Home Management**: Automatic initialization of `/home/<username>` with standard subdirectories (`Desktop`, `Downloads`, `Documents`, `Notes`, `Scripts`, `Trash`).
- **Rich File Types & Metadata**: Full support for extensions (`.txt`, `.log`, `.cfg`, `.sys`, `.key`, `.enc`, `.exe`, `.sh`, `.net`, `.tmp`) with size, timestamps, owner, permissions, hidden and system-protected flags.
- **Terminal Commands (Command Pattern)**:
  - `cd`: Navigation with relative/absolute paths & tilde expansion.
  - `mkdir`: Create directory hierarchy.
  - `touch`: Create files / update timestamps.
  - `rm`: Move to Trash or permanent removal (`-f`).
  - `mv`: Relocate / rename nodes.
  - `cp`: Copy files/directories recursively.
  - `cat`: Read file contents.
  - `pwd`: Print working directory.
  - `ls`: List directory contents with `-a` (hidden) and `-l` (long format).
  - `tree`: Display ASCII tree hierarchy.
  - `find`: Global file search by query.
  - `open`: Inspect or execute files/directories.
  - `rename`: Rename files and folders.
  - `version`: Display kernel & AbyssFS version details (`AbyssOS 0.2.0`).
- **FilesApp File Manager**:
  - Breadcrumb path navigation & back buttons.
  - Dual-pane layout: Directory tree on left, Inspector & Context Actions on right.
  - Real-time search, sorting (Name, Size, Date, Type).
  - Modal dialogs for File creation, Editing, Folder creation, Renaming, Copying, Moving, File Info.
  - Interactive Trash manager with Restore and Permanent Delete options.
- **System Protection**: Protection against deleting `/system`, `/bin`, `/boot`, `/dev`, `/etc`, or `/`.
- **Audit Logging**: Automatic action logging into `/logs/fs_audit.log`.
- **Persistence Layer**: JSON serialization/deserialization tied to Room database save slots (`vfsDataJson`).

## [0.1.0] - 2026-07-30
### Initial Release
#### Added
- **Base AbyssOS Architecture**: Initial OS state manager, desktop UI, taskbar, notifications.
- **Authentication System**: User registration and login flows.
- **Core Applications**: Terminal, Files (In-memory initial version), Browser, Network, DarkNet, Settings, Logs, System Monitor.
- **Save Management**: Room database integration (`AppDatabase`) for user profile and save slot storage.
- **CI/CD Integration**: GitHub Actions workflow for automatic Debug APK creation on commit/push.
