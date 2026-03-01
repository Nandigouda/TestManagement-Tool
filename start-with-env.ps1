#!/usr/bin/env powershell
# Start QA Automation Platform with environment configuration
# Usage: .\start-with-env.ps1

Write-Host "`n=========================================" -ForegroundColor Green
Write-Host "  QA Automation Platform" -ForegroundColor Green
Write-Host "  Build & Start with Environment Config" -ForegroundColor Green
Write-Host "=========================================`n" -ForegroundColor Green

# Set Java home
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Validate Java installation
if (!(Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    Write-Host "ERROR: Java 21 not found at $env:JAVA_HOME" -ForegroundColor Red
    Write-Host "Please ensure Java 21 is installed" -ForegroundColor Yellow
    exit 1
}

Write-Host "OK - Java 21 found" -ForegroundColor Green

# Load environment variables from .env file
Write-Host "`nLoading environment configuration..." -ForegroundColor Cyan
if (Test-Path ".env") {
    Write-Host "Found .env file - loading variables" -ForegroundColor Yellow
    
    # Read .env file and set environment variables
    $loadedCount = 0
    Get-Content .env | ForEach-Object {
        if ($_ -match '^\s*[^#]' -and $_ -match '=') {
            $parts = $_ -split '=', 2
            if ($parts.Count -eq 2) {
                $key = $parts[0].Trim()
                $value = $parts[1].Trim()
                if ($key -and $value) {
                    [Environment]::SetEnvironmentVariable($key, $value, "Process")
                    # Do NOT print actual values to avoid leaking secrets; only show the key name
                    Write-Host "  OK - Loaded: $key" -ForegroundColor Green
                    $loadedCount++
                }
            }
        }
    }
    Write-Host "  Summary: Loaded $loadedCount environment variable(s)." -ForegroundColor Cyan
} else {
    Write-Host "WARNING - .env file not found" -ForegroundColor Yellow
    Write-Host "  Create .env file for Azure OpenAI configuration" -ForegroundColor Yellow
    Write-Host "  Template: .env.example" -ForegroundColor Yellow
}

# Check if application is already running
Write-Host "`nChecking for running instances..." -ForegroundColor Cyan
$runningApp = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*qa-automation-platform*" }
if ($runningApp) {
    Write-Host "Found running application, stopping..." -ForegroundColor Yellow
    Stop-Process -InputObject $runningApp -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    Write-Host "OK - Previous instance stopped" -ForegroundColor Green
} else {
    Write-Host "OK - No existing instances" -ForegroundColor Green
}

# Kill any process on port 8081
Write-Host "`nChecking port 8081..." -ForegroundColor Cyan
$netstatOutput = netstat -aon | Select-String ":8081.*LISTENING" -ErrorAction SilentlyContinue
if ($netstatOutput) {
    $processId = $netstatOutput -split '\s+' | Select-Object -Last 1
    if ($processId -match '^\d+$') {
        Write-Host "Stopping process on port 8081..." -ForegroundColor Yellow
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 1
    }
}

# Build application
Write-Host "`nBuilding application..." -ForegroundColor Cyan
$buildLog = @()

# Determine a JDK 17 to use for the build (do not change the runtime JAVA_HOME used to launch the app)
$javaHomeForBuild = $null
$projectJavaHome = [Environment]::GetEnvironmentVariable('PROJECT_JAVA_HOME', 'Process')
if ($projectJavaHome -and (Test-Path (Join-Path $projectJavaHome 'bin\java.exe'))) {
    $javaHomeForBuild = $projectJavaHome
} elseif (Test-Path (Join-Path $PSScriptRoot 'tools\jdk17\bin\java.exe')) {
    $javaHomeForBuild = (Resolve-Path (Join-Path $PSScriptRoot 'tools\jdk17')).Path
} elseif (Test-Path 'C:\Program Files\Java\jdk-17\bin\java.exe') {
    $javaHomeForBuild = 'C:\Program Files\Java\jdk-17'
} else {
    # Fallback to whatever JAVA_HOME is already set (may be JDK 21)
    $javaHomeForBuild = $env:JAVA_HOME
}

Write-Host "  Using JAVA_HOME for build: $javaHomeForBuild" -ForegroundColor Yellow

# Temporarily set JAVA_HOME for the mvn/mvnd invocation and restore afterwards
$origJavaHome = $env:JAVA_HOME
if ($javaHomeForBuild) { $env:JAVA_HOME = $javaHomeForBuild }
try {
    # Prefer the bundled mvn if present (it will respect JAVA_HOME). Fallback to mvnd.
    $mvnCmdPath = Join-Path $PSScriptRoot 'tools\apache-maven-3.9.6\bin\mvn.cmd'
    if (Test-Path $mvnCmdPath) { $mvnCmd = $mvnCmdPath } else { $mvnCmd = 'mvnd.cmd' }
    Write-Host "  Maven command: $mvnCmd" -ForegroundColor Cyan
    # Capture full build output to a file for debugging
    $buildLogPath = Join-Path $PSScriptRoot 'build-script.log'
    Write-Host "  Writing build log to: $buildLogPath" -ForegroundColor Cyan
    & $mvnCmd clean package -DskipTests -e -X 2>&1 | Tee-Object -FilePath $buildLogPath
} finally {
    # restore original JAVA_HOME
    if ($null -ne $origJavaHome) { $env:JAVA_HOME = $origJavaHome } else { Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue }
}

 $artifactJar = Join-Path $PSScriptRoot 'target\qa-automation-platform-1.0.0-SNAPSHOT.jar'
 $artifactWar = Join-Path $PSScriptRoot 'target\qa-automation-platform-1.0.0-SNAPSHOT.war'
 if ((Test-Path $artifactJar) -or (Test-Path $artifactWar)) {
    Write-Host "`nOK - Build successful" -ForegroundColor Green
    Write-Host "`n=========================================" -ForegroundColor Green
    Write-Host "  Starting QA Automation Platform" -ForegroundColor Green
    Write-Host "=========================================`n" -ForegroundColor Green
    Write-Host "Server Details:" -ForegroundColor Cyan
    Write-Host "  URL: http://localhost:8081/api/v1" -ForegroundColor Yellow
    Write-Host "  Port: 8081" -ForegroundColor Yellow
    Write-Host "  Java: 21" -ForegroundColor Yellow
    Write-Host "`nEnvironment:" -ForegroundColor Cyan
    Write-Host "  Azure OpenAI Enabled: $([Environment]::GetEnvironmentVariable('AZURE_OPENAI_ENABLED', 'Process'))" -ForegroundColor Yellow
    Write-Host "`nPress Ctrl+C to stop the application`n" -ForegroundColor Yellow

    $artifactPath = if (Test-Path $artifactWar) { $artifactWar } else { $artifactJar }
    & "$env:JAVA_HOME\bin\java.exe" -jar $artifactPath
 } else {
    Write-Host "`nERROR - Build FAILED!" -ForegroundColor Red
    exit 1
 }
