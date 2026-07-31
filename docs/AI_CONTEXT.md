# AI_CONTEXT.md
# Backdoor Development Context
# Last Updated: Version 1.0.0 (Living Grid)
# Next Target: Version 1.0.0 (Living Grid)

==================================================
PROJECT OVERVIEW
==================================================

Project Name:
Backdoor

Genre:
Immersive Hacker Simulator / Virtual Operating System / Sandbox RPG

Platform:
Android

Language:
Kotlin

Framework:
Jetpack Compose

Architecture:
MVVM + Repository + StateFlow + Coroutines + Event Driven Architecture

Development Style:
Incremental SDLC (Software Development Life Cycle)

The project is developed milestone-by-milestone.
Backward compatibility is mandatory.
Existing systems must never be rewritten unless absolutely necessary.
Always extend existing architecture.

==================================================
CORE PHILOSOPHY
==================================================

Backdoor is NOT a hacking minigame.

Backdoor is a living virtual operating system.

The player should feel that they are using a real computer connected to a living Internet.

Every application exists for a reason.

Every subsystem interacts with other subsystems.

The world continues to evolve even if the player does nothing.

Immersion is always prioritized over visual effects.

==================================================
CURRENT IMPLEMENTED SYSTEMS
==================================================

✔ AbyssOS Desktop Environment

✔ Multi Window Manager

✔ Process Manager

✔ Taskbar

✔ System Tray

✔ Window Sessions

✔ Process Lifecycle

✔ Application Manager

✔ SystemEventBus

✔ Notification Center

✔ AbyssFS

✔ Terminal Core

✔ Command Registry

✔ Linux-like Shell

✔ Browser

✔ Corporate Grid

✔ AbyssNet

✔ Onion Network

✔ Hidden Services

✔ Reputation System

✔ NPC Identity System

✔ Security Framework

✔ SaveManager

==================================================
CURRENT APPLICATIONS
==================================================

Terminal

Files

Browser

Network

DarkNet

Settings

Logs

System Monitor

==================================================
TERMINAL
==================================================

Architecture:

Command Pattern

Parser

Autocomplete

History

Environment Variables

Aliases

Prompt Styles

Supported Prompt Styles:

DEFAULT

SHORT

MINIMAL

CYBER

Never remove existing prompt styles.

Always preserve backward compatibility.

==================================================
FILESYSTEM
==================================================

Filesystem:

AbyssFS

Linux hierarchy

Permissions

Ownership

Metadata

Trash

Audit Logs

Persistent Save

==================================================
NETWORK
==================================================

AbyssNet

DNS

Packet Routing

Latency Simulation

Corporate Infrastructure

Local Networks

Routing Tables

Network Scanner

==================================================
CORPORATE GRID
==================================================

105+

Organizations

2500+

Servers

200+

Data Centers

Multiple Industries

Network Topologies

==================================================
SECURITY FRAMEWORK
==================================================

Port Scanner

Service Scanner

Knowledge Database

Markdown Reports

Security Commands

==================================================
ONION NETWORK
==================================================

Relay Nodes

Hidden Services

Dark Market

Forums

NPC Reputation

Community Ranks

Anonymous Identity

Browser Integration

==================================================
SYSTEM INTEGRATION
==================================================

Applications communicate only through

SystemEventBus

Never create hard dependencies between applications.

Use events whenever possible.

Application state belongs to the Process.

Windows only display Process state.

Applications must survive:

Window switching

Minimize

Restore

Background

Foreground

==================================================
WINDOW MANAGEMENT
==================================================

Every application has:

OSProcess

ApplicationState

WindowSession

WindowState

Taskbar Entry

Tray Support

Applications must never restart when switching windows.

==================================================
DESIGN RULES
==================================================

Never fake interactions.

Everything should be simulated.

Avoid scripted behavior.

Systems should generate behavior naturally.

Player actions must have consequences.

The game should reward curiosity.

The UI should resemble a professional operating system.

==================================================
PERFORMANCE
==================================================

Prefer:

StateFlow

Immutable UI State

LazyColumn

LazyGrid

Coroutines

Avoid:

Blocking UI Thread

Memory leaks

Large recompositions

Duplicate repositories

Duplicate state holders

==================================================
SAVE SYSTEM
==================================================

Everything must persist.

Never introduce features that disappear after restart.

All new systems must integrate with SaveManager.

==================================================
DOCUMENTATION POLICY
==================================================

Every milestone updates:

CHANGELOG.md

Architecture.md

Roadmap.md

VersionHistory.md

AI_CONTEXT.md

New systems require their own documentation.

==================================================
CURRENT DEVELOPMENT STATUS
==================================================

Current Stable Version:

0.8.1

Current Codename:

SYSTEM INTEGRATION

Next Version:

1.0.0

Next Codename:

LIVING GRID

==================================================
LONG TERM GOALS
==================================================

Living Internet

Living DarkNet

Dynamic Economy

Reactive Organizations

Persistent NPCs

Living Forums

Realistic Server Infrastructure

Dynamic Contracts

AI-driven Corporate Behavior

Complete Hacker Sandbox

==================================================
IMPORTANT RULES FOR AI
==================================================

Never replace existing systems.

Always extend architecture.

Maintain backward compatibility.

Reuse existing managers whenever possible.

Integrate through SystemEventBus.

Do not create duplicate implementations.

Maintain immersive UX.

Prefer modular architecture.

Always compile successfully.

Never leave placeholder implementations without explicit TODO markers.

Every new feature must interact with at least one existing subsystem.

Backdoor should always feel like a real operating system rather than a collection of independent screens.
