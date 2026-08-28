#!/bin/bash
# BUILD ALL PROFILES – MetadataService
#
# Run from ./scripts/:  bash build_all_variants.sh [0-3]
#
# Arguments:
#   1st – profile selection (default: interactive prompt)
#           0 → all profiles (emu + icc + no-gn)
#           1 → emu only
#           2 → icc only
#           3 → no-gn only
#
# Examples:
#   bash build_all_variants.sh 0        # all 3 APKs  (emu + icc + no-gn)
#   bash build_all_variants.sh 1        # 1 APK       (emu)
#   bash build_all_variants.sh          # interactive profile prompt
#
# Profiles:
#   emu    – vanilla Android Automotive emulator. Conradlib, approval endpoint,
#            mock service list + disable reasons, baked-in test token, DEV mode ON.
#   icc    – real testbench with Magic. Conradlib, real token,
#            real service list + disable reasons, LIVE mode.
#   no-gn  – VW no-license fallback. Same runtime behavior as icc, GN license OFF.
#
# Output (relative to workspace root):
#   output/MetadataService-{Profile}-tech-cariad-dev-row-debug.apk
#     e.g. output/MetadataService-EmuGeneric-tech-cariad-dev-row-debug.apk
#
# The Settings screen in the dev build shows the ConRad client library version and
# Gracenote SDK availability status.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WORKSPACE_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
MDS_DIR="$WORKSPACE_ROOT/metadataservice"
OUTPUT_DIR="$WORKSPACE_ROOT/output"
APK_SRC="$MDS_DIR/app/build/outputs/apk/devRow/debug"

# ─────────────────────────────────────────────────────────────────────────────
# Helpers
# ─────────────────────────────────────────────────────────────────────────────

banner() { echo; echo "============================================"; echo "$1"; echo "============================================"; }

prompt_build_selection() {
  echo ""
  echo "Select build option:"
  echo "  0) Build all variants"
  echo "  1) Build emu generic variant (emu)"
  echo "  2) Build ICC variant (icc)"
  echo "  3) Build no-GN variant (no-gn)"
  read -r -p "Enter option [0-3]: " BUILD_SELECTION
}

resolve_build_targets() {
  BUILD_TARGETS=()
  case "$BUILD_SELECTION" in
    0)
      BUILD_TARGETS=("emu" "icc" "no-gn")
      ;;
    1)
      BUILD_TARGETS=("emu")
      ;;
    2)
      BUILD_TARGETS=("icc")
      ;;
    3)
      BUILD_TARGETS=("no-gn")
      ;;
    *)
      echo "ERROR: Invalid option '$BUILD_SELECTION'. Choose 0, 1, 2, or 3." >&2
      exit 1
      ;;
  esac
}

contains_target() {
  local needle="$1"
  for target in "${BUILD_TARGETS[@]}"; do
    if [ "$target" = "$needle" ]; then
      return 0
    fi
  done
  return 1
}

# Use no-daemon by default to avoid intermittent daemon-stop failures in long
# multi-profile runs. Extra args can be injected via MDS_GRADLE_ARGS.
GRADLE_ARGS=${MDS_GRADLE_ARGS:-"--no-daemon --stacktrace"}
MAX_RETRIES=${MDS_MAX_RETRIES:-1}

BUILD_SELECTION="${1:-${MDS_BUILD_SELECTION:-}}"
if [ -z "$BUILD_SELECTION" ]; then
  prompt_build_selection
fi
resolve_build_targets

BUILD_IDENTITY="new"
APK_IDENTITY_SUFFIX="tech-cariad"

# ─────────────────────────────────────────────────────────────────────────────
# Java / Android SDK detection  (same logic as build_local.sh)
# ─────────────────────────────────────────────────────────────────────────────

if [ -z "${JAVA_HOME:-}" ]; then
  for jbr in \
    "/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
    "/Applications/Android Studio Preview.app/Contents/jbr/Contents/Home"
  do
    if [ -d "$jbr" ]; then
      export JAVA_HOME="$jbr"
      break
    fi
  done
fi

if [ -n "${JAVA_HOME:-}" ]; then
  echo "Using JAVA_HOME=$JAVA_HOME"
  "$JAVA_HOME/bin/java" -version || true
else
  echo "WARNING: JAVA_HOME is not set. Android Studio's embedded JBR (JDK 17) is recommended."
fi

SDK_DEFAULT="$HOME/Library/Android/sdk"
if [ -n "${ANDROID_HOME:-}" ] && [ -d "${ANDROID_HOME:-}" ]; then
  SDK_PATH="$ANDROID_HOME"
elif [ -n "${ANDROID_SDK_ROOT:-}" ] && [ -d "${ANDROID_SDK_ROOT:-}" ]; then
  SDK_PATH="$ANDROID_SDK_ROOT"
elif [ -d "$SDK_DEFAULT" ]; then
  SDK_PATH="$SDK_DEFAULT"
else
  SDK_PATH=""
fi

LP="$MDS_DIR/local.properties"
if [ -n "$SDK_PATH" ]; then
  SDK_PATH="${SDK_PATH//\\//}"
  echo "Using Android SDK at: $SDK_PATH"
  mkdir -p "$MDS_DIR"
  SDK_ESCAPED="${SDK_PATH// /\\ }"
  printf "sdk.dir=%s\n" "$SDK_ESCAPED" >"$LP"
else
  echo "WARNING: Android SDK not found. Set ANDROID_HOME or install via Android Studio."
fi

# Default to a licensed build for normal variants; no-gn overrides this.
export GN_LICENSE_VARIANT=VARIANT_OEM_AU
export USE_LOCAL_LIBS=true

DEFAULT_NDK_VERSION="26.1.10909125"
if [ -n "$SDK_PATH" ]; then
  DEFAULT_NDK_SOURCE_PROPS="$SDK_PATH/ndk/$DEFAULT_NDK_VERSION/source.properties"
  if [ -f "$DEFAULT_NDK_SOURCE_PROPS" ]; then
    export NDK_VERSION="$DEFAULT_NDK_VERSION"
  else
    NDK_FALLBACK_VERSION=""
    if [ -d "$SDK_PATH/ndk" ]; then
      NDK_FALLBACK_VERSION="$(find "$SDK_PATH/ndk" -mindepth 2 -maxdepth 2 -name source.properties -printf '%h\n' 2>/dev/null | sed 's|.*/||' | sort -V | tail -n 1)"
    fi

    if [ -n "$NDK_FALLBACK_VERSION" ]; then
      export NDK_VERSION="$NDK_FALLBACK_VERSION"
      echo "WARNING: NDK $DEFAULT_NDK_VERSION is missing source.properties; using installed NDK $NDK_VERSION instead."
    else
      echo "WARNING: No valid NDK with source.properties found under $SDK_PATH/ndk"
      echo "         Install NDK $DEFAULT_NDK_VERSION using sdkmanager when network is available."
    fi
  fi
fi

mkdir -p "$OUTPUT_DIR"

# Ensure the standalone Gracenote SDK payload is present before starting Gradle.
GNSDK_ROOT="$WORKSPACE_ROOT/gnsdk-auto-2.6.19.14264-20250729"
GNSDK_REQUIRED_WRAPPER="$GNSDK_ROOT/wrappers/gnsdk_cplusplus/src_wrapper/gnsdk_base.cpp"
GNSDK_REQUIRED_LIB_GLOB="$GNSDK_ROOT/lib/android_arm64-v8a/libgnsdk_manager"*
SKIP_GNSDK_REQUIREMENTS="${SKIP_GNSDK_REQUIREMENTS:-false}"

REQUIRE_GNSDK=false
if contains_target "emu" || contains_target "icc"; then
  REQUIRE_GNSDK=true
fi

if [ "$REQUIRE_GNSDK" = "true" ]; then
  if [ "$SKIP_GNSDK_REQUIREMENTS" = "true" ]; then
    echo "WARNING: SKIP_GNSDK_REQUIREMENTS=true -> skipping Gracenote SDK preflight checks and native GNSDK build."
    echo "         Default is false (full Gracenote build)."
  else
    echo "Gracenote SDK mode enabled (default): validating SDK payload before build."
    if [ ! -f "$GNSDK_REQUIRED_WRAPPER" ]; then
      echo "ERROR: Missing Gracenote wrapper source: $GNSDK_REQUIRED_WRAPPER" >&2
      echo "       The folder '$GNSDK_ROOT' appears incomplete (currently often only .gitkeep)." >&2
      echo "       Populate the full Gracenote SDK payload (include/lib/wrappers) before building." >&2
      exit 1
    fi
    if ! ls $GNSDK_REQUIRED_LIB_GLOB >/dev/null 2>&1; then
      echo "ERROR: Missing Gracenote native libs under $GNSDK_ROOT/lib/android_arm64-v8a" >&2
      echo "       Populate the full Gracenote SDK payload before building." >&2
      exit 1
    fi
  fi
else
  echo "Skipping Gracenote SDK preflight: selected target(s) do not require it."
fi

# ─────────────────────────────────────────────────────────────────────────────
# Token injection from testtoken.env (gitignored – never committed)
# Format:
#   #conradlibV2
#   <conradlib JWT token>
# ─────────────────────────────────────────────────────────────────────────────

TOKEN_ENV="$WORKSPACE_ROOT/testtoken.env"
if [ -f "$TOKEN_ENV" ]; then
  echo "Loading tokens from testtoken.env..."
  CONRADLIB_TOKEN=$(awk '/^#conradlibV2/{f=1;next} /^#/{f=0} f && NF{printf "%s",$0}' "$TOKEN_ENV")
  if [ -n "$CONRADLIB_TOKEN" ]; then
    mkdir -p "$WORKSPACE_ROOT/token"
    printf "%s" "$CONRADLIB_TOKEN" > "$WORKSPACE_ROOT/token/testtoken.txt"
    echo "  → token/testtoken.txt updated"
  else
    echo "WARNING: No #conradlibV2 token section found in testtoken.env"
  fi
else
  echo "WARNING: testtoken.env not found at $TOKEN_ENV"
  echo "         Builds may fail or use stale/placeholder tokens."
  echo "         Create testtoken.env at the workspace root (see README.md)."
fi

# ─────────────────────────────────────────────────────────────────────────────
# Helper: find the built APK and copy it to the output directory
# ─────────────────────────────────────────────────────────────────────────────

copy_apk() {
  local dest_name="$1"
  # The APK filename is based on archivesBaseName set in app/build.gradle.
  # It typically looks like MetadataService-LocalBuild-dev-row-debug.apk.
  local apk
  apk="$(find "$APK_SRC" -maxdepth 1 -name "*.apk" | head -n 1)"
  if [ -z "$apk" ]; then
    echo "ERROR: No APK found in $APK_SRC" >&2
    exit 1
  fi
  cp "$apk" "$OUTPUT_DIR/$dest_name"
  echo "  → $OUTPUT_DIR/$dest_name"
}

build_variant() {
  local profile="$1"
  local label="$2"
  local dest_name="$3"
  local gn_license_variant="${4:-VARIANT_OEM_AU}"
  local attempt=0

  banner "BUILD ${label}"

  cd "$MDS_DIR"

  until [ "$attempt" -gt "$MAX_RETRIES" ]; do
    if [ "$attempt" -gt 0 ]; then
      echo "Retry $attempt/$MAX_RETRIES for profile '$profile'..."
      # Best-effort cleanup before retry.
      ./gradlew --stop >/dev/null 2>&1 || true
      rm -rf "$MDS_DIR/.gradle/buildOutputCleanup" >/dev/null 2>&1 || true
    fi

    set +e
    GN_LICENSE_VARIANT="$gn_license_variant" ./gradlew $GRADLE_ARGS clean assembleDevRowDebug -Pmds.profile="$profile" -Pmds.identity="$BUILD_IDENTITY"
    rc=$?
    set -e

    if [ "$rc" -eq 0 ]; then
      copy_apk "$dest_name"
      return 0
    fi

    attempt=$((attempt + 1))
    if [ "$attempt" -gt "$MAX_RETRIES" ]; then
      echo "ERROR: Build failed for profile '$profile' after $((MAX_RETRIES + 1)) attempt(s)." >&2
      echo "       Last command: GN_LICENSE_VARIANT=$gn_license_variant ./gradlew $GRADLE_ARGS clean assembleDevRowDebug -Pmds.profile=$profile -Pmds.identity=$BUILD_IDENTITY" >&2
      exit "$rc"
    fi
  done
}

banner "IDENTITY ${BUILD_IDENTITY}"

if contains_target "emu"; then
  build_variant "emu" "Emu-generic (vanilla emulator)" "MetadataService-EmuGeneric-${APK_IDENTITY_SUFFIX}-dev-row-debug.apk" "VARIANT_OEM_AU"
fi

if contains_target "icc"; then
  build_variant "icc" "ICC (real testbench)" "MetadataService-ICC-${APK_IDENTITY_SUFFIX}-dev-row-debug.apk" "VARIANT_OEM_AU"
fi

if contains_target "no-gn"; then
  build_variant "no-gn" "No-GN (VW no-license)" "MetadataService-NoGN-${APK_IDENTITY_SUFFIX}-dev-row-debug.apk" "NONE"
fi

banner "ALL BUILDS COMPLETE"
echo "APKs are in: $OUTPUT_DIR/"
echo ""
ls -lh "$OUTPUT_DIR/"*.apk 2>/dev/null || true
echo ""
echo "Profiles:"
echo "  emu → MetadataService-EmuGeneric-* (vanilla emulator, DEV out-of-the-box)"
echo "  icc → MetadataService-ICC-*        (real testbench, LIVE)"
echo "  no-gn → MetadataService-NoGN-*     (real testbench behavior, GN license disabled)"
echo "Identity:"
echo "  new -> technology.cariad.entertainment.metadataservice / technology.cariad.entertainment.metadataserviceclient.api.IMetadataServiceAidl"
echo ""
echo "ConRadLib version shown in Settings screen (dev build only):"
echo "  Build → OI_<build-timestamp>"
