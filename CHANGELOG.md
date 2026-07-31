# AbyssOS Changelog

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
- **Global Corporate Entity Generation**: 105 distinct organizations across 8 sectors (Financial, Tech, Defense, Energy, Pharma, Telecom, Logistics, Cyber) with domains, employee counts, and security clearance levels.
- **Data Centers & Server Rack Topology**: Tier I-IV data centers housing 2,500+ corporate servers with rack IDs, IP addresses, service ports, and OS builds.
- **Grid Navigation UI**: Tree view and sector filtering in Network App for exploring global corporate networks.

---

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
- **Save Compatibility**: Fully compatible with previous save files, maintaining seamless upgrades to version 0.3.0.

---

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

---

## [0.1.0] - 2026-07-30
### Initial Release
#### Added
- **Base AbyssOS Architecture**: Initial OS state manager, desktop UI, taskbar, notifications.
- **Authentication System**: User registration and login flows.
- **Core Applications**: Terminal, Files (In-memory initial version), Browser, Network, DarkNet, Settings, Logs, System Monitor.
- **Save Management**: Room database integration (`AppDatabase`) for user profile and save slot storage.
- **CI/CD Integration**: GitHub Actions workflow for automatic Debug APK creation on commit/push.
