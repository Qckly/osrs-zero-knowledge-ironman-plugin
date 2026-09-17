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
        $lines = & $javaExe -version 2>&1
        $text = ($lines | Out-String)
        if ($text -match 'version\s+"1\.(\d+)') {
            return [int]$Matches[1]
        }
        if ($text -match 'version\s+"(\d+)') {
            return [int]$Matches[1]
        }
    }
    catch {
    }
    return 0
}

function Test-JdkHome([string]$jdkHomePath) {
    if (-not $jdkHomePath -or -not (Test-Path $jdkHomePath)) {
        return $null
    }

    $java = Join-Path $jdkHomePath 'bin\java.exe'
    $javac = Join-Path $jdkHomePath 'bin\javac.exe'
    if ((Test-Path $java) -and (Test-Path $javac) -and ((Get-JavaMajorVersion $java) -ge 11)) {
        return $java
    }

    return $null
}

$javaExe = $null
$javaHome = $null
$candidateHomes = New-Object System.Collections.Generic.List[string]

# 1) Existing JAVA_HOME.
if ($env:JAVA_HOME) {
    $candidateHomes.Add($env:JAVA_HOME)
}

# 2) Registry locations used by Temurin/Adoptium and other JDK vendors.
$registryPatterns = @(
    'HKLM:\SOFTWARE\Eclipse Adoptium\JDK\*\hotspot\MSI',
    'HKLM:\SOFTWARE\WOW6432Node\Eclipse Adoptium\JDK\*\hotspot\MSI',
    'HKLM:\SOFTWARE\AdoptOpenJDK\JDK\*\hotspot\MSI',
    'HKLM:\SOFTWARE\JavaSoft\JDK\*',
    'HKLM:\SOFTWARE\JavaSoft\Java Development Kit\*'
)

foreach ($pattern in $registryPatterns) {
    try {
        foreach ($key in Get-ItemProperty -Path $pattern -ErrorAction SilentlyContinue) {
            foreach ($propertyName in @('Path', 'JavaHome', 'InstallationPath')) {
                $value = $key.$propertyName
                if ($value) {
                    $candidateHomes.Add([string]$value)
                }
            }
        }
    }
    catch {
    }
}

# 3) Common installation roots. Search recursively because vendor folder layouts differ.
$roots = @()
if ($env:ProgramFiles) { $roots += $env:ProgramFiles }
if (${env:ProgramFiles(x86)}) { $roots += ${env:ProgramFiles(x86)} }
if ($env:LOCALAPPDATA) { $roots += $env:LOCALAPPDATA }

$vendorFolders = @(
    'Eclipse Adoptium',
    'AdoptOpenJDK',
    'Java',
    'Microsoft',
    'Zulu',
    'Amazon Corretto'
)

foreach ($root in $roots | Select-Object -Unique) {
    foreach ($vendor in $vendorFolders) {
        $vendorRoot = Join-Path $root $vendor
        if (-not (Test-Path $vendorRoot)) {
            continue
        }

        try {
            Get-ChildItem -Path $vendorRoot -Filter javac.exe -File -Recurse -ErrorAction SilentlyContinue |
                ForEach-Object {
                    $binDir = Split-Path -Parent $_.FullName
                    $discoveredJdkHome = Split-Path -Parent $binDir
                    $candidateHomes.Add($discoveredJdkHome)
                }
        }
        catch {
        }
    }
}

# 4) Try all discovered JDK homes, preferring the highest Java version.
$validJdks = @()
foreach ($jdkCandidateHome in $candidateHomes | Where-Object { $_ } | Select-Object -Unique) {
    $candidate = Test-JdkHome $jdkCandidateHome
    if ($candidate) {
        $validJdks += [PSCustomObject]@{
            Home = $jdkCandidateHome
            Java = $candidate
            Version = Get-JavaMajorVersion $candidate
        }
    }
}

if ($validJdks.Count -gt 0) {
    $selected = $validJdks | Sort-Object Version -Descending | Select-Object -First 1
    $javaExe = $selected.Java
    $javaHome = $selected.Home
}

# 5) Last fallback: java/javac currently on PATH.
if (-not $javaExe) {
    $javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
    $javacCommand = Get-Command javac.exe -ErrorAction SilentlyContinue
    if ($javaCommand -and $javacCommand -and ((Get-JavaMajorVersion $javaCommand.Source) -ge 11)) {
        $javaExe = $javaCommand.Source
        $javaHome = Split-Path -Parent (Split-Path -Parent $javaExe)
    }
}

if (-not $javaExe) {
    Write-Host ''
    Write-Host 'Java 11 or newer JDK was not found.' -ForegroundColor Red
    Write-Host 'Installed JDK locations checked:' -ForegroundColor Yellow
    foreach ($jdkCandidateHome in $candidateHomes | Where-Object { $_ } | Select-Object -Unique) {
        Write-Host "  $jdkCandidateHome" -ForegroundColor DarkGray
    }
    Write-Host ''
    Write-Host 'Diagnostic command:' -ForegroundColor Yellow
    Write-Host '  Get-ChildItem "C:\Program Files" -Filter javac.exe -Recurse -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName' -ForegroundColor Gray
    exit 1
}

$env:JAVA_HOME = $javaHome
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
