#!/usr/bin/env powershell
# Start the QA Automation Platform
# Usage: .\start.ps1

Write-Host "`n=========================================" -ForegroundColor Green
Write-Host "  QA Automation Platform - START" -ForegroundColor Green
Write-Host "=========================================`n" -ForegroundColor Green

# Set Java home
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Validate Java installation
if (!(Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    Write-Host "ERROR: Java 21 not found at $env:JAVA_HOME" -ForegroundColor Red
    Write-Host "Please ensure Java 21 is installed in: $env:JAVA_HOME" -ForegroundColor Yellow
    exit 1
}

Write-Host "OK - Java found: $env:JAVA_HOME" -ForegroundColor Green

# Check if JAR exists
if (!(Test-Path "target\qa-automation-platform-1.0.0-SNAPSHOT.jar")) {
    Write-Host "ERROR: JAR file not found!" -ForegroundColor Red
    Write-Host "Please build the application first using:" -ForegroundColor Yellow
    Write-Host "  mvnd.cmd clean package -DskipTests" -ForegroundColor Yellow
    Write-Host "Or use: .\start-with-env.ps1 (to build and start)" -ForegroundColor Yellow
    exit 1
}

Write-Host "OK - JAR file found" -ForegroundColor Green

# Kill any existing process on port 8081
Write-Host "`nChecking for existing process on port 8081..." -ForegroundColor Cyan
$netstatOutput = netstat -aon | Select-String ":8081.*LISTENING" -ErrorAction SilentlyContinue

if ($netstatOutput) {
    $pid = $netstatOutput -split '\s+' | Select-Object -Last 1
    if ($pid -match '^\d+$') {
        Write-Host "Stopping existing process with PID $pid..." -ForegroundColor Yellow
        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 1
        Write-Host "OK - Previous process stopped" -ForegroundColor Green
    }
} else {
    Write-Host "OK - No existing process on port 8081" -ForegroundColor Green
}

# Start the application
Write-Host "`nStarting QA Automation Platform..." -ForegroundColor Cyan
Write-Host "Port: 8081" -ForegroundColor Cyan
Write-Host "URL: http://localhost:8081/api/v1" -ForegroundColor Yellow
Write-Host "`nPress Ctrl+C to stop the application`n" -ForegroundColor Yellow
Write-Host "=========================================`n" -ForegroundColor Green

& "$env:JAVA_HOME\bin\java.exe" -jar "target\qa-automation-platform-1.0.0-SNAPSHOT.jar"
