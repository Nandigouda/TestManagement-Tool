@echo off
REM Build and start the QA Automation Platform
REM Usage: build-and-start.bat

echo.
echo =========================================
echo   QA Automation Platform - BUILD & START
echo =========================================
echo.

set JAVA_HOME=C:\Program Files\Java\jdk-21
set MAVEN_HOME=C:\Program Files\maven-mvnd-1.0.2-windows-amd64

REM Check Java
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo ERROR: Java 21 not found
    pause
    exit /b 1
)

REM Check Maven
if not exist "%MAVEN_HOME%\bin\mvnd.cmd" (
    echo ERROR: Maven not found
    pause
    exit /b 1
)

REM Kill existing process
echo Stopping any running instance...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081 ^| findstr LISTENING') do (
    taskkill /PID %%a /F /T >nul 2>&1
)

REM Build
echo.
echo Building application...
echo.
call "%MAVEN_HOME%\bin\mvnd.cmd" clean package -DskipTests -q

if not exist "target\qa-automation-platform-1.0.0-SNAPSHOT.jar" (
    echo ERROR: Build failed
    pause
    exit /b 1
)

echo.
echo ✓ Build successful
echo.
echo Starting application...
echo.
echo Server will be available at: http://localhost:8081/api/v1
echo.
echo Press Ctrl+C to stop
echo.

"%JAVA_HOME%\bin\java.exe" -jar "target\qa-automation-platform-1.0.0-SNAPSHOT.jar"

pause
