# MetadataService - Cariad MDS

Audio metadata recognition service using Gracenote SDK for automotive applications.

**Maintainer:** Christian Winter (T2-CH), Volkswagen Group / Cariad SE  
**Version:** 1.90-release  
**License:** See LICENSE file

---

## Overview

MetadataService provides audio fingerprinting and metadata lookup capabilities for the
Cariad/VW IVI platform. This standalone fork removes all dependencies on internal
e.solutions infrastructure, enabling independent development and testing.

### Features
- Gracenote audio fingerprinting (song recognition)
- Album/Artist metadata lookup
- Cover art retrieval
- Radio station identification via legacy ConnectedRadio backend
- AIDL client interface for cross-process IPC (radio app, MDSProbe)
- SQLite cache with configurable expiration
- Status monitoring via Demo Activity

---

## Requirements

### Development Environment
| Tool | Version | Notes |
|------|---------|-------|
| Android Studio | 2023.1.1+ (Hedgehog) | or newer |
| Android SDK | API 30+ | compileSdk 34 |
| Android NDK | 26.1.10909125 | side-by-side installation |
| JDK | 17+ | bundled with Android Studio |
| CMake | 3.22.1+ | via SDK Manager |

### Target Platform
- Android `arm64-v8a` (64-bit ARM)
- VW MQB Evo / Cariad IVI units (Android Automotive)

---

## Quick Start

### 1. Clone Repository
```bash
git clone <repository-url>
cd <repo-name>/metadataservice
```

### 2. Configure NDK
Ensure Android NDK 26.1.10909125 is installed via Android Studio SDK Manager.

### 3. Build
```bash
# Build dev debug variant
./gradlew assembleDevRowDebug

# Output location:
# app/build/outputs/apk/devRow/debug/MetadataService-LocalBuild-dev-row-debug.apk
```

### 3b. Build All Demo Variants (Legacy Profiles)
From workspace root, run:
```bash
./scripts/build_all_variants.sh
```

This script builds three `devRowDebug` APKs across the available runtime profiles:
- Legacy emu (`legacy-emu`) using `metadataservice_conradlib`
- Legacy icc (`legacy-icc`) using `metadataservice_conradlib`
- Legacy no-GN (`legacy-no-gn`) using `metadataservice_conradlib`

Output location:
```text
output/MetadataService-Legacy-EmuGeneric-tech-cariad-dev-row-debug.apk
output/MetadataService-Legacy-ICC-tech-cariad-dev-row-debug.apk
output/MetadataService-Legacy-NoGN-tech-cariad-dev-row-debug.apk
```

In the demo app (dev build), open Settings and check "ConRad Lib Version":
- `OI_<timestamp>` => Legacy build

### 4. Install on Device

#### Using Install Scripts (Recommended)
The installer scripts automatically:
- Detect which variant is currently installed (legacy or cariad package)
- Find the latest built APK without relying on hardcoded filenames
- Handle both package names (`technology.cariad.entertainment.metadataservice` and `technology.cariad.entertainment.metadataservice`)

```bash
# Install devRowDebug variant (LocalBuild)
./scripts/install_local_build.sh

# Install prodRowDebug variant (with PROD config)
./scripts/build_and_install_prod.sh

# Check current installation status
../scripts/switch_app.sh status

# Switch between original and local build
../scripts/switch_app.sh original   # Restore system app from backup
../scripts/switch_app.sh local      # Build and install local dev build
```

#### Manual Installation via ADB
```bash
# Build first
./gradlew assembleDevRowDebug

# Install the resulting APK
adb install -r app/build/outputs/apk/devRow/debug/*.apk
```

---

## Build Variants

| Variant | Description |
|---------|-------------|
| `devRowDebug` | Development build with debug logging |
| `devRowRelease` | Development build without debug |
| `prodRowDebug` | Production-like config with debug (MagicAdapter token auth) |
| `prodRowRelease` | Full production build |

**Recommended for testing:** `devRowDebug`

---

## Project Structure

```
<repo-root>/
├── gnsdk-auto-2.6.19.14264-20250729/   # Gracenote SDK (standalone)
│   ├── include/                         # C/C++ headers
│   ├── lib/                             # Native libraries
│   └── wrappers/                        # Java/JNI wrappers
├── metadataservice/                     # Main Android app
│   ├── app/                             # Application module
│   │   └── src/main/cpp/                # Native C++ code
│   ├── scripts/                         # Install/uninstall scripts
│   └── deploy/                          # Version configuration
├── metadataservice_clientinterface/     # AIDL client library (see its README)
│   ├── lib/                             # Android library module
│   └── rpc2aidl.main.kts               # Code generator
├── mds_probe/                           # Test app for cross-process IPC testing
│   └── app/                             # Standalone APK (see its README)
├── doc/                                 # Central documentation index (workspace)
└── metadataservice_conradlib/           # Old ConRadLib v2 (legacy, read-only)
```

---

## Gracenote SDK

This build includes **Gracenote SDK for Auto** version **3.18.19.14264**.

### SDK Location
The SDK is located at `gnsdk-auto-2.6.19.14264-20250729/`.

### Documentation
Use the central documentation index in the workspace root:
- `../doc/README.md`

### Native Libraries (arm64-v8a)
- `libgnsdk_manager.so` - Core SDK manager
- `libgnsdk_musicid.so` - Music identification
- `libgnsdk_musicidstream.so` - Streaming audio recognition
- `libgnsdk_dsp.so` - Digital signal processing
- `libgnsdk_storage_sqlite.so` - Local storage
- ... and others (15 total)

### API Keys
Development keys are configured in:
- `GracenoteConfigProviderImpl.kt` (dev builds)
- `GracenoteConfigProvider.kt` (prod builds)

---

## Scripts

| Script | Description |
|--------|-------------|
| `scripts/install_local_build.sh` | Install devRowDebug; detects current variant; auto-finds APK |
| `scripts/build_and_install_prod.sh` | Build & install prodRowDebug with MagicAdapter token config |
| `scripts/uninstall_local_build.sh` | Remove LocalBuild, restore system app |
| `../scripts/switch_app.sh` | Check status or switch between original and local build |
| `../scripts/build_all_variants.sh` | Build all demo variants (legacy-emu/icc/no-gn) |

**New in v1.90:** All installer scripts now detect both package names (`technology.cariad.entertainment.metadataservice` and `technology.cariad.entertainment.metadataservice`) and dynamically resolve APK filenames, making them robust across different build profiles.

---

## Differences from e.solutions Build

This fork removes all dependencies on internal infrastructure:

| Feature | e.solutions | Cariad Fork |
|---------|-------------|-------------|
| Repositories | eso.local Artifactory | None (standalone) |
| Gracenote SDK | Git LFS / Conan | Direct inclusion |
| Tracing | eso.fw.tracing | Android Log |
| ASI Admin | Required | Optional (graceful fallback) |
| Extended Storage | Required | Optional (graceful fallback) |

---

## Troubleshooting

### Build fails with NDK error
Ensure NDK version 26.1.10909125 is installed:
```bash
# List installed NDKs
ls ~/Library/Android/sdk/ndk/

# Install via SDK Manager if missing
```

### Native library not found
Verify the SDK libraries are present:
```bash
ls -la ../gnsdk-auto-2.6.19.14264-20250729/lib/android/arm64-v8a/
```

### App crashes on device
Check logcat for Gracenote initialization:
```bash
adb logcat | grep -E "GnService|Gracenote|MDS"
```

---

## Contributing

1. Create feature branch from `master`
2. Make changes and test on device
3. Update CHANGELOG.md
4. Submit pull request

---

## License

See LICENSE file for details.

**Gracenote SDK:** Subject to Gracenote licensing terms.  
**Original Code:** Copyright (c) e.solutions GmbH  
**Modifications:** Copyright (c) 2026 Cariad SE

---

## Version History

See [CHANGELOG.md](CHANGELOG.md) for full version history.

