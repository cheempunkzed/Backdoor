# AbyssOS Changelog

## [0.2.0] - 2026-07-30
### Stage 3: Virtual File System (AbyssFS)
#### Added
- **AbyssFS Architecture**: Implemented full persistent Virtual File System engine (`InMemoryVirtualFileSystem`).
- **Standard Linux Hierarchy**: Created system root directories: `/`, `/bin`, `/boot`, `/dev`, `/etc`, `/home`, `/tmp`, `/usr`, `/var`, `/logs`, `/network`, `/desktop`, `/downloads`, `/documents`, `/darknet`, `/system`.
- **User Home Management**: Automatic initialization of `/home/<username>` with standard subdirectories (`Desktop`, `Downloads`, `Documents`, `Notes`, `Scripts`, `Trash`).
- **Rich File Types & Metadata**: Full support for extensions (`.txt`, `.log`, `.cfg`, `.sys`, `.key`, `.enc`, `.exe`, `.sh`, `.net`, `.tmp`) with size, timestamps, owner, permissions, hidden and system-protected flags.
- **Terminal Commands (Command Pattern)**:
  - `cd`: Navigation with relative/absolute paths & tilde expansion.
  - `mkdir`: Create directory hierarchy.
  - `touch`: Create files / update timestamps.
  - `rm`: Move to Trash or permanent removal (`-f`).
  - `mv`: Relocate / rename nodes.
  - `cp`: Copy files/directories recursively.
  - `cat`: Read file contents.
  - `pwd`: Print working directory.
  - `ls`: List directory contents with `-a` (hidden) and `-l` (long format).
  - `tree`: Display ASCII tree hierarchy.
  - `find`: Global file search by query.
  - `open`: Inspect or execute files/directories.
  - `rename`: Rename files and folders.
  - `version`: Display kernel & AbyssFS version details (`AbyssOS 0.2.0`).
- **FilesApp File Manager**:
  - Breadcrumb path navigation & back buttons.
  - Dual-pane layout: Directory tree on left, Inspector & Context Actions on right.
  - Real-time search, sorting (Name, Size, Date, Type).
  - Modal dialogs for File creation, Editing, Folder creation, Renaming, Copying, Moving, File Info.
  - Interactive Trash manager with Restore and Permanent Delete options.
- **System Protection**: Protection against deleting `/system`, `/bin`, `/boot`, `/dev`, `/etc`, or `/`.
- **Audit Logging**: Automatic action logging into `/logs/fs_audit.log`.
- **Persistence Layer**: JSON serialization/deserialization tied to Room database save slots (`vfsDataJson`).

---

## [0.1.0] - 2026-07-30
### Initial Release
#### Added
- **Base AbyssOS Architecture**: Initial OS state manager, desktop UI, taskbar, notifications.
- **Authentication System**: User registration and login flows.
- **Core Applications**: Terminal, Files (In-memory initial version), Browser, Network, DarkNet, Settings, Logs, System Monitor.
- **Save Management**: Room database integration (`AppDatabase`) for user profile and save slot storage.
- **CI/CD Integration**: GitHub Actions workflow for automatic Debug APK creation on commit/push.
