$ErrorActionPreference = 'Stop'

$gradleVersion = '8.10'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$toolsDir = Join-Path $projectRoot '.tools'
$gradleHome = Join-Path $toolsDir "gradle-$gradleVersion"
$gradleExe = Join-Path $gradleHome 'bin\gradle.bat'
$zipPath = Join-Path $toolsDir "gradle-$gradleVersion-bin.zip"

Write-Host 'Zero Knowledge Ironman Guide - RuneLite dev launcher' -ForegroundColor Cyan

function Get-JavaMajorVersion([string]$javaExe) {
    try {
        $line = (& $javaExe -version 2>&1 | Select-Object -First 1).ToString()
        if ($line -match 'version\s+"1\.(\d+)') {
            return [int]$Matches[1]
        }
        if ($line -match 'version\s+"(\d+)') {
            return [int]$Matches[1]
        }
    }
    catch {
    }
    return 0
}

$javaExe = $null

# Prefer JAVA_HOME if it already points to a suitable JDK.
if ($env:JAVA_HOME) {
    $candidate = Join-Path $env:JAVA_HOME 'bin\java.exe'
    if ((Test-Path $candidate) -and ((Get-JavaMajorVersion $candidate) -ge 11)) {
        $javaExe = $candidate
    }
}

# Look for Eclipse Temurin / Adoptium JDK installations even when PATH still points to Java 8.
if (-not $javaExe) {
    $jdkRoots = @(
        (Join-Path $env:ProgramFiles 'Eclipse Adoptium'),
        (Join-Path $env:ProgramFiles 'AdoptOpenJDK'),
        (Join-Path $env:ProgramFiles 'Java')
    )

    foreach ($root in $jdkRoots) {
        if (-not (Test-Path $root)) {
            continue
        }

        $jdkDirs = Get-ChildItem -Path $root -Directory -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending

        foreach ($jdkDir in $jdkDirs) {
            $candidate = Join-Path $jdkDir.FullName 'bin\java.exe'
            if ((Test-Path $candidate) -and ((Get-JavaMajorVersion $candidate) -ge 11)) {
                $javaExe = $candidate
                $env:JAVA_HOME = $jdkDir.FullName
                break
            }
        }

        if ($javaExe) {
            break
        }
    }
}

# Finally try whatever java.exe Windows currently resolves from PATH.
if (-not $javaExe) {
    $javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($javaCommand -and ((Get-JavaMajorVersion $javaCommand.Source) -ge 11)) {
        $javaExe = $javaCommand.Source
        $env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent $javaExe)
    }
}

if (-not $javaExe) {
    Write-Host ''
    Write-Host 'Java 11 or newer JDK was not found.' -ForegroundColor Red
    Write-Host 'Temurin JDK 11 may be installed, but the launcher could not locate it.' -ForegroundColor Yellow
    Write-Host 'Expected location is usually under C:\Program Files\Eclipse Adoptium\.' -ForegroundColor Yellow
    exit 1
}

$env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent $javaExe)
$env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:Path

$javaVersion = & $javaExe -version 2>&1
Write-Host 'Java selected:' -ForegroundColor Green
$javaVersion | Select-Object -First 1 | ForEach-Object { Write-Host "  $_" }
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor DarkGray

if (-not (Test-Path $gradleExe)) {
    New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null

    if (-not (Test-Path $zipPath)) {
        $downloadUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
        Write-Host "Downloading Gradle $gradleVersion..." -ForegroundColor Yellow
        Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath
    }

    Write-Host 'Extracting Gradle...' -ForegroundColor Yellow
    Expand-Archive -Path $zipPath -DestinationPath $toolsDir -Force
}

if (-not (Test-Path $gradleExe)) {
    Write-Host 'Gradle bootstrap failed: gradle.bat was not found after extraction.' -ForegroundColor Red
    exit 1
}

Write-Host 'Starting RuneLite developer client...' -ForegroundColor Green
Push-Location $projectRoot
try {
    & $gradleExe run
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
