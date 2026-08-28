#!/bin/bash
#
# Copyright (c) 2026 Cariad SE
# All rights reserved.
#
# Author: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
# Author: Ritabroto Ganguly (T2-CD), Volkswagen Group / Cariad SE
#
# Install MetadataService as a privileged system app on a real target device.
#
# Run from ./scripts/:  bash install_target.sh [backup] [apk_path]
#
# !!!!!!!! Prerequisites (do these before running the script and if script run exits unexpectedly):
#   adb root && adb remount
#
# Usage:
#   ./install_target.sh                          # auto-detects latest APK in output/
#   ./install_target.sh path/to/MyApp.apk        # explicit APK path
#   ./install_target.sh backup                   # restores latest backup_original_mds timestamp folder
#   ./install_target.sh -s <serial> [apk_path]  # target specific device
#
# !!! WARNING !!!
# The non-backup install path infers the target package from the APK filename.
# Supported APK naming patterns:
#   de-esolutions-metadataservice -> de.esolutions.metadataservice
#   tech-cariad                   -> technology.cariad.entertainment.metadataservice
#
# The script:
#   1. Verifies the device is connected and /product is writable
#   2. Removes any existing competing app variants
#   3. Pushes the APK into /product/priv-app/<package>/
#   4. Sets ownership, permissions, and restores SELinux label
#   5. Clears the PackageManager cache and reboots
#

set -euo pipefail

# Prevent Git Bash / MSYS2 from converting Unix-style paths (e.g. /product/priv-app)
# to Windows paths when passing them as arguments to native Windows binaries like adb.
export MSYS_NO_PATHCONV=1
export MSYS2_ARG_CONV_EXCL="*"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Package identifiers (all supported variants)
LEGACY_PKG="de.esolutions.metadataservice"
NEW_PKG="technology.cariad.entertainment.metadataservice"
PRIV_APP_BASE="/product/priv-app"
BACKUP_ROOT="$SCRIPT_DIR/backup_original_mds"
PERMISSIONS_XML_NAME="privapp-permissions-metadataservice.xml"
PERMISSIONS_XML_LOCAL_SCRIPT_DIR="$SCRIPT_DIR/$PERMISSIONS_XML_NAME"
PERMISSIONS_XML_LOCAL_REPO="$WORKSPACE_ROOT/metadataservice/deploy/$PERMISSIONS_XML_NAME"
PERMISSIONS_XML_TARGET="/product/etc/permissions/$PERMISSIONS_XML_NAME"

# Package name extracted from APK
TARGET_PKG=""
TARGET_APK_NAME=""
TARGET_REMOTE_DIR=""
BACKUP_SOURCE_DIR=""
BACKUP_SOURCE_XML=""

if [[ -f "$PERMISSIONS_XML_LOCAL_SCRIPT_DIR" ]]; then
    PERMISSIONS_XML_LOCAL="$PERMISSIONS_XML_LOCAL_SCRIPT_DIR"
else
    PERMISSIONS_XML_LOCAL="$PERMISSIONS_XML_LOCAL_REPO"
fi

# ─── Colors ───────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

ok()   { echo -e "${GREEN}✓${NC} $*"; }
info() { echo -e "${CYAN}→${NC} $*"; }
warn() { echo -e "${YELLOW}⚠${NC} $*"; }
fail() { echo -e "${RED}✗ ERROR:${NC} $*" >&2; exit 1; }
banner() {
    echo ""
    echo -e "${BLUE}${BOLD}══════════════════════════════════════════════${NC}"
    echo -e "${BLUE}${BOLD}  $1${NC}"
    echo -e "${BLUE}${BOLD}══════════════════════════════════════════════${NC}"
    echo ""
}

# ─── Argument parsing ─────────────────────────────────────────────────────────
SERIAL_ARGS=()
APK_PATH=""
INSTALL_MODE="apk"

while [[ $# -gt 0 ]]; do
    case "$1" in
        -s|--serial)
            SERIAL_ARGS=(-s "$2")
            shift 2
            ;;
        backup)
            INSTALL_MODE="backup"
            shift
            ;;
        -*)
            fail "Unknown option: $1"
            ;;
        *)
            APK_PATH="$1"
            shift
            ;;
    esac
done

infer_target_pkg() {
    local apk_name="$1"
    if [[ "$apk_name" == *de-esolutions-metadataservice* ]]; then
        printf '%s\n' "$LEGACY_PKG"
    elif [[ "$apk_name" == *tech-cariad* ]]; then
        printf '%s\n' "$NEW_PKG"
    else
        return 1
    fi
}

get_latest_backup_dir() {
    find "$BACKUP_ROOT" -mindepth 1 -maxdepth 1 -type d 2>/dev/null | sort | tail -n 1
}


adb_cmd() { adb "${SERIAL_ARGS[@]}" "$@"; }
adb_shell() { adb_cmd shell "$@"; }

# adb push requires the local path in Windows format (native adb binary on Windows).
# cygpath -w converts Git Bash Unix paths (e.g. /c/Users/...) to Windows paths (C:\Users\...).
adb_push() {
    local local_path="$1"
    local remote_path="$2"
    local win_path
    if command -v cygpath &>/dev/null; then
        win_path=$(cygpath -w "$local_path")
    else
        win_path="$local_path"
    fi
    adb_cmd push "$win_path" "$remote_path"
}

choose_physical_adb_serial() {
    local physical_serials=()
    while read -r serial state _; do
        [[ -z "$serial" ]] && continue
        [[ "$state" != "device" ]] && continue
        [[ "$serial" == *:* ]] && continue
        physical_serials+=("$serial")
    done < <(adb_cmd devices | tail -n +2)

    if [[ ${#physical_serials[@]} -eq 1 ]]; then
        printf '%s\n' "${physical_serials[0]}"
        return 0
    fi

    return 1
}

# ─── Auto-detect source ───────────────────────────────────────────────────────
if [[ "$INSTALL_MODE" == "backup" ]]; then
    BACKUP_SOURCE_DIR="$(get_latest_backup_dir || true)"
    [[ -n "$BACKUP_SOURCE_DIR" ]] || fail "No backup_original_mds timestamp folder found in $BACKUP_ROOT. Run backup_original_mds.sh first."
    BACKUP_SOURCE_DIR="$BACKUP_SOURCE_DIR/$LEGACY_PKG"
    BACKUP_SOURCE_XML="$(dirname "$BACKUP_SOURCE_DIR")/$PERMISSIONS_XML_NAME"
    [[ -d "$BACKUP_SOURCE_DIR" ]] || fail "Backup package folder not found: $BACKUP_SOURCE_DIR"
    [[ -f "$BACKUP_SOURCE_XML" ]] || fail "Backup XML not found: $BACKUP_SOURCE_XML"
    TARGET_PKG="$LEGACY_PKG"
else
    if [[ -z "$APK_PATH" ]]; then
        info "No APK specified — searching output/ for latest APK..."
        APK_PATH=$(find "$WORKSPACE_ROOT/output" -maxdepth 1 -name "*.apk" 2>/dev/null | sort | tail -n 1 || true)
        if [[ -z "$APK_PATH" ]]; then
            fail "No APK found in $WORKSPACE_ROOT/output/. Run build_all_variants.sh first or pass the APK path explicitly."
        fi
        info "Found: $(basename "$APK_PATH")"
    fi

    [[ -f "$APK_PATH" ]] || fail "APK not found: $APK_PATH"
    TARGET_PKG="$(infer_target_pkg "$(basename "$APK_PATH")")" || fail "Unsupported APK name: $(basename "$APK_PATH"). Expected tech-cariad or de-esolutions-metadataservice in the filename."
fi

TARGET_APK_NAME="${TARGET_PKG}.apk"
TARGET_REMOTE_DIR="$PRIV_APP_BASE/$TARGET_PKG"

if [[ "$INSTALL_MODE" == "backup" ]]; then
    PERMISSIONS_XML_LOCAL="$BACKUP_SOURCE_XML"
else
    [[ -f "$PERMISSIONS_XML_LOCAL" ]] || fail "Required privapp allowlist XML not found: $PERMISSIONS_XML_LOCAL"
fi

# ─── Checks ───────────────────────────────────────────────────────────────────
banner "MetadataService — Target Install"

# adb available?
command -v adb &>/dev/null || fail "adb not found in PATH. Add the Android SDK platform-tools to PATH."

# Device connected?
DEVICE_COUNT=$(adb_cmd devices | awk 'NR>1 && /device$/ {count++} END {print count+0}')
if [[ "$DEVICE_COUNT" -eq 0 ]]; then
    fail "No device detected via adb. Connect the bench unit and retry.\n" \
         "  Tip: adb devices"
elif [[ "$DEVICE_COUNT" -gt 1 && "${#SERIAL_ARGS[@]}" -eq 0 ]]; then
    if SELECTED_SERIAL=$(choose_physical_adb_serial); then
        SERIAL_ARGS=(-s "$SELECTED_SERIAL")
        info "Multiple adb devices detected; using physical target: $SELECTED_SERIAL"
    else
        warn "Multiple devices detected. Use -s <serial> to select one:"
        adb_cmd devices -l | tail -n +2 | grep -v "^$" | sed 's/^/    /'
        echo ""
        fail "Aborting — ambiguous target."
    fi
fi

DEVICE_SERIAL=$(adb_cmd get-serialno 2>/dev/null || echo "unknown")
ok "Device: $DEVICE_SERIAL"

# Running as root?
WHOAMI=$(adb_shell whoami 2>/dev/null | tr -d '\r')
if [[ "$WHOAMI" != "root" ]]; then
    fail "Device shell is not running as root.\n" \
         "  Run:  adb root && adb remount\n" \
         "  Then re-run this script."
fi
ok "Shell running as root"

# /product is writable? (remount check)
WRITE_TEST=$(adb_shell "touch /product/.install_write_test 2>&1 && echo OK && rm /product/.install_write_test" | tr -d '\r')
if [[ "$WRITE_TEST" != "OK" ]]; then
    fail "/product is not writable — remount not active.\n" \
         "  Run:  adb root && adb remount\n" \
         "  Then re-run this script."
fi
ok "/product is writable (remount active)"

echo ""
if [[ "$INSTALL_MODE" == "backup" ]]; then
    info "Backup folder: $BACKUP_SOURCE_DIR"
else
    APK_BASENAME=$(basename "$APK_PATH")
    info "APK:      $APK_BASENAME"
fi
info "Package:  $TARGET_PKG"
info "Target:   $TARGET_REMOTE_DIR/"
info "Allowlist: $PERMISSIONS_XML_TARGET"
if [[ "$INSTALL_MODE" == "backup" ]]; then
    info "Source:   $BACKUP_SOURCE_DIR"
    info "XML src:  $PERMISSIONS_XML_LOCAL"
fi
echo ""

# ─── Remove existing app (clean slate) ────────────────────────────────────
banner "Step 1 — Clean slate (remove competing variants)"

# Always remove all variants to ensure clean install
for pkg in "$LEGACY_PKG" "$NEW_PKG"; do
    PKG_DIR="$PRIV_APP_BASE/$pkg"
    PKG_EXISTS=$(adb_shell "[ -d \"$PKG_DIR\" ] && echo yes || echo no" | tr -d '\r')
    
    if [[ "$PKG_EXISTS" == "yes" ]]; then
        info "Removing priv-app variant: $pkg"
        adb_shell "rm -rf \"$PKG_DIR\""
    fi
    
    # Also check PackageManager
    PKG_INSTALLED=$(adb_shell "pm list packages 2>/dev/null | grep -F \"package:$pkg\" || echo" | tr -d '\r')
    if [[ -n "$PKG_INSTALLED" ]]; then
        adb_shell "pm uninstall \"$pkg\" 2>/dev/null || true"
    fi
done
ok "All competing variants removed"

# ─── Install new APK ──────────────────────────────────────────────────────────
banner "Step 2 — Install APK"
if [[ "$INSTALL_MODE" == "backup" ]]; then
    info "Creating priv-app directory..."
    adb_shell "mkdir -p \"$PRIV_APP_BASE\""
    info "Pushing backup package folder (this may take a moment)..."
    adb_push "$BACKUP_SOURCE_DIR" "$PRIV_APP_BASE"
    ok "Package folder pushed to $TARGET_REMOTE_DIR"
else
    NEW_DIR="$PRIV_APP_BASE/$TARGET_PKG"
    info "Creating priv-app directory..."
    adb_shell "mkdir -p \"$NEW_DIR\""

    TARGET_PATH="$NEW_DIR/$TARGET_APK_NAME"
    info "Pushing APK (this may take a moment)..."
    adb_push "$APK_PATH" "$TARGET_PATH"
    ok "APK pushed to $TARGET_PATH"
fi

# ─── Install privapp allowlist ────────────────────────────────────────────────
banner "Step 3 — Install privapp allowlist"

info "Pushing privapp allowlist XML..."
adb_shell "mkdir -p \"/product/etc/permissions\""
adb_push "$PERMISSIONS_XML_LOCAL" "$PERMISSIONS_XML_TARGET"
ok "Allowlist XML pushed to $PERMISSIONS_XML_TARGET"

info "Setting allowlist ownership to root:root..."
adb_shell "chown root:root \"$PERMISSIONS_XML_TARGET\""
ok "Allowlist ownership set"

info "Setting allowlist permissions to 644..."
adb_shell "chmod 644 \"$PERMISSIONS_XML_TARGET\""
ok "Allowlist permissions set"

info "Restoring allowlist SELinux context..."
RESTORECON_XML_OUT=$(adb_shell "restorecon -v \"$PERMISSIONS_XML_TARGET\" 2>&1" | tr -d '\r')
if [[ -n "$RESTORECON_XML_OUT" ]]; then
    info "  $RESTORECON_XML_OUT"
fi
ok "Allowlist SELinux label restored"

# ─── Set ownership, permissions, SELinux ──────────────────────────────────────
banner "Step 4 — APK permissions and SELinux"

if [[ "$INSTALL_MODE" == "backup" ]]; then
    info "Setting ownership to root:root recursively..."
    adb_shell "chown -R root:root \"$TARGET_REMOTE_DIR\""
    ok "Ownership set"

    info "Setting directory permissions..."
    adb_shell "find \"$TARGET_REMOTE_DIR\" -type d -exec chmod 755 {} +"
    adb_shell "find \"$TARGET_REMOTE_DIR\" -type f -exec chmod 644 {} +"
    ok "Permissions set"

    info "Restoring SELinux context recursively..."
    RESTORECON_OUT=$(adb_shell "restorecon -Rv \"$TARGET_REMOTE_DIR\" 2>&1" | tr -d '\r')
    if [[ -n "$RESTORECON_OUT" ]]; then
        info "  $RESTORECON_OUT"
    fi
    ok "SELinux label restored"
else
    info "Setting ownership to root:root..."
    adb_shell "chown root:root \"$TARGET_PATH\""
    ok "Ownership set"

    info "Setting file permissions to 644..."
    adb_shell "chmod 644 \"$TARGET_PATH\""
    ok "Permissions set"

    info "Restoring SELinux context..."
    RESTORECON_OUT=$(adb_shell "restorecon -v \"$TARGET_PATH\" 2>&1" | tr -d '\r')
    if [[ -n "$RESTORECON_OUT" ]]; then
        info "  $RESTORECON_OUT"
    fi
    ok "SELinux label restored"
fi

# ─── Clear PackageManager cache ───────────────────────────────────────────────
banner "Step 5 — Clear package cache"

info "Clearing PackageManager parse cache..."
adb_shell "rm -rf /data/system/package_cache/*" 2>/dev/null || true
ok "Package cache cleared"

# ─── Reboot ───────────────────────────────────────────────────────────────────
banner "Step 6 — Reboot"

echo -e "${YELLOW}  The device will now reboot. Priv-apps are scanned at boot time.${NC}"
echo -e "${YELLOW}  After reboot, verify the install with:${NC}"
echo ""
echo -e "    adb shell pm list packages | grep metadataservice"
echo -e "    adb shell dumpsys package $TARGET_PKG | grep versionName"
echo ""

adb_cmd reboot

banner "Done"
ok "Reboot triggered. Waiting for device to come back online..."
adb_cmd wait-for-device
ok "Device back online."

info "Re-establishing root and remount after reboot..."
if adb_cmd root >/dev/null 2>&1; then
    adb_cmd wait-for-device >/dev/null 2>&1 || true
    if adb_cmd remount >/dev/null 2>&1; then
        ok "Remount active again (/product writable)"
    else
        warn "adb remount failed after reboot. Run 'adb root && adb remount' manually if you need further system writes."
    fi
else
    warn "adb root failed after reboot. Run 'adb root && adb remount' manually if you need further system writes."
fi
echo ""
echo -e "  Verify install:"
echo -e "    ${CYAN}adb shell pm list packages | grep metadataservice${NC}"
echo ""
