# greymmc Executive Desktop Workstation (.EXE / Windows Installer)

This directory contains the independent **Compose Multiplatform (Desktop JVM)** version of **greymmc**, engineered to package into a standalone Windows `.exe` and `.msi` installer via `jpackage`.

## Features
- **Independent Windows Desktop Application**: Operates directly on Windows 10/11 without Android subsystems, emulators, or mobile dependencies.
- **Owl Head Executive Identity**: Uses the high-resolution Owl Head icon for the Windows desktop taskbar and executable icon (`src/desktopMain/resources/icon.ico`).
- **Live A4 Landscape PDF Engine**: Powered by pure desktop JVM Apache PDFBox to render and export 842 x 595 pt A4 Landscape dossier documents.
- **Dual-Pane Workstation**: Left form editor (Personal & Contact Information with Address, Social Media with TikTok, Other Information with `Occupation/ Role`), right live preview canvas.

---

## 1. Automated Build via GitHub Actions CI/CD (Recommended)
This repository includes a ready-to-run GitHub Actions workflow:
- Path: `.github/workflows/build-desktop-exe.yml`
- **Runner**: `windows-latest`
- **Tooling**: WiX Toolset, OpenJDK 17, and Gradle `jpackage` tasks
- **Output Artifacts**:
  - `greymmc.exe` (Windows Standalone Executable)
  - `greymmc-1.0.0.msi` (Windows Desktop Installer)

### To trigger on GitHub:
1. Push this repository to GitHub or click **Actions > Build Windows Desktop EXE**.
2. Download the packaged installer `.exe` from the GitHub Actions **Artifacts** section.

---

## 2. Building Locally on a Windows Machine
If you have a Windows PC with JDK 17 installed:
```powershell
# 1. Navigate to the desktop module
cd desktop-app

# 2. Run the Gradle package task (requires WiX Toolset on PATH)
./gradlew packageExe packageMsi

# 3. Locate the built installer:
# desktop-app/build/compose/binaries/main/exe/greymmc.exe
# desktop-app/build/compose/binaries/main/msi/greymmc-1.0.0.msi
```

To run and test the desktop app without packaging:
```powershell
./gradlew run
```
