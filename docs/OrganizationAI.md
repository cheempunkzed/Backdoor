# Autonomous Organization AI (v1.5.0)

## Overview
Organizations in Backdoor act as multi-departmental AI agents (`OrganizationAI`) operating autonomously to protect assets, manage employees, schedule server maintenance, and manage corporate budgets.

## Departmental Roles

### 1. Security Department (`SecurityDepartmentAI`)
- **Probing Detection**: Monitors network scans (`NetworkScanCompleted`) and vulnerability probes. Automatically elevates firewall sensitivity (1 to 10) and enables Intrusion Detection Systems (IDS).
- **Incident Response & Patching**: On server breach (`ServerCompromised`, `VulnerabilityExploited`), triggers max firewall lockdown, logs security reports, and schedules CVE software patches (`ServerPatched`).

### 2. HR Department
- **Employee Lifecycle**: Simulates employee stress, fatigue, salary satisfaction, and organizational loyalty.
- **Resignations & Leaks**: High stress (>85) or low loyalty (<15) triggers employee resignations (`EmployeeResigned`), disloyal data leaks (`DataLeakDetected`), and procedural investigation contracts.

### 3. IT Department (`ITDepartmentAI`)
- **Infrastructure Maintenance**: Handles server uptime, software upgrades, reboot cycles, and load management.

### 4. Finance Department (`FinanceDepartmentAI`)
- **Budget Allocation**: Dynamically adjusts corporate budget (`BudgetChanged`). High budget expansions trigger penetration test contract offers (`ProceduralMissionGenerator`).
