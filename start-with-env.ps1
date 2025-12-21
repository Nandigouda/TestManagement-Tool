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
    Get-Content .env | ForEach-Object {
        if ($_ -match '^\s*[^#]' -and $_ -match '=') {
            $parts = $_ -split '=', 2
            if ($parts.Count -eq 2) {
                $key = $parts[0].Trim()
                $value = $parts[1].Trim()
                if ($key -and $value) {
                    [Environment]::SetEnvironmentVariable($key, $value, "Process")
                    # Mask sensitive values in output
                    if ($key -like "*KEY*" -or $key -like "*TOKEN*" -or $key -like "*SECRET*") {
                        $displayValue = if ($value.Length -gt 4) { $value.Substring(0, 4) + "...***" } else { "***" }
                        Write-Host "  OK - $key = $displayValue" -ForegroundColor Green
                    } else {
                        Write-Host "  OK - $key = $value" -ForegroundColor Green
                    }
                }
            }
        }
    }
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
    $pid = $netstatOutput -split '\s+' | Select-Object -Last 1
    if ($pid -match '^\d+$') {
        Write-Host "Stopping process on port 8081..." -ForegroundColor Yellow
        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 1
    }
}

# Build application
Write-Host "`nBuilding application..." -ForegroundColor Cyan
$buildLog = @()
& mvnd.cmd clean package -DskipTests 2>&1 | Tee-Object -Variable buildLog | Out-Null

if (Test-Path "target\qa-automation-platform-1.0.0-SNAPSHOT.jar") {
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
    
    & "$env:JAVA_HOME\bin\java.exe" -jar "target\qa-automation-platform-1.0.0-SNAPSHOT.jar"
} else {
    Write-Host "`nERROR - Build FAILED!" -ForegroundColor Red
    exit 1
}
