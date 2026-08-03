# Abyss Web Matrix Documentation (v1.3.0)

## Overview
The **Abyss Web Matrix** turns the virtual network in Backdoor from static placeholders into a living simulated internet ecosystem. Every website is a digital entity tied to real organizations, servers, NPCs, economy, news, and player hacking actions.

## Core Components

### 1. WebContentEngine (`com.example.backdoor.web.engine.WebContentEngine`)
Central content engine responsible for:
- Lazy procedural generation of websites upon domain navigation (`getWebEntity`).
- Caching generated sites in memory to maintain consistency.
- Subscribing to `SystemEventBus` events (`ServerCompromised`, `CompanySecurityChanged`, `DataLeakDetected`, `MissionCompleted`, `MarketChanged`) to dynamically mutate web page content in real-time.
- Json serialization & deserialization via `SaveManager`.

### 2. Entity & Page Models (`com.example.backdoor.web.models.WebContentModels`)
- `WebEntity`: Represents a domain destination (`id`, `domain`, `name`, `entityType`, `pages`, `eventHistory`, `securityClearanceLevel`, `organizationId`).
- `WebPage`: Individual page sections (`sectionKey`, `title`, `content`, `lastUpdated`).
- `WebEntityType`: Categorizes sites (`CORPORATE`, `NEWS`, `DARKNET_FORUM`, `MARKETPLACE`, `RESEARCH`, `BLOG`, `PERSONAL`, `ROUTER_PORTAL`).

### 3. Dynamic Browser Integration (`BrowserApp.kt`)
- Multi-tab management: Each tab preserves independent URL state and back/forward navigation stack.
- Bookmarks & Categories: Organize bookmarks into `Corporate`, `Research`, `Darknet`, `Personal` filters with instant search.
- Visited History Manager: Searchable log of visited URLs with one-tap clear capability.
- Section Navigation: Web entities render interactive section tabs (`HOME`, `ABOUT`, `SERVICES`, `SECURITY STATUS`, `PRESS`, `CONTACT`, `EMPLOYEES`, `INCIDENTS`).
- Incident & Advisory Banners: Automatically display security breach advisories when player compromises corporate servers.
