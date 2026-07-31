# Employee Simulation

The `Employee` model tracks individual NPC attributes and state within an organization.

Attributes:
- `position`: Job title and department.
- `salary`: Financial compensation.
- `skillLevel`: Competence in their role.
- `stressLevel`: Accumulated stress from work and incidents.
- `fatigue`: Physical and mental exhaustion.
- `loyalty`: Dedication to the organization.
- `productivity`: Output efficiency.
- `isAtWork`: Current status based on work schedules.

Employee behavior is governed by the `OrganizationAI` and `LivingWorldEngine`, responding to events, time of day, and incidents.
