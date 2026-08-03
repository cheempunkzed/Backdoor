# Employee Simulation System (v1.5.0)

## Overview
Every organization maintains a simulated workforce (`Employee.kt`) with individual traits, job positions, stress levels, loyalty metrics, and shift schedules.

## Employee Attributes
- `id`: Unique UUID
- `name`: Employee name
- `position`: Job position (`CEO`, `CTO`, `CISO`, `SYSADMIN`, `SOC_ANALYST`, `DEVELOPER`, `RESEARCHER`, `FINANCE_ANALYST`, `HR_MANAGER`, `LEGAL_ADVISOR`)
- `department`: Department (`EXECUTIVE`, `SECURITY`, `HR`, `IT`, `FINANCE`, `LEGAL`, `DEVELOPMENT`)
- `skillLevel`: Skill rating (1 to 10)
- `stress`: Stress level (0 to 100)
- `loyalty`: Loyalty rating (0 to 100)
- `salary`: Monthly compensation
- `isAtWork`: Shift status based on game clock (work hours: 09:00 - 17:00)

## Simulation Behaviors
- **Resignations**: Stressed or disloyal employees resign, emitting `EmployeeResigned` events and generating procedural forensic audit contracts.
- **Corporate Leaks**: Disloyal employees may leak internal credentials or blueprints, triggering `DataLeakDetected` events and darknet forum posts.
- **Workforce Recruitment**: HR departments automatically recruit replacement specialists when understaffed.
