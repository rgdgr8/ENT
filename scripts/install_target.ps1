<#
.SYNOPSIS
    Install MetadataService as a privileged system app on a real target device.
    Run from ./scripts/:  .\install_target.ps1 [-Identity backup] [-ApkPath path]
    Or:                   powershell -ExecutionPolicy Bypass -File install_target.ps1

.DESCRIPTION
    Prerequisites (do these before running the script):
        adb root
        adb remount

        !!! WARNING !!!
        The non-backup install path infers the target package from the APK filename.
        Supported APK naming patterns:
            de-esolutions-metadataservice -> de.esolutions.metadataservice
            tech-cariad                   -> technology.cariad.entertainment.metadataservice
            backup                        -> restore from the latest .\backup_original_mds\ timestamp folder

    The script:
      1. Verifies the device is connected and /product is writable
      2. Removes all existing competing priv-app variants
    3. Pushes the APK into /product/priv-app/<package>/, or restores the latest backup folder for legacy
    4. Sets ownership, permissions, and restores the SELinux label
      5. Clears the PackageManager cache and reboots

.PARAMETER Identity
        Optional mode selector. Use only 'backup' to restore the latest backup_original_mds timestamp folder.

.PARAMETER ApkPath
    Path to the APK file to install. If omitted, the latest APK in ..\output\ is used.

.PARAMETER Serial
    ADB serial number (or host:port) of the target device.
    Required when more than one device is connected.

.EXAMPLE
    .\install_target.ps1
    .\install_target.ps1 -ApkPath ..\output\MetadataService-ICC-tech-cariad-dev-row-debug.apk
    .\install_target.ps1 -Identity backup
    .\install_target.ps1 -Serial 172.16.250.248:5555
#>

param(
    [ValidateSet('backup', '')]
    [string]$Identity = '',
    [string]$ApkPath  = '',
    [string]$Serial   = ''
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# ─── Package identifiers ──────────────────────────────────────────────────────
$LegacyPkg            = 'de.esolutions.metadataservice'
$NewPkg               = 'technology.cariad.entertainment.metadataservice'
$PrivAppBase          = '/product/priv-app'
$PermissionsXmlName   = 'privapp-permissions-metadataservice.xml'
$PermissionsXmlTarget = '/product/etc/permissions/' + $PermissionsXmlName

$ScriptDir     = Split-Path -Parent $MyInvocation.MyCommand.Path
$WorkspaceRoot = (Resolve-Path (Join-Path $ScriptDir '..')).Path
$BackupRoot    = Join-Path $ScriptDir 'backup_original_mds'

$PermissionsXmlLocalScriptDir = Join-Path $ScriptDir $PermissionsXmlName
$PermissionsXmlLocalRepo      = Join-Path $WorkspaceRoot ('metadataservice\deploy\' + $PermissionsXmlName)

if (Test-Path $PermissionsXmlLocalScriptDir) {
    $PermissionsXmlLocal = $PermissionsXmlLocalScriptDir
} else {
    $PermissionsXmlLocal = $PermissionsXmlLocalRepo
}

function Get-LatestBackupDir {
    if (-not (Test-Path $BackupRoot)) {
        return ''
    }
    $dirs = Get-ChildItem -Path $BackupRoot -Directory -ErrorAction SilentlyContinue |
            Sort-Object Name
    if (-not $dirs) {
        return ''
    }
    return $dirs[-1].FullName
}

# ─── Validate identity ────────────────────────────────────────────────────────
if ($Identity -and ($Identity -ne 'backup')) {
    Write-Host '[ERROR] Only backup is supported as an explicit Identity value.' -ForegroundColor Red
    exit 1
}

$TargetPkg     = ''
$TargetApkName = ''
$TargetRemoteDir = ''
$BackupSourceDir = ''
$BackupSourceXml = ''

function Get-TargetPackageFromApkName {
    param([Parameter(Mandatory=$true)][string]$ApkName)

    if ($ApkName -like '*de-esolutions-metadataservice*') {
        return $LegacyPkg
    }
    if ($ApkName -like '*tech-cariad*') {
        return $NewPkg
    }
    throw ('Unsupported APK name: ' + $ApkName + '. Expected tech-cariad or de-esolutions-metadataservice in the filename.')
}

if ($Identity -eq 'backup') {
    $latestBackupDir = Get-LatestBackupDir
    if (-not $latestBackupDir) {
        Write-Fail ('No backup_original_mds timestamp folder found in ' + $BackupRoot + '. Run backup_original_mds.sh first.')
    }
    $TargetPkg = $LegacyPkg
    $BackupSourceDir = Join-Path $latestBackupDir $TargetPkg
    $BackupSourceXml = Join-Path $latestBackupDir $PermissionsXmlName
    if (-not (Test-Path $BackupSourceDir)) {
        Write-Fail ('Backup package folder not found: ' + $BackupSourceDir)
    }
    if (-not (Test-Path $BackupSourceXml)) {
        Write-Fail ('Backup XML not found: ' + $BackupSourceXml)
    }
}

# ─── Helpers ──────────────────────────────────────────────────────────────────
function Write-Ok([string]$msg)   { Write-Host ('[OK]   ' + $msg) -ForegroundColor Green }
function Write-Info([string]$msg) { Write-Host ('[INFO] ' + $msg) -ForegroundColor Cyan }
function Write-Warn([string]$msg) { Write-Host ('[WARN] ' + $msg) -ForegroundColor Yellow }
function Write-Fail([string]$msg) { Write-Host ('[ERROR] ' + $msg) -ForegroundColor Red; exit 1 }
function Write-Banner([string]$title) {
    Write-Host ''
    Write-Host ('=' * 50) -ForegroundColor Blue
    Write-Host ('  ' + $title) -ForegroundColor Blue
    Write-Host ('=' * 50) -ForegroundColor Blue
    Write-Host ''
}

function Invoke-Adb {
    param([Parameter(Mandatory=$true)][string[]]$CmdArgs, [switch]$AllowFail)
    if ($Serial) {
        $allArgs = @('-s', $Serial) + @($CmdArgs)
    } else {
        $allArgs = @($CmdArgs)
    }
    # PS5.1: relax ErrorActionPreference around native adb to suppress stderr termination
    $oldEap = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $output = & adb @allArgs 2>&1
        $rc = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $oldEap
    }
    $text = ($output | ForEach-Object { $_.ToString() }) -join "`n"
    if ((-not $AllowFail) -and ($rc -ne 0)) {
        throw ('adb command failed (exit=' + $rc + '): adb ' + ($allArgs -join ' ') + "`n" + $text)
    }
    return $text.Trim()
}

function Invoke-AdbShell([string]$cmd, [switch]$AllowFail) {
    return Invoke-Adb -CmdArgs @('shell', $cmd) -AllowFail:$AllowFail
}

function Get-PhysicalAdbSerial {
    $serials = @()
    $rows = (& adb devices) | Select-Object -Skip 1
    foreach ($row in $rows) {
        if (-not $row) { continue }
        $cols = $row -split '\s+'
        if ($cols.Count -lt 2) { continue }
        if ($cols[1] -ne 'device') { continue }
        if ($cols[0] -match ':') { continue }
        $serials += $cols[0]
    }
    return $serials
}

# ─── Auto-detect source ───────────────────────────────────────────────────────
if (-not $ApkPath) {
    if ($Identity -eq 'backup') {
        $ApkPath = $BackupSourceDir
    } else {
    Write-Info 'No APK specified - searching output\ for latest APK...'
    $outputDir = Join-Path $WorkspaceRoot 'output'
    $apks = Get-ChildItem -Path $outputDir -Filter '*.apk' -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending
    if (-not $apks) {
        Write-Fail ('No APK found in ' + $outputDir + '. Run build_all_variants.sh first or pass -ApkPath explicitly.')
    }
    $ApkPath = $apks[0].FullName
    Write-Info ('Found: ' + $apks[0].Name)
    }
}

if ($Identity -eq 'backup') {
    if (-not (Test-Path $BackupSourceDir)) {
        Write-Fail ('Backup package folder not found: ' + $BackupSourceDir)
    }
} elseif (-not (Test-Path $ApkPath)) {
    Write-Fail ('APK not found: ' + $ApkPath)
}

if ($Identity -ne 'backup') {
    $TargetPkg = Get-TargetPackageFromApkName -ApkName (Split-Path -Leaf $ApkPath)
}

$TargetApkName = $TargetPkg + '.apk'
$TargetRemoteDir = $PrivAppBase + '/' + $TargetPkg

if ($Identity -eq 'backup') {
    $PermissionsXmlLocal = $BackupSourceXml
} elseif (-not (Test-Path $PermissionsXmlLocal)) {
    Write-Fail ('Required privapp allowlist XML not found: ' + $PermissionsXmlLocal)
}

# ─── Checks ───────────────────────────────────────────────────────────────────
Write-Banner 'MetadataService - Target Install'

if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    Write-Fail 'adb not found in PATH. Add Android SDK platform-tools to PATH.'
}

# Device connected?
$deviceLines = @((& adb devices) | Select-Object -Skip 1 | Where-Object { $_ -match 'device$' })
$deviceCount = $deviceLines.Count

if ($deviceCount -eq 0) {
    Write-Fail 'No device detected. Connect the bench unit and retry. Tip: adb devices'
}
if (($deviceCount -gt 1) -and (-not $Serial)) {
    $physicalSerials = Get-PhysicalAdbSerial
    if ($physicalSerials.Count -eq 1) {
        $Serial = $physicalSerials[0]
        Write-Info ('Multiple adb devices; using physical target: ' + $Serial)
    } else {
        Write-Warn 'Multiple devices detected. Use -Serial <id> to select one:'
        (& adb devices -l) | Select-Object -Skip 1 | Where-Object { $_ } | ForEach-Object { Write-Host ('    ' + $_) }
        Write-Fail 'Aborting - ambiguous target.'
    }
}

$deviceSerial = Invoke-Adb -CmdArgs @('get-serialno') -AllowFail
Write-Ok ('Device: ' + $deviceSerial)

# Running as root?
$whoami = Invoke-AdbShell 'whoami' -AllowFail
if ($whoami -ne 'root') {
    Write-Fail 'Device shell is not running as root. Run: adb root; adb remount; then re-run this script.'
}
Write-Ok 'Shell running as root'

# /product is writable? Test with separate adb shell calls (avoids && in shell string)
Invoke-AdbShell 'touch /product/.install_write_test' -AllowFail | Out-Null
$writeCheck = Invoke-AdbShell 'test -f /product/.install_write_test && echo OK || echo FAIL' -AllowFail
Invoke-AdbShell 'rm -f /product/.install_write_test' -AllowFail | Out-Null
if ($writeCheck -notmatch 'OK') {
    Write-Fail '/product is not writable - remount not active. Run: adb root; adb remount; then re-run this script.'
}
Write-Ok '/product is writable (remount active)'

$apkBaseName = Split-Path -Leaf $ApkPath
Write-Host ''
if ($Identity -eq 'backup') {
    Write-Info ('Backup folder: ' + $BackupSourceDir)
} else {
    Write-Info ('APK:       ' + $apkBaseName)
}
Write-Info ('Package:   ' + $TargetPkg)
Write-Info ('Target:    ' + $TargetRemoteDir + '/')
Write-Info ('Allowlist: ' + $PermissionsXmlTarget)
if ($Identity -eq 'backup') {
    Write-Info ('XML src:   ' + $PermissionsXmlLocal)
}
Write-Host ''

# ─── Step 1: Clean slate ──────────────────────────────────────────────────────
Write-Banner 'Step 1 - Clean slate (remove competing variants)'

foreach ($pkg in @($LegacyPkg, $NewPkg)) {
    $pkgDir    = $PrivAppBase + '/' + $pkg
    # String concatenation avoids PS5.1 parsing && || inside double-quoted strings
    $pkgExists = Invoke-AdbShell ('[ -d "' + $pkgDir + '" ] && echo yes || echo no') -AllowFail

    if ($pkgExists -eq 'yes') {
        Write-Info ('Removing priv-app variant: ' + $pkg)
        Invoke-AdbShell ('rm -rf "' + $pkgDir + '"') -AllowFail | Out-Null
    }

    $pkgListRaw = Invoke-AdbShell 'pm list packages 2>/dev/null' -AllowFail
    $pkgFound   = @(($pkgListRaw -split "`n") | Where-Object { $_ -match ('package:' + [regex]::Escape($pkg) + '$') })
    if ($pkgFound.Count -gt 0) {
        Invoke-AdbShell ('pm uninstall ' + $pkg + ' 2>/dev/null') -AllowFail | Out-Null
    }
}
Write-Ok 'All competing variants removed'

# ─── Step 2: Install APK ──────────────────────────────────────────────────────
Write-Banner 'Step 2 - Install APK'

if ($Identity -eq 'backup') {
    Write-Info 'Creating priv-app directory...'
    Invoke-AdbShell ('mkdir -p ' + $PrivAppBase) | Out-Null

    Write-Info 'Pushing backup package folder (this may take a moment)...'
    Invoke-Adb -CmdArgs @('push', $BackupSourceDir, $PrivAppBase)
    Write-Ok ('Package folder pushed to ' + $TargetRemoteDir)
} else {
    $newDir = $PrivAppBase + '/' + $TargetPkg
    Write-Info 'Creating priv-app directory...'
    Invoke-AdbShell ('mkdir -p ' + $newDir) | Out-Null

    $targetPath = $newDir + '/' + $TargetApkName
    Write-Info 'Pushing APK (this may take a moment)...'
    Invoke-Adb -CmdArgs @('push', $ApkPath, $targetPath)
    Write-Ok ('APK pushed to ' + $targetPath)
}

# ─── Step 3: Install privapp allowlist ───────────────────────────────────────
Write-Banner 'Step 3 - Install privapp allowlist'

Write-Info 'Pushing privapp allowlist XML...'
Invoke-AdbShell 'mkdir -p /product/etc/permissions' | Out-Null
Invoke-Adb -CmdArgs @('push', $PermissionsXmlLocal, $PermissionsXmlTarget)
Write-Ok ('Allowlist XML pushed to ' + $PermissionsXmlTarget)

Write-Info 'Setting allowlist ownership to root:root...'
Invoke-AdbShell ('chown root:root ' + $PermissionsXmlTarget) | Out-Null
Write-Ok 'Allowlist ownership set'

Write-Info 'Setting allowlist permissions to 644...'
Invoke-AdbShell ('chmod 644 ' + $PermissionsXmlTarget) | Out-Null
Write-Ok 'Allowlist permissions set'

Write-Info 'Restoring allowlist SELinux context...'
$restoreconXml = Invoke-AdbShell ('restorecon -v ' + $PermissionsXmlTarget + ' 2>&1') -AllowFail
if ($restoreconXml) { Write-Info ('  ' + $restoreconXml) }
Write-Ok 'Allowlist SELinux label restored'

# ─── Step 4: APK permissions and SELinux ─────────────────────────────────────
Write-Banner 'Step 4 - APK permissions and SELinux'

if ($Identity -eq 'backup') {
    Write-Info 'Setting ownership to root:root recursively...'
    Invoke-AdbShell ('chown -R root:root "' + $TargetRemoteDir + '"') | Out-Null
    Write-Ok 'Ownership set'

    Write-Info 'Setting directory permissions...'
    Invoke-AdbShell ('find "' + $TargetRemoteDir + '" -type d -exec chmod 755 {} +') | Out-Null
    Invoke-AdbShell ('find "' + $TargetRemoteDir + '" -type f -exec chmod 644 {} +') | Out-Null
    Write-Ok 'Permissions set'

    Write-Info 'Restoring SELinux context recursively...'
    $restorecon = Invoke-AdbShell ('restorecon -Rv "' + $TargetRemoteDir + '" 2>&1') -AllowFail
    if ($restorecon) { Write-Info ('  ' + $restorecon) }
    Write-Ok 'SELinux label restored'
} else {
    Write-Info 'Setting ownership to root:root...'
    Invoke-AdbShell ('chown root:root ' + $targetPath) | Out-Null
    Write-Ok 'Ownership set'

    Write-Info 'Setting file permissions to 644...'
    Invoke-AdbShell ('chmod 644 ' + $targetPath) | Out-Null
    Write-Ok 'Permissions set'

    Write-Info 'Restoring SELinux context...'
    $restorecon = Invoke-AdbShell ('restorecon -v ' + $targetPath + ' 2>&1') -AllowFail
    if ($restorecon) { Write-Info ('  ' + $restorecon) }
    Write-Ok 'SELinux label restored'
}

# ─── Step 5: Clear package cache ─────────────────────────────────────────────
Write-Banner 'Step 5 - Clear package cache'

Write-Info 'Clearing PackageManager parse cache...'
Invoke-AdbShell 'rm -rf /data/system/package_cache/*' -AllowFail | Out-Null
Write-Ok 'Package cache cleared'

# ─── Step 6: Reboot ──────────────────────────────────────────────────────────
Write-Banner 'Step 6 - Reboot'

Write-Host '  The device will now reboot. Priv-apps are scanned at boot time.' -ForegroundColor Yellow
Write-Host '  After reboot, verify the install with:' -ForegroundColor Yellow
Write-Host ''
Write-Host '    adb shell pm list packages | findstr metadataservice'
Write-Host ('    adb shell dumpsys package ' + $TargetPkg + ' | findstr versionName')
Write-Host ''

Invoke-Adb -CmdArgs @('reboot') -AllowFail | Out-Null

Write-Banner 'Done'
Write-Ok 'Reboot triggered. Waiting for device to come back online...'
Invoke-Adb -CmdArgs @('wait-for-device') | Out-Null
Write-Ok 'Device back online.'

Write-Info 'Re-establishing root and remount after reboot...'
Invoke-Adb -CmdArgs @('root') -AllowFail | Out-Null
if ($LASTEXITCODE -eq 0) {
    Invoke-Adb -CmdArgs @('wait-for-device') -AllowFail | Out-Null
    Invoke-Adb -CmdArgs @('remount') -AllowFail | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Ok 'Remount active again (/product writable)'
    } else {
        Write-Warn 'adb remount failed after reboot. Run adb root; adb remount manually if needed.'
    }
} else {
    Write-Warn 'adb root failed after reboot. Run adb root; adb remount manually if needed.'
}

Write-Host ''
Write-Host '  Verify install:' -ForegroundColor Cyan
Write-Host '    adb shell pm list packages | findstr metadataservice' -ForegroundColor Cyan
Write-Host ''
