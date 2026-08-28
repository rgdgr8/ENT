# Documentation Hub

This folder is the central entry point for project documentation.

## Build and Usage
- Main app guide: `metadataservice/README.md`
- Legacy setup notes: `doc/legacy_setup.txt`
- Build scripts: `scripts/` (build_all_variants.sh, build_local.sh, test_local.sh, switch_app.sh, CI scripts)

## Architecture and Migration
- Client implementation notes: `doc/ClientImplementation.md`
- Installing over the legacy `technology.cariad.entertainment.metadataservice` app: `doc/INSTALL_OVER_LEGACY.md`

## Module Documentation
- Main service docs: `metadataservice/doc/`
- Main service user manual: `metadataservice/user_manual/`
- Client interface docs: `metadataservice_clientinterface/README.md`
- Conradlib legacy docs: `metadataservice_conradlib/README.md`
- Probe app docs: `mds_probe/README.md`

## Release and Quality
- Release notes: `release_notes/` (includes releasenote.adoc and release_notes.txt)
- Review exports: `reviews/` (git-ignored)
- Static analysis: `static_code_analysis/` (includes ignored-issues.json and Coverity reports)

## Migration Sign-off Checklist
- Namespace migration complete to `technology.cariad.entertainment.*` in tracked source/config/docs.
- Legacy namespace verification clean:
	- no legacy package-prefix usages in tracked non-generated files
	- no legacy metadataservice package-name usages in tracked non-generated files
- AIDL path/package alignment verified for client interface sources.
- Build verification complete for all supported dev profiles:
	- `emu`, `icc`, `no-gn`
- No blocking IDE diagnostics in:
	- `metadataservice/`
	- `metadataservice_clientinterface/`
- Known residual warnings are currently non-blocking (deprecations/toolchain warnings).

## Assets
- Icons: `doc/icons/`

## Build Artifacts
- APK output: `output/`

## CI / Integration
- Conan dependency config: `integration/conanfile.py`
- Stream config: `integration/stream.json`

