<#
.SYNOPSIS
    Capture MetadataService logs from adb device.

.DESCRIPTION
    Run from ./scripts/:  .\capture_mds_logs.ps1 [options]
    Or:                   powershell -ExecutionPolicy Bypass -File capture_mds_logs.ps1

.DESCRIPTION
    Run from ./scripts/:  .\capture_mds_logs.ps1 [options]
    Or:                   powershell -ExecutionPolicy Bypass -File capture_mds_logs.ps1

.PARAMETER Serial
    ADB device serial or host:port

.PARAMETER Package
    Package name to capture logs for (default: technology.cariad.entertainment.metadataservice)

.PARAMETER OutputDir
    Output directory for logs (default: ..\output\logs)

.PARAMETER ProjectWide
    Include project-wide log patterns (default: true)

.PARAMETER RestartSafe
    Restart adb logcat safely without loss

.EXAMPLE
    .\capture_mds_logs.ps1 -Identity new
    .\capture_mds_logs.ps1 -Identity new -Serial 172.16.250.248:5555
    .\\capture_mds_logs.ps1 -OutputDir ..\logs
#>

param(
    [ValidateSet('new', '')]
    [string]$Identity = '',
    [string]$Serial = "172.16.250.248",
    [string]$Package = "technology.cariad.entertainment.metadataservice",
    [string]$OutputDir = "..\output\logs",
    [bool]$ProjectWide = $true,
    [string[]]$ExtraPatterns = @(),
    [switch]$RestartSafe
)

# Resolve package from identity shortcut if provided
if ($Identity -eq 'new') {
    $Package = 'technology.cariad.entertainment.metadataservice'
}

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    throw "adb is not available in PATH."
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$outFile = Join-Path $OutputDir ("mds_repro_{0}.log" -f $timestamp)

$projectPatterns = @(
    "\s[VDIWEF]\s+mds/",
    "\s[VDIWEF]\s+metadataservice\.connectedradioclient:",
    "\[ServiceListApiBinding-STUB\]",
    "\[DisableReasonsApiBinding-STUB\]",
    "\bmds_probe\b",
    "\bmdsprobe\b",
    "\bmds/probe\b",
    "\bconnectedradio-v2\b",
    "\bgnsdk\b",
    "\bgracenote\b",
    "\bgmdlib\b",
    "\bConRad\b",
    "\bToken source=coreservices\b",
    "\bCan't receive token from CoreServices\b",
    "\bAuthenticatorException\b",
    "\bAccountManager(Service)?\b",
    "\bgetAuthToken\b",
    "\btransactionTypeID\b",
    "\boperationTypeID\b",
    "\bserviceId\b",
    "\boperationId\b",
    "\btechnology\.cariad\.magic\.oauth\b",
    "\bCoreServices\b",
    "\bMagicAdapter\b",
    "\bIAccountAuthenticator\b",
    "\bMagicServiceFsm\b",
    "\bTokenResult\b",
    "\btokenStatus\b",
    "\bError code:\s*5[12]\b",
    "\bfailed to get token with ErrorCode\s*#5[12]\b",
    "\btransactionTypeId\b",
    "\boperationTypeId\b",
    # ConRad Internal Result Codes — key error missed by \bConRad\b word boundary
    "IRC\[ConRadLib",
    "\bConRadLibError",
    # SSOA / auth registration
    "\bSSOA\b",
    "\bregistration.*fail\b",
    "ConRadLibErrorRegistration",
    # Broader error capture
    "onComplete:.*fail",
    "station update fail",
    "\bstationParts\b",
    "\bretry=DELAYED\b"
)

function Get-FilterPattern {
    $patterns = @()

    if (-not $ProjectWide) {
        $patterns += [regex]::Escape($Package)
    }

    $patterns += $projectPatterns

    foreach ($pattern in $ExtraPatterns) {
        if (-not [string]::IsNullOrWhiteSpace($pattern)) {
            $patterns += $pattern
        }
    }

    $patterns = $patterns | Select-Object -Unique
    return ($patterns -join "|")
}

Write-Host "[mds-capture] Device: $Serial"
Write-Host "[mds-capture] Package: $Package"
Write-Host "[mds-capture] Project-wide mode: $ProjectWide"
if (-not $ProjectWide) {
    Write-Host "[mds-capture] Package-only mode enabled (special case)."
}
Write-Host "[mds-capture] Output: $outFile"
Write-Host "[mds-capture] Clearing existing logcat buffer..."
& adb -s $Serial logcat -c | Out-Null

function Start-RestartSafeCapture {
    $filterPattern = Get-FilterPattern

    Write-Host "[mds-capture] Starting restart-safe capture (project filter)."
    Write-Host "[mds-capture] Filter pattern: $filterPattern"
    Write-Host "[mds-capture] Reproduce issue now. Press Ctrl+C to stop."
    & adb -s $Serial logcat -v threadtime |
        Select-String -Pattern $filterPattern |
        ForEach-Object { $_.Line } |
        Tee-Object -FilePath $outFile
}

if ($RestartSafe -or $ProjectWide) {
    Start-RestartSafeCapture
    exit 0
}

$appPid = $null
for ($i = 1; $i -le 20; $i++) {
    $candidate = (& adb -s $Serial shell pidof -s $Package).Trim()
    if ($candidate) {
        $appPid = $candidate
        break
    }
    Start-Sleep -Milliseconds 500
}

if (-not $appPid) {
    Write-Warning "[mds-capture] PID not found for $Package. Falling back to restart-safe capture."
    Start-RestartSafeCapture
    exit 0
}

Write-Host "[mds-capture] PID: $appPid"
Write-Host "[mds-capture] Starting PID-scoped capture."
Write-Host "[mds-capture] Reproduce issue now. Press Ctrl+C to stop."
& adb -s $Serial logcat -v threadtime --pid=$appPid |
    Tee-Object -FilePath $outFile
