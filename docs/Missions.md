# MISSIONS & CONTRACTS ENGINE - ABYSSOS 1.2.0

## CODENAME: ABYSS CONTRACTS ENGINE

### Executive Summary
The Abyss Contracts Engine in AbyssOS 1.2.0 replaces the legacy contract system with a validated, objective-driven gameplay framework. Contracts no longer rely on simple player button clicks for instant completion; instead, every contract consists of real, verifiable gameplay objectives tracked via `SystemEventBus`.

---

## 1. Core Architecture

### Objective Model
An `Objective` is a discrete, trackable requirement embedded within a `Contract`:
```kotlin
data class Objective(
    val objectiveId: String,
    val objectiveType: ObjectiveType,
    val description: String,
    val currentProgress: Int = 0,
    val requiredProgress: Int = 1,
    val targetParam: String? = null,
    val secondaryParam: String? = null,
    val isCompleted: Boolean = false
)
```

### Supported Objective Types (`ObjectiveType`)
- **NETWORK**:
  - `SCAN_TARGET`: Verifies port scans and IP subnet sweeps against target hosts.
  - `DISCOVER_SERVICE`: Validates service discovery (e.g., SSH, HTTP, FTP).
- **SECURITY**:
  - `ANALYZE_SECURITY`: Tracks security vulnerability assessment tasks.
  - `EXPLOIT_VULNERABILITY`: Validates successful exploitation.
- **FILESYSTEM**:
  - `DOWNLOAD_FILE`: Confirms retrieval of target files to `/home/operator/downloads/`.
  - `DELETE_FILE`: Verifies permanent removal or log sanitization.
  - `CREATE_REPORT`: Tracks generation and saving of security reports.
- **TERMINAL**:
  - `EXECUTE_COMMAND`: Validates execution of specific terminal commands (e.g., `whois`, `ls`, `ping`).
- **DARKNET**:
  - `VISIT_HIDDEN_SERVICE`: Tracks encrypted onion service connections (e.g., `abyss-forum.onion`).
  - `CREATE_FORUM_POST`: Validates creating threads or replies in underground cyber forums.
  - `CONTACT_NPC`: Verifies communication with darknet identities.
- **ECONOMY**:
  - `PAYMENT_RECEIVED`: Validates incoming credit transfers.
  - `PAYMENT_SENT`: Validates outgoing transactions.
  - `PURCHASE_ITEM`: Confirms item procurement from Marketplace or Dark Market.
  - `SELL_ITEM`: Tracks selling hardware/software assets.

---

## 2. Event-Driven Validation Pipeline

The `ContractManager` registers an observer on `SystemEventBus`:
1. Subsystems (Terminal, Network Scanner, Security Framework, VFS, DarkNet, Wallet, Marketplace) emit high-level `SystemEvent` instances.
2. `ContractManager.onSystemEvent(event)` evaluates all currently `ACCEPTED` contracts.
3. Matching objectives increment `currentProgress`. Once `currentProgress >= requiredProgress`, `isCompleted` evaluates to `true`.
4. When all objectives of a contract are satisfied, `contract.isCompleted()` becomes `true` and a notification is triggered.

---

## 3. Completion Validation & Security Guard (`CompletionValidator`)

Before granting contract rewards:
```kotlin
val result = CompletionValidator.validate(contract)
```
Checks:
- Contract status must be `ACCEPTED`.
- Contract expiration timestamp must not have elapsed.
- All objectives in `contract.objectives` must be satisfied (`isDone() == true`).

If validation passes:
1. Status changes to `COMPLETED`.
2. `WalletManager` deposits rewards (CREDITS or ABYSS_COIN).
3. User receives visual and audio feedback.

---

## 4. UI Representation (`ContractsApp.kt`)
- **Checklist Display**: Each contract presents a live objective checklist (`✓` for completed, `✗` for pending).
- **Progress Header**: Shows total progress (e.g., `2/3 Objectives Completed (66%)`).
- **Protected Actions**: The reward claim button is disabled with progress indicators until all objectives are fulfilled. Clicking an incomplete button displays exact missing objectives.

---

## 5. Persistence & Save System
Contracts and objective progress are serialized into JSON via `ShadowEconomyEngine.serializeToJson()` and restored on game load, ensuring objective progress persists across game restarts.
