#!/bin/bash
#
# Copyright (c) 2026 Cariad SE
# All rights reserved.
#
# Author: Ritabroto Ganguly (T2-CD), Volkswagen Group / Cariad SE
#
# Backup original de.esolutions MetadataService artifacts from a target device.
# Uses adb pull only and stores the backup under ./backup_original_mds/
# relative to this script.
#
# Run from ./scripts/:  bash backup_original_mds.sh [--serial <serial>]
#
# The script:
#   1. Creates a timestamped backup directory under ./backup_original_mds/
#   2. Detects the original de.esolutions MetadataService codePath via dumpsys
#   3. Pulls the entire priv-app folder for that package
#   4. Pulls the single privapp permission XML overwritten by install_target.sh
#   5. Saves the package folder and XML directly inside the timestamp folder
#

set -euo pipefail

export MSYS_NO_PATHCONV=1
export MSYS2_ARG_CONV_EXCL="*"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_ROOT="$SCRIPT_DIR/backup_original_mds"

SERIAL_ARGS=()

usage() {
    echo "Usage: $0 [-s <serial>|--serial <serial>]"
    echo ""
    echo "Backs up the original de.esolutions MetadataService folder and"
    echo "the privapp-permissions-metadataservice.xml file from the connected target device."
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        -s|--serial)
            SERIAL_ARGS=(-s "$2")
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "ERROR: Unsupported argument '$1'" >&2
            usage
            exit 1
            ;;
    esac
done

adb_cmd() { adb "${SERIAL_ARGS[@]}" "$@"; }
adb_shell() { adb_cmd shell "$@"; }

command -v adb >/dev/null 2>&1 || {
    echo "ERROR: adb not found in PATH." >&2
    exit 1
}

STAMP="$(date +%Y%m%d_%H%M%S)"
BACKUP_DIR="$BACKUP_ROOT/$STAMP"
TARGET_XML="/product/etc/permissions/privapp-permissions-metadataservice.xml"

OLD_PACKAGES=(
    "de.esolutions.media.metadataservice"
    "de.esolutions.metadataservice"
)

mkdir -p "$BACKUP_DIR"

get_code_path() {
    local pkg="$1"
    adb_shell "dumpsys package '$pkg' 2>/dev/null | grep 'codePath=' | head -1 | awk -F= '{print \$2}'" | tr -d '\r'
}

backup_remote() {
    local remote="$1"
    local dest_name="$(basename "$remote")"
    local local_expected="$BACKUP_DIR/$dest_name"
    local adb_local_target="$BACKUP_DIR"
    local pull_output=""

    # On Windows Git Bash, adb.exe cannot write to /c/... style paths.
    # Convert only the local destination to a native Windows path.
    if command -v cygpath >/dev/null 2>&1; then
        adb_local_target="$(cygpath -w "$BACKUP_DIR")"
    fi

    echo "Backing up: $remote"
    if ! pull_output="$(adb_cmd pull "$remote" "$adb_local_target" 2>&1)"; then
        echo "ERROR: adb pull failed for $remote" >&2
        echo "$pull_output" >&2
        exit 1
    fi
    if [[ ! -e "$local_expected" ]]; then
        echo "ERROR: Backup target missing after pull: $local_expected" >&2
        echo "$pull_output" >&2
        exit 1
    fi
}

CODE_PATH=""
for pkg in "${OLD_PACKAGES[@]}"; do
    CODE_PATH="$(get_code_path "$pkg")"
    if [[ -n "$CODE_PATH" ]]; then
        echo "Detected original package: $pkg"
        break
    fi
done

if [[ -n "$CODE_PATH" ]]; then
    backup_remote "$CODE_PATH"
else
    echo "No original de.esolutions MetadataService package found via dumpsys"
fi

backup_remote "$TARGET_XML"

echo "Backup complete: $BACKUP_DIR"
