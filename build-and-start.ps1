# Build and start the QA Automation Platform
# Usage: .\build-and-start.ps1

Write-Host "`n=========================================`n  QA Automation Platform - BUILD & START`n=========================================`n" -ForegroundColor Cyan

$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$MAVEN_HOME = "C:\Program Files\maven-mvnd-1.0.2-windows-amd64"

# Check Java
if (!(Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    Write-Host "ERROR: Java 21 not found" -ForegroundColor Red
    exit 1
}

# Check Maven
if (!(Test-Path "$MAVEN_HOME\bin\mvnd.cmd")) {
    Write-Host "ERROR: Maven not found" -ForegroundColor Red
    exit 1
}

# Kill existing process
Write-Host "Stopping any running instance..." -ForegroundColor Yellow
$netstatOutput = netstat -aon | Select-String ":8081.*LISTENING"

if ($netstatOutput) {
    $pid = $netstatOutput -split '\s+' | Select-Object -Last 1
    Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
}

# Build
Write-Host "`nBuilding application..." -ForegroundColor Cyan
& "$MAVEN_HOME\bin\mvnd.cmd" clean package -DskipTests -q

if (!(Test-Path "target\qa-automation-platform-1.0.0-SNAPSHOT.jar")) {
    Write-Host "ERROR: Build failed" -ForegroundColor Red
    exit 1
}

Write-Host "`n✓ Build successful`n" -ForegroundColor Green
Write-Host "Starting application...`n" -ForegroundColor Cyan
Write-Host "Server will be available at: http://localhost:8081/api/v1`n" -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop`n" -ForegroundColor Yellow

& "$env:JAVA_HOME\bin\java.exe" -jar "target\qa-automation-platform-1.0.0-SNAPSHOT.jar"
