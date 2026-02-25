@echo off
REM Start the QA Automation Platform
REM Usage: start.bat

echo.
echo =========================================
echo   QA Automation Platform - START
echo =========================================
echo.

REM Prefer workspace JDK under tools\jdk17 if present
setlocal enabledelayedexpansion
set "JDK_BASE=tools\jdk17"
set "JAVA_HOME="
if exist "%JDK_BASE%" (
    for /f "delims=" %%D in ('dir /b /ad "%JDK_BASE%" 2^>nul') do (
        if not defined JAVA_HOME set "JAVA_HOME=%JDK_BASE%\%%D"
    )
)

if not defined JAVA_HOME (
    REM fallback to environment or default path
    if defined JAVA_HOME (
        echo Using JAVA_HOME from environment: %JAVA_HOME%
    ) else (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    )
)

REM Check if Java is installed
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo.
    echo ERROR: Java not found at %JAVA_HOME%
    echo Please install a compatible JDK or set JAVA_HOME environment variable
    echo.
    pause
    exit /b 1
)

REM Kill any existing process on port 8081
echo Checking for existing process on port 8081...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081 ^| findstr LISTENING') do (
    echo Stopping process with PID %%a...
    taskkill /PID %%a /F /T >nul 2>&1
)

REM Start the application (use workspace Maven to build if jar missing)
echo.
echo Starting QA Automation Platform...
echo Server will be available at: http://localhost:8081/api/v1

set "MVN_CMD=tools\apache-maven-3.9.6\bin\mvn.cmd"
if not exist "%MVN_CMD%" set "MVN_CMD=mvn"

if not exist "target\qa-automation-platform-1.0.0-SNAPSHOT.jar" (
    echo JAR not found; attempting to build using %MVN_CMD% ...
    "%MVN_CMD%" -DskipTests clean package || (
        echo Build failed. Aborting.
        exit /b 1
    )
)

REM Launch in a new window so the script can exit while the app keeps running
start "QA Automation" "%JAVA_HOME%\bin\java.exe" -jar "target\qa-automation-platform-1.0.0-SNAPSHOT.jar"

echo Application started. Use stop.bat to stop it, or run netstat/tasklist to inspect.
endlocal
