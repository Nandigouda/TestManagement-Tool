@echo off
REM Minimal runner: supports `start` and `stop` commands only.
REM Usage:
REM   build-and-start.bat start   -> build and run the app
REM   build-and-start.bat stop    -> stop any process listening on port 8081

if "%1"=="stop" goto stop
if "%1"=="start" goto start

echo Usage: %~n0 start ^| stop
exit /b 1

:start
echo Building application...

REM Prefer bundled Maven in tools\apache-maven-3.9.6
set "MAVEN=%~dp0tools\apache-maven-3.9.6\bin\mvn.cmd"
if exist "%MAVEN%" (
    call "%MAVEN%" clean package -DskipTests -q
) else (
    echo ERROR: mvn not found at %MAVEN%
    exit /b 1
)

REM Accept either WAR or JAR produced by the build
if exist "target\qa-automation-platform-1.0.0-SNAPSHOT.war" (
    set "ART=target\qa-automation-platform-1.0.0-SNAPSHOT.war"
) else if exist "target\qa-automation-platform-1.0.0-SNAPSHOT.jar" (
    set "ART=target\qa-automation-platform-1.0.0-SNAPSHOT.jar"
) else (
    echo ERROR: Build failed - no artifact found
    exit /b 1
)

echo Starting application using %ART%...

REM Use JAVA_HOME if set, otherwise try common locations
if defined JAVA_HOME (
    set "JAVA=%JAVA_HOME%\bin\java.exe"
) else if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
    set "JAVA=C:\Program Files\Java\jdk-21\bin\java.exe"
) else if exist "C:\Program Files\Java\jdk-17\bin\java.exe" (
    set "JAVA=C:\Program Files\Java\jdk-17\bin\java.exe"
) else (
    echo ERROR: java not found; set JAVA_HOME
    exit /b 1
)

"%JAVA%" -jar "%ART%"
exit /b 0

:stop
echo Stopping any running instance on port 8081...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8081 ^| findstr LISTENING') do (
    taskkill /PID %%a /F 1>nul 2>nul
)
exit /b 0
