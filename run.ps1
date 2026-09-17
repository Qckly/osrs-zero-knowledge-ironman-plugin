$ErrorActionPreference = 'Stop'

$gradleVersion = '8.10'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$toolsDir = Join-Path $projectRoot '.tools'
$gradleHome = Join-Path $toolsDir "gradle-$gradleVersion"
$gradleExe = Join-Path $gradleHome 'bin\gradle.bat'
$zipPath = Join-Path $toolsDir "gradle-$gradleVersion-bin.zip"

Write-Host 'Zero Knowledge Ironman Guide - RuneLite dev launcher' -ForegroundColor Cyan

function Get-JdkMajorVersion([string]$jdkHomePath) {
    if (-not $jdkHomePath -or -not (Test-Path $jdkHomePath)) {
        return 0
    }

    # Prefer the JDK release file because it avoids PowerShell 5.1 stderr quirks
    # from java -version / javac -version.
    $releaseFile = Join-Path $jdkHomePath 'release'
    if (Test-Path $releaseFile) {
        try {
            $releaseText = Get-Content -Path $releaseFile -Raw
            if ($releaseText -match 'JAVA_VERSION="1\.(\d+)') {
                return [int]$Matches[1]
            }
            if ($releaseText -match 'JAVA_VERSION="(\d+)') {
                return [int]$Matches[1]
            }
        }
        catch {
        }
    }

    # Fallback: ask javac through cmd.exe so stderr handling cannot confuse
    # Windows PowerShell's ErrorActionPreference.
    $javac = Join-Path $jdkHomePath 'bin\javac.exe'
    if (Test-Path $javac) {
        try {
            $output = cmd.exe /d /c "`"$javac`" -version 2^>^&1"
            $text = ($output | Out-String)
            if ($text -match 'javac\s+1\.(\d+)') {
                return [int]$Matches[1]
            }
            if ($text -match 'javac\s+(\d+)') {
                return [int]$Matches[1]
            }
        }
        catch {
        }
    }

    return 0
}

function Test-JdkHome([string]$jdkHomePath) {
    if (-not $jdkHomePath -or -not (Test-Path $jdkHomePath)) {
        return $null
    }

    $java = Join-Path $jdkHomePath 'bin\java.exe'
    $javac = Join-Path $jdkHomePath 'bin\javac.exe'
    $major = Get-JdkMajorVersion $jdkHomePath

    if ((Test-Path $java) -and (Test-Path $javac) -and ($major -ge 11)) {
        return [PSCustomObject]@{
            Home = $jdkHomePath
            Java = $java
            Version = $major
        }
    }

    return $null
}

$selectedJdk = $null
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
                if ($value -and ($value -is [string])) {
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

# 4) Validate all discovered JDK homes, preferring the highest version.
$validJdks = @()
foreach ($jdkCandidateHome in $candidateHomes | Where-Object { $_ -and ($_ -is [string]) } | Select-Object -Unique) {
    $candidate = Test-JdkHome ([string]$jdkCandidateHome)
    if ($candidate) {
        $validJdks += $candidate
    }
}

if ($validJdks.Count -gt 0) {
    $selectedJdk = $validJdks | Sort-Object Version -Descending | Select-Object -First 1
}

# 5) Last fallback: derive a JDK home from javac.exe currently on PATH.
if (-not $selectedJdk) {
    $javacCommand = Get-Command javac.exe -ErrorAction SilentlyContinue
    if ($javacCommand) {
        $pathHome = Split-Path -Parent (Split-Path -Parent $javacCommand.Source)
        $selectedJdk = Test-JdkHome $pathHome
    }
}

if (-not $selectedJdk) {
    Write-Host ''
    Write-Host 'Java 11 or newer JDK was not found.' -ForegroundColor Red
    Write-Host 'Installed JDK locations checked:' -ForegroundColor Yellow
    foreach ($jdkCandidateHome in $candidateHomes | Where-Object { $_ -and ($_ -is [string]) } | Select-Object -Unique) {
        $major = Get-JdkMajorVersion ([string]$jdkCandidateHome)
        Write-Host "  $jdkCandidateHome  (detected major: $major)" -ForegroundColor DarkGray
    }
    Write-Host ''
    Write-Host 'Diagnostic command:' -ForegroundColor Yellow
    Write-Host '  Get-Content "C:\Program Files\Eclipse Adoptium\jdk-11.0.32.101-hotspot\release" | Select-String JAVA_VERSION' -ForegroundColor Gray
    exit 1
}

$env:JAVA_HOME = $selectedJdk.Home
$env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:Path
$javaExe = $selectedJdk.Java

Write-Host 'Java selected:' -ForegroundColor Green
Write-Host "  JDK $($selectedJdk.Version)" -ForegroundColor Green
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
