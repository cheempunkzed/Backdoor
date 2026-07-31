# Changelog

All notable changes to the Backdoor project will be documented in this file.

## [0.5.0] - 2026-07-31

### Added
- **AbyssNet Core Subsystem**: Engine (`AbyssNetworkEngine`), repository (`InMemoryNetworkRepository`), DNS resolver (`DomainResolver`), latency simulator (`LatencySimulator`), packet router (`PacketRouter`), and network scanner (`NetworkScanner`).
- **Network Terminal Commands**: `ping`, `traceroute`, `netstat`, `ipconfig`/`ifconfig`, `arp`, `nslookup`, `whois`, `route`.
- **Network App GUI**: Subnet topology map view, host search & filter table, device detail inspector, and active interface monitor.
- **Browser App**: DNS-backed web navigation rendering local router portal (`router.local`), developer dashboard (`localhost`), network diagnostic hub (`about:network`), and host pages.
- **Save State Persistence**: Added `getNetworkTopologyJson()` and `saveNetworkTopologyJson()` to persist network topology across app reboots.
- **Documentation**: Added `/docs/Roadmap.md`, `/docs/Architecture.md`, `/docs/Networking.md`, `/docs/VersionHistory.md`, and `/docs/CHANGELOG.md`.

### Fixed
- Fixed missing input field in terminal view.
- Cleaned up top status bar (removed CPU/RAM percentage and date, preserving clean system time).
- Injected `AbyssNetworkEngine` into `AbyssOSManager` context.
