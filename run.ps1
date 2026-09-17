$ErrorActionPreference = 'Stop'

$gradleVersion = '8.10'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$toolsDir = Join-Path $projectRoot '.tools'
$gradleHome = Join-Path $toolsDir "gradle-$gradleVersion"
$gradleExe = Join-Path $gradleHome 'bin\gradle.bat'
$zipPath = Join-Path $toolsDir "gradle-$gradleVersion-bin.zip"

Write-Host 'Zero Knowledge Ironman Guide - RuneLite dev launcher' -ForegroundColor Cyan

try {
    $javaVersion = & java -version 2>&1
    Write-Host 'Java detected:' -ForegroundColor Green
    $javaVersion | Select-Object -First 1 | ForEach-Object { Write-Host "  $_" }
}
catch {
    Write-Host ''
    Write-Host 'Java was not found.' -ForegroundColor Red
    Write-Host 'Install a JDK, then reopen PowerShell and run this script again.' -ForegroundColor Yellow
    exit 1
}

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
