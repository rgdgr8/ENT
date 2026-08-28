# Changelog — MetadataService App (Cariad Fork)

All notable changes since the initial clone of the vendor MetadataService repository (February 2026).

**Maintainer:** Christian Winter (T2-CH), Volkswagen Group / Cariad SE
**Latest Version:** 1.90-release

---

## [1.90-release] — July 2026

### Namespace Migration Sign-off (Production Safety)
- Scope completed: refactor consolidated to `technology.cariad.entertainment.*` across app, client interface, scripts, and deployment metadata.
- Package/namespace consistency checks passed on tracked non-generated files:
  - no legacy package-prefix leftovers
  - no legacy metadataservice package-name leftovers
- AIDL/package-path consistency validated (AIDL sources aligned with declared package paths).
- Profile build matrix passed:
  - `emu`: `:app:assembleDevRowDebug -Pmds.profile=emu`
  - `icc`: `:app:assembleDevRowDebug -Pmds.profile=icc`
  - `no-gn`: `:app:assembleDevRowDebug -Pmds.profile=no-gn`
- IDE diagnostics status: no blocking errors in `metadataservice` and `metadataservice_clientinterface`.
- Remaining warnings are non-blocking legacy/deprecation/toolchain warnings; no migration-specific build failures observed.

### Version and Branding Updates
- Updated app version to 1.90-release across all builds
- Enhanced APK naming to include package identifier: `tech-cariad` for `technology.cariad.entertainment.metadataservice` package
  - New APK naming: `MetadataService-<profile>-tech-cariad-dev-row-*.apk`
  - Makes it clear the build uses the new Cariad package name (not legacy `technology.cariad.entertainment.metadataservice`)
- Added Gracenote SDK availability indicator in demo app Settings UI ("Available (Licensed)" or "Not Available")
- Verified all build variants correctly select the appropriate legacy ConRadLib mode with correct client version display

### Unreleased — July 2026

### Package Rename Refactoring (`technology.cariad.entertainment.metadataservice` → `technology.cariad.entertainment.metadataservice`)
- Renamed `applicationId`, Gradle `namespace`, and all Kotlin/AIDL source packages from `technology.cariad.entertainment.metadataservice` to `technology.cariad.entertainment.metadataservice`
- Image provider authority renamed to `technology.cariad.entertainment.metadataservice.MetadataService.ImageProvider`
- Client contract preserved: the app still declares the legacy `technology.cariad.entertainment.metadataservice.USE_SERVICE` permission **and** the new `technology.cariad.entertainment.metadataservice.USE_SERVICE` permission (both `signature|privileged`), and still exports the unchanged bind action `technology.cariad.entertainment.metadataserviceclient.api.IMetadataServiceAidl` — existing clients (e.g. `de.eso.radio`) keep working without changes
- Because the `applicationId` changed, the refactored build is a **new package** and does not update/replace the legacy install; the legacy `technology.cariad.entertainment.metadataservice` app must be removed to avoid duplicate service/permission conflicts
- Added `doc/INSTALL_OVER_LEGACY.md` describing how to deploy the refactored app on a device that already has `technology.cariad.entertainment.metadataservice` installed (priv-app replacement and `adb install` flows, package-cache clearing, verification)

### Three-Profile Build System
- Added Gradle property `mds.profile=legacy-emu|legacy-icc|legacy-no-gn` selecting a complete runtime profile at build time
- `legacy-emu` — vanilla Android Automotive emulator: legacy ConRadLib, approval endpoint, **mock** service list + disable reasons, baked `token/testtoken.txt`, DEV mode ON. Runs out of the box.
- `legacy-icc` — real testbench with Magic: legacy ConRadLib, real token (Magic/AccountManager), **real** service list + disable reasons, LIVE mode (DEV switch still available)
- `legacy-no-gn` — legacy ConRadLib with Gracenote disabled (`GN_LICENSE_VARIANT=NONE`) for quick local runs without GN provisioning
- New `BuildConfig` fields: `MDS_PROFILE`, `DEFAULT_DEV_MODE`, `DEFAULT_TOKEN_OVERRIDE_ENABLED`, `DEFAULT_USE_REAL_SERVICE_LIST`, `DEFAULT_USE_REAL_DISABLE_REASONS`
- `DemoConfigProvider` (dev) seeds runtime defaults from the profile's `BuildConfig` values and persists tester overrides across restarts
- `scripts/build_all_variants.sh` now builds all three profiles; APK names: `MetadataService-Legacy-EmuGeneric-*`, `MetadataService-Legacy-ICC-*`, `MetadataService-Legacy-NoGN-*`
- Settings screen variant label now distinguishes `Legacy (emu-generic)` / `Legacy (icc)` / `Legacy (no-gn)`

### Settings Tab Refresh Fix
- Fixed intermittent stale Settings screen after switching tabs: `ViewPager.offscreenPageLimit` now keeps all tab fragments alive and a page-selected listener calls `SettingsFragment.refreshSettings()` to re-query the service

### Documentation
- Consolidated documentation into `doc/` and refreshed build-profile guidance for legacy flows

### Project Structure Cleanup
- Moved all build/test scripts from root into `scripts/`
- Moved documentation (`ClientImplementation.md`) into `doc/`
- Created central documentation hub at `doc/README.md`
- Moved `release_notes.txt` → `release_notes/`, `conanfile.py` → `integration/`, `ignored-issues.json` → `static_code_analysis/`, `icons/` → `doc/icons/`
- Moved legacy root readme to `doc/legacy_setup.txt`
- Removed unused `db_export/` folder
- Added `reviews/` to `.gitignore`
- Removed duplicate `gracenotesdk/` folder (build uses `gnsdk-auto-2.6.19.14264-20250729/` only)
- Removed Git LFS tracking entirely (`.gitattributes` deleted) — all binaries stored directly in Git
- Integrated `metadataservice_conradlib/` into main repo (removed nested `.git`, removed from `.gitignore`)

### Build Variant Consolidation
- Standardized ConRadLib usage on legacy implementation for all supported profiles
- `settings.gradle` resolves `:conradlib` to `metadataservice_conradlib/ConnectedRadioClient/app`
- `scripts/build_all_variants.sh` builds supported legacy profiles sequentially and copies APKs to `output/`
- APK names include variant label such as `MetadataService-Legacy-EmuGeneric-*`
- `BuildConfig.CONRADLIB_VARIANT` identifies the active variant at runtime
- Default token source remains `token/testtoken.txt` for local builds
- Default backend URL is configured in `app/build.gradle`

### Demo App Settings UI (dev builds)
- Added Runtime Mode switch (LIVE/DEV) with explicit `textOn="DEV"` / `textOff="LIVE"` labels
- Added "Save Runtime Settings" button with pending-state flow
- Added custom backend URL field and runtime summary text
- Settings screen shows active ConRadLib variant (`OI_<timestamp>`)

---

## [1.8.0-beta] — 2026-05 — Client Interface and Probe App

### Client Interface Module
- New module `metadataservice_clientinterface/` — AIDL-based client library
- `rpc2aidl.main.kts` code generator: parses `IMetadataService.kt` → AIDL interfaces + adapters
- Provides `IMetadataServiceClientAdapter` for RxJava ↔ AIDL binding
- All Parcelable data classes: `Station`, `StationBearer`, `StationMetadata`, `StationLogo`, etc.

### MDSProbe Test App
- New module `mds_probe/` — standalone APK for cross-process IPC testing against MetadataService

---

## [1.7.2-release] — 2026-02-06 — Gracenote SDK Standalone

### SDK Integration
- Added Gracenote SDK `gnsdk-auto-2.6.19.14264-20250729` (Version 3.18.19.14264) directly to repository
- Configured native libraries for Android `arm64-v8a`
- Updated `CMakeLists.txt` to use new SDK path format (`android_arm64-v8a`)
- Replaced IRC tracing in native code (`GnServiceIrcTracing.cxx/hxx`) with Android Log

### Build System
- Removed `maven-publish` plugin and publishing block (no more eso.local references)
- Removed TestApp module (not needed for local builds)

### New Tools
- Added `switch_app.sh` for easy app exchange on device
- Added Status Page in Demo Activity for service diagnostics

### Output
- APK: `MetadataService-LocalBuild-dev-row-debug.apk` (~44MB with 15 native `.so` libraries)

---

## [1.7.1-beta] — 2026-02-06 — Branding

- New "Cariad MDS" branding and launcher icon
- Version bump for beta testing

---

## [1.7.0-beta] — 2026-02-04 — Local Build Enablement

### e.solutions Dependency Removal (MetadataService)
- Removed all `eso.local` and `eso.fw` Maven repository references
- Replaced vendor `eso.fw.tracing` with `android.util.Log`
- Replaced vendor `media-common` Gradle plugin (removed Asciidoctor, OSS, SBOM tasks)
- Made `<uses-library>` tags optional (`android:required="false"`) for emulator compatibility
- Made ASI Admin and ExtStorageManager initialization non-fatal (graceful fallback)

### Stub Implementations Created
| Area | Stubs |
|------|-------|
| gmdlib | `GmdClient`, `GmdErrorType`, `AccountManagerConfig`, `EndpointConfig`, `IGracenoteMusicDataCallback` |
| ASI/Storage | `StorageClientAdapter`, `IStorageServiceListener`, `OpResult`, `OptionalValue`, `Consts` |
| Tracing | `TraceClient`, `ITraceCallback`, `TraceLevels` |
| Framework | `AsiAdmin`, `IServiceCallback`, `OneDimShortArrayWrapper` |
| Storage Manager | `ExtStorageManager`, `ExtStorageManagerFactory` |
| Navigation | `IPositionInformation`, `IPositionInformationCallback`, `GeneralPositionInformationType`, `RoadInformationType` |

### Logger Rewrite
- Complete rewrite of `Logger.kt` to use `android.util.Log` (v→TRACE, d→DEBUG, i→INFO, w→WARN, e→ERROR, wtf→FATAL)
- New `PrivacyFlag.kt` enum replacing vendor `eso.fw` variant

### e.solutions Dependency Removal (ConnectedRadioClient / Legacy ConRadLib)
- Replaced `esofw` Version Catalog with `google()` + `mavenCentral()`
- Removed `api(platform(esofw.platform))`, `esofw.util.android.newstapi`, `esofw.util.android.support`
- Replaced Logger with `android.util.Log`, added local `PrivacyFlag` + `TraceLevels` enums
- IRC client replaced with no-op implementation (logs locally only)
- Removed `<uses-library>` for vendor platform library
- Removed `maven-publish` plugin and Artifactory publishing
- Changed Gradle wrapper URL from `artifactory.eso.local` to `services.gradle.org`

### Build Scripts
- `build_local.sh` — local build with JAVA_HOME/SDK auto-detection
- `test_local.sh` — unit test runner
- Install/uninstall scripts for device deployment

---

## [1.6.40-CLU53TVR2] — 2025 — Baseline

- Last release under vendor development
- Production version installed on Cariad IVI systems (VW MQB Evo)
- Initial clone imported as `7fd69097 initial commit from eso master Feb 2026`

---

## File Origin Reference

This changelog consolidates information previously scattered across:
- `metadataservice/CHANGELOG.md` (version history)
- `metadataservice/CHANGES_LOCAL_BUILD.md` (detailed local build changes)
- `metadataservice_conradlib/changed.md` (legacy conradlib changes)

