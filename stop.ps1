#!/usr/bin/env powershell
# Stop the QA Automation Platform
# Usage: .\stop.ps1

Write-Host "`n=========================================" -ForegroundColor Red
Write-Host "  QA Automation Platform - STOP" -ForegroundColor Red
Write-Host "=========================================`n" -ForegroundColor Red

$stopped = $false

# Kill process on port 8081
Write-Host "Checking for running process on port 8081..." -ForegroundColor Cyan

$netstatOutput = netstat -aon | Select-String ":8081.*LISTENING" -ErrorAction SilentlyContinue

if ($netstatOutput) {
    $pid = $netstatOutput -split '\s+' | Select-Object -Last 1
    if ($pid -match '^\d+$') {
        Write-Host "Found process with PID $pid listening on port 8081" -ForegroundColor Yellow
        Write-Host "Stopping process..." -ForegroundColor Cyan
        Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 1
        Write-Host "OK - Process on port 8081 stopped successfully" -ForegroundColor Green
        $stopped = $true
    }
} else {
    Write-Host "No process found on port 8081" -ForegroundColor Yellow
}

# Also stop Java processes running this application
Write-Host "`nStopping any QA Automation Platform Java processes..." -ForegroundColor Cyan
$javaProcesses = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*qa-automation-platform*" }

if ($javaProcesses) {
    $javaProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "OK - QA Platform processes terminated" -ForegroundColor Green
    $stopped = $true
} else {
    Write-Host "No QA Platform processes found" -ForegroundColor Yellow
}

Write-Host "`n=========================================" -ForegroundColor Green
if ($stopped) {
    Write-Host "  Application stopped successfully" -ForegroundColor Green
} else {
    Write-Host "  No running application found" -ForegroundColor Yellow
}
Write-Host "=========================================`n" -ForegroundColor Green
