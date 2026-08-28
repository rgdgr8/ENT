# Cariad MetadataService Workspace

Monorepo-like workspace for MetadataService, legacy conrad library, client interface, probe app, build scripts, and documentation.

## Current App Version Status
- Active build version for `technology.cariad.entertainment.metadataservice`: `1.90-release`
- Source of truth: `metadataservice/deploy/versions.json`
- Note: `doc/CHANGELOG.md` already contains a `1.8.0-beta` section (migration work), but the configured build version is still `1.7.2-release`.
- **Package rename:** the app was refactored from `technology.cariad.entertainment.metadataservice` to `technology.cariad.entertainment.metadataservice`. Because the `applicationId` changed, the refactored build is a new package and does not replace a legacy install. To deploy it on a device that already has `technology.cariad.entertainment.metadataservice`, follow `doc/INSTALL_OVER_LEGACY.md`.

## Components Overview

| Component | Path | Purpose | Main Doc |
|---|---|---|---|
| Main Android app | `metadataservice/` | Core metadata service app (Kotlin + native Gracenote integration) | `metadataservice/README.md` |
| Legacy ConRad library (v2) | `metadataservice_conradlib/` | Legacy ConnectedRadio implementation and related docs | `metadataservice_conradlib/README.md` |
| Client interface library | `metadataservice_clientinterface/` | AIDL/RPC-facing client library and generated interfaces | `metadataservice_clientinterface/README.md` |
| Probe test app | `mds_probe/` | Standalone app to test IPC/service behavior | `mds_probe/README.md` |
| Central documentation | `doc/` | Migration notes, API notes, changelog, and documentation index | `doc/README.md` |
| Build and helper scripts | `scripts/` | Variant builds, local build flows, switching app variants, local test helpers | `scripts/` |
| Integration configs | `integration/` | Conan and stream/variant integration metadata | `integration/` |
| Release notes | `release_notes/` | Release artifacts and release documentation | `release_notes/` |
| Build outputs | `output/` | Generated APKs and temporary build logs | `output/` |
| Static analysis data | `static_code_analysis/` | Suppressions and analysis-related artifacts | `static_code_analysis/` |
| External assets/deps | `external/`, `gnsdk-auto-2.6.19.14264-20250729/`, `libs_from_Cariad/` | Third-party and prebuilt dependencies used by builds | `NOTICE.md` |

## Token Setup (required before building)

Build scripts read tokens from `testtoken.env` at the workspace root.  
This file is **gitignored** — create it locally once:

```
#conradlibV2
<paste legacy ConnectedRadio JWT here>
```

The build scripts will automatically write the tokens into:
- `token/testtoken.txt` (gitignored)

Both token files contain only a `Add token here` placeholder in the repo.

---

## Typical Workflows

### Build all demo variants
Run from workspace root:

```bash
./scripts/build_all_variants.sh
```

Generated APKs are placed in `output/`.

### Build main app only
Run from workspace root:

```bash
./scripts/build_local.sh
```

### Where to start reading
1. `metadataservice/README.md`
2. `doc/README.md`
3. Module readmes for the component you are changing

