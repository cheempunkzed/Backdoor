# Changelog

All notable changes to the Backdoor project will be documented in this file.

## [0.6.0] - 2026-07-31

### Added
- **Corporate Grid Subsystem**: Added `CorporateModels` (`IndustryType`, `ServerType`, `DataCenter`, `CorporateServer`, `Organization`, `NetworkTopologyType`) and `CorporateGridRepository`.
- **Global World Generation**: Generated 105 distinct corporate entities across 11 industry sectors operating 2500+ server nodes and 200+ data center facilities.
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

### Fixed
- **Terminal Input & Cursor UX Fix**: Fixed cursor positioning using native `cursorBrush` property in `BasicTextField` without altering Prompt Engine or Prompt styles.

## [0.5.0] - 2026-07-31

### Added
- **AbyssNet Core Subsystem**: Engine (`AbyssNetworkEngine`), repository (`InMemoryNetworkRepository`), DNS resolver (`DomainResolver`), latency simulator (`LatencySimulator`), packet router (`PacketRouter`), and network scanner (`NetworkScanner`).
- **Network Terminal Commands**: `ping`, `traceroute`, `netstat`, `ipconfig`/`ifconfig`, `arp`, `nslookup`, `whois`, `route`.
- **Network App GUI**: Subnet topology map view, host search & filter table, device detail inspector, and active interface monitor.
- **Browser App**: DNS-backed web navigation rendering local router portal (`router.local`), developer dashboard (`localhost`), network diagnostic hub (`about:network`), and host pages.
