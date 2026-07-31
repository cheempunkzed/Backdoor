# Corporate Network Infrastructure Architecture

## Overview

AbyssNet 0.6.0 models corporate infrastructure down to individual server classes, rack placement, data center facilities, and network topology schemes.

## Server Node Categories

1. **Authentication Server** (`AUTHENTICATION`, Port 88): Kerberos / Active Directory / LDAP.
2. **Database Node** (`DATABASE`, Port 5432): PostgreSQL / Oracle / Spanner data engine.
3. **Web Portal** (`WEB`, Port 443): Public HTTPS enterprise website.
4. **DNS Server** (`DNS`, Port 53): BIND9 domain resolution server.
5. **Mail Gateway** (`MAIL`, Port 25): SMTP / Exchange mail router.
6. **Backup Vault** (`BACKUP`, Port 8080): Encrypted rsync cold storage.
7. **File Storage** (`FILE`, Port 445): SMB / NFS network attached storage.
8. **Reverse Proxy** (`PROXY`, Port 8000): HAProxy / Envoy load balancer.
9. **VPN Gateway** (`VPN`, Port 1194): WireGuard / OpenVPN secure tunnel.
10. **Internal API Mesh** (`INTERNAL_API`, Port 8081): gRPC / REST microservice cluster.

## Network Topologies

- **Star Topology**: Centralized distribution core with direct branch connections.
- **Hierarchical Tree**: Core, distribution, and access tiers for enterprise networks.
- **Redundant Mesh**: Interconnected nodes providing high fault tolerance.
- **Hybrid Enterprise**: Combined star and mesh configuration for optimal performance.

## System Integration

- **AbyssNetworkEngine**: All corporate servers are registered into the primary node repository and DNS resolver.
- **Browser App**: Visiting corporate domains renders custom branded Web Portals with live server statuses.
- **System Monitor**: Displays live corporate grid telemetry (105 Orgs, 2500+ Nodes, Data Centers).
- **SaveManager**: Serializes corporate grid state for persistent session state.
