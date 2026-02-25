@echo off
REM Stop the QA Automation Platform
REM Usage: stop.bat

echo.
echo =========================================
echo   QA Automation Platform - STOP
echo =========================================
echo.

REM First try to stop process listening on port 8081
echo Checking for running process on port 8081...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081 ^| findstr LISTENING') do (
    echo Found PID %%a listening on port 8081; attempting to stop...
    taskkill /PID %%a /F /T >nul 2>&1 && (
        echo ✓ Process %%a stopped
    ) || (
        echo × Failed to stop process %%a
    )
)

REM Try to stop any Java process that has our jar in its command line (safer than killing all java.exe)
echo Attempting to stop Java processes that contain 'qa-automation-platform' in command line...
powershell -Command "Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -and $_.CommandLine -like '*qa-automation-platform*' } | ForEach-Object { Write-Output ('Stopping PID ' + $_.ProcessId); Stop-Process -Id $_.ProcessId -Force }" 2>nul

echo Stop complete.

echo.
echo =========================================
echo   Application stopped
echo =========================================
echo.

pause
