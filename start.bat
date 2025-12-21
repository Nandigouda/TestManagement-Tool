@echo off
REM Start the QA Automation Platform
REM Usage: start.bat

echo.
echo =========================================
echo   QA Automation Platform - START
echo =========================================
echo.

REM Set Java home
set JAVA_HOME=C:\Program Files\Java\jdk-21

REM Check if JAR exists
if not exist "target\qa-automation-platform-1.0.0-SNAPSHOT.jar" (
    echo.
    echo ERROR: JAR file not found!
    echo Please build the application first using: mvn clean package -DskipTests
    echo.
    pause
    exit /b 1
)

REM Check if Java is installed
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo.
    echo ERROR: Java 21 not found at %JAVA_HOME%
    echo Please install Java 21 or set JAVA_HOME environment variable
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

REM Start the application
echo.
echo Starting QA Automation Platform...
echo.
echo Server will be available at: http://localhost:8081/api/v1
echo.
echo Press Ctrl+C to stop the application
echo.

"%JAVA_HOME%\bin\java.exe" -jar "target\qa-automation-platform-1.0.0-SNAPSHOT.jar"

pause
