# Incident System

The `Incident` model defines anomalous events that occur within the simulated world.

Properties:
- `id`: Unique identifier.
- `type`: Category of the incident (e.g., POWER_FAILURE, DDOS, SYSTEM_CRASH).
- `severity`: Impact level (LOW, MEDIUM, HIGH, CRITICAL).
- `organizationId`: The affected organization.
- `targetServerId`: The specific server impacted (optional).
- `timestamp`: When the incident occurred.
- `description`: Textual details of the event.

Incidents are generated probabilistically by the `LivingWorldEngine` and broadcast via the `SystemEventBus`. They affect employee stress, organizational productivity, and generate news/contracts in the Shadow Economy.
