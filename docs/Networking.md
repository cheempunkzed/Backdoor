# AbyssNet Virtual Network Engine

## Specifications & Topology Architecture

### Overview
AbyssNet is the virtual networking subsystem of AbyssOS (v0.5.0). It simulates local networks (LAN), gateways, subnets, domain resolution, latency jitter, and standard network commands.

### Topology Data Models
- **`NetworkNode`**: Represents a host (Router, Server, PC, IoT device) with IP, MAC, status, security clearance, and exposed services.
- **`NetworkConnection`**: Defines link state, bandwith limit (Mbps), and latency between node pairs.
- **`Packet`**: Simulates ICMP/TCP/UDP data packets with TTL, source/destination IPs, and payload size.
- **`DomainResolver`**: In-memory DNS mapping hostnames (e.g., `router.local`, `darknet.local`, `abyss.net`) to internal IP addresses.

### Supported Network Commands
| Command | Category | Description |
|---|---|---|
| `ping <host>` | NETWORK | Sends ICMP echo requests to verify connectivity and measure latency. |
| `traceroute <host>` | NETWORK | Traces route hops to destination host. |
| `netstat` | NETWORK | Lists active network sockets and listening ports. |
| `ipconfig` / `ifconfig` | NETWORK | Displays network interface details (`eth0`, `wlan0`, `tun0`). |
| `arp` | NETWORK | Displays Address Resolution Protocol IP-to-MAC translation table. |
| `nslookup <domain>` | NETWORK | Queries DNS domain name resolution. |
| `whois <domain>` | NETWORK | Looks up registration records for network domain names. |
| `route` | NETWORK | Displays system routing table entries. |

### Default LAN Topology (Subnet 192.168.1.0/24)
- `192.168.1.1` - `router.local` (AbyssRouter Gateway)
- `192.168.1.104` - `player-pc.local` (Local Terminal Host)
- `192.168.1.10` - `nas-storage.local` (Network Attached Storage)
- `192.168.1.50` - `smart-tv.local` (IoT Device)
- `192.168.1.254` - `dns.gateway.local` (Subnet DNS Server)
