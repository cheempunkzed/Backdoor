# AI_CHANGELOG.md
# Log of AI Developer Actions

## Session 2026-07-31 (0.8.1 System Integration)
- Identified the issue of Application UI states not surviving Window Close/Minimize/Restore actions.
- Introduced `AppState` hierarchy containing concrete data classes for each OSApp (`TerminalAppState`, `BrowserAppState`, etc.).
- Bound `AppState` to `OSProcess` in `ProcessManager`.
- Refactored `DesktopScreen`, `WindowManager` and individual App Composables (`TerminalApp`, `BrowserApp`, `NetworkApp`, `FilesApp`) to read and write state from `AppState`.
- Implemented `SystemEventBus` providing decoupled communication streams.
- Connected `AbyssOSManager` to `SystemEventBus` to handle `AppRequested` and `NotificationTriggered` globally.
- Updated `OnionCommand` (Terminal) to dispatch `OnionRouteEstablished` and `AppRequested(BROWSER)` events to automatically boot the browser and navigate to the newly constructed `.onion` domain.
- Audited the architecture for System Integration compliance.
- Updated `/docs/AI_CONTEXT.md` to reflect the 0.8.1 state and 0.9.0 goals.
- Created `/docs/PROJECT_STATE.md` to track project status.
- Created `/docs/AI_CHANGELOG.md` to track AI interactions.
- Updated `CHANGELOG.md` and `VersionHistory.md`.
Milestone 9 Shadow Economy completed.
