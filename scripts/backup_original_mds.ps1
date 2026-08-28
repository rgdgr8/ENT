<#
.SYNOPSIS
    Backup original de.esolutions MetadataService artifacts from a target device.

.DESCRIPTION
    Uses adb pull only and stores a timestamped backup under
    .\backup_original_mds\ relative to this script.

    The script:
      1. Creates a timestamped backup directory under .\backup_original_mds\
    2. Detects the original de.esolutions MetadataService codePath via dumpsys
        3. Pulls the entire priv-app folder for that package
    4. Pulls the single privapp permission XML overwritten by install_target.ps1
        5. Saves the package folder and XML directly inside the timestamp folder

.PARAMETER Serial
    Optional adb serial (or host:port) to target a specific device.

.EXAMPLE
    .\backup_original_mds.ps1
    .\backup_original_mds.ps1 -Serial 172.16.250.248:5555
#>

param(
    [string]$Serial = ''
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackupRoot = Join-Path $ScriptDir 'backup_original_mds'

if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    throw 'adb not found in PATH.'
}

$adbPrefix = @()
if ($Serial) {
    $adbPrefix = @('-s', $Serial)
}

$stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$backupDir = Join-Path $BackupRoot $stamp
$targetXml = '/product/etc/permissions/privapp-permissions-metadataservice.xml'
$oldPackages = @(
    'de.esolutions.media.metadataservice',
    'de.esolutions.metadataservice'
)

function Get-CodePath {
    param([Parameter(Mandatory=$true)][string]$PackageName)

    $result = & adb @adbPrefix shell "dumpsys package $PackageName" 2>$null
    foreach ($line in ($result | ForEach-Object { $_.ToString() })) {
        if ($line -match 'codePath=(.+)$') {
            return $matches[1].Trim()
        }
    }
    return ''
}

function Backup-Remote {
    param([Parameter(Mandatory=$true)][string]$RemotePath)

    $destName = [System.IO.Path]::GetFileName($RemotePath.Replace('/', '\'))
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

    Write-Host ('Backing up: ' + $RemotePath)
    $oldEap = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        $output = & adb @adbPrefix pull $RemotePath $backupDir 2>&1
        $rc = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $oldEap
    }

    if ($rc -ne 0) {
        $text = ($output | ForEach-Object { $_.ToString() }) -join "`n"
        throw ('adb pull failed for ' + $RemotePath + ' (exit=' + $rc + ')' + "`n" + $text)
    }

    $localExpected = Join-Path $backupDir $destName
    if (-not (Test-Path $localExpected)) {
        throw ('Backup target missing after pull: ' + $localExpected)
    }
}

$codePath = ''
foreach ($packageName in $oldPackages) {
    $codePath = Get-CodePath -PackageName $packageName
    if ($codePath) {
        Write-Host ('Detected original package: ' + $packageName)
        break
    }
}

if ($codePath) {
    Backup-Remote -RemotePath $codePath
} else {
    Write-Host 'No original de.esolutions MetadataService package found via dumpsys'
}

Backup-Remote -RemotePath $targetXml

Write-Host ('Backup complete: ' + $backupDir)
