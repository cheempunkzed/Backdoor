# Living World Simulation

The core objective of Milestone 10 is to transform the digital world into an autonomous ecosystem where organizations, employees, servers, forums, and the economy operate independently of the player.

The `LivingWorldEngine` serves as the central orchestrator, executing background simulation loops (ticks) and managing the state of the entire ecosystem.

Key Features:
- Incremental background simulation (no expensive loops).
- Autonomous organization and employee logic.
- Dynamic incident generation and resolution.
- Integration with the shadow economy.

All systems are reactive via `StateFlow` and decoupled via `SystemEventBus`.
