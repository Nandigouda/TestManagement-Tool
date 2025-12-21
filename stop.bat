@echo off
REM Stop the QA Automation Platform
REM Usage: stop.bat

echo.
echo =========================================
echo   QA Automation Platform - STOP
echo =========================================
echo.

REM Check if there's a process running on port 8081
echo Checking for running process on port 8081...
setlocal enabledelayedexpansion

for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081 ^| findstr LISTENING') do (
    set "PID=%%a"
    echo Found process with PID !PID! listening on port 8081
    echo Stopping process...
    taskkill /PID !PID! /F /T
    if !errorlevel! equ 0 (
        echo.
        echo ✓ Process stopped successfully
    ) else (
        echo.
        echo × Failed to stop process
    )
)

REM Also try to stop Java processes by name
echo.
echo Stopping any remaining Java processes...
taskkill /IM java.exe /F /T >nul 2>&1

if errorlevel 1 (
    echo No Java processes found
) else (
    echo ✓ Java processes terminated
)

echo.
echo =========================================
echo   Application stopped
echo =========================================
echo.

pause
