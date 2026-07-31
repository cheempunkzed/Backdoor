# World Clock

The `GameClock` provides a global time system for the simulation.

Features:
- Tracks Year, Month, Day, Hour, and Minute.
- Operates on a coroutine-based tick system, independent of real-world time.
- Exposes time state via `StateFlow` for UI components and simulation engines.

The World Clock drives schedules (e.g., employee work hours), contract deadlines, and time-sensitive events.
