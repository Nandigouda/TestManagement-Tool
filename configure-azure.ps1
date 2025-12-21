#!/usr/bin/env powershell
# Azure OpenAI Setup Script
# Run this to configure Azure credentials and start the application

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  QA Automation Platform" -ForegroundColor Cyan
Write-Host "  Azure OpenAI Configuration" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Prompt for credentials
Write-Host "Enter your Azure OpenAI credentials:" -ForegroundColor Yellow
Write-Host ""

$apiKey = Read-Host "Azure OpenAI API Key"
$endpoint = Read-Host "Azure OpenAI Endpoint (e.g., https://your-resource.openai.azure.com/)"
$deployment = Read-Host "Deployment Name (e.g., gpt-4)"

# Validate inputs
if ([string]::IsNullOrWhiteSpace($apiKey) -or [string]::IsNullOrWhiteSpace($endpoint) -or [string]::IsNullOrWhiteSpace($deployment)) {
    Write-Host "Error: All fields are required!" -ForegroundColor Red
    exit 1
}

# Set environment variables
Write-Host ""
Write-Host "Setting environment variables..." -ForegroundColor Cyan
$env:AZURE_OPENAI_ENABLED = "true"
$env:AZURE_OPENAI_API_KEY = $apiKey
$env:AZURE_OPENAI_ENDPOINT = $endpoint
$env:AZURE_OPENAI_DEPLOYMENT = $deployment

Write-Host "✓ Environment variables configured" -ForegroundColor Green
Write-Host ""

# Show configuration (masked)
Write-Host "Configuration Summary:" -ForegroundColor Yellow
Write-Host "  AZURE_OPENAI_ENABLED: true"
Write-Host "  AZURE_OPENAI_API_KEY: $($apiKey.Substring(0, [Math]::Min(4, $apiKey.Length)))...***"
Write-Host "  AZURE_OPENAI_ENDPOINT: $endpoint"
Write-Host "  AZURE_OPENAI_DEPLOYMENT: $deployment"
Write-Host ""

# Ask if user wants to start the app
$startApp = Read-Host "Start the application now? (y/n)"
if ($startApp -eq 'y' -or $startApp -eq 'Y') {
    Write-Host ""
    Write-Host "Building and starting application..." -ForegroundColor Green
    Write-Host ""
    
    # Build
    mvn clean package -DskipTests -q
    
    if (Test-Path "target\qa-automation-platform-1.0.0-SNAPSHOT.jar") {
        Write-Host "Build successful. Starting application..." -ForegroundColor Green
        $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
        & "$env:JAVA_HOME\bin\java.exe" -jar "target\qa-automation-platform-1.0.0-SNAPSHOT.jar"
    } else {
        Write-Host "Build failed!" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "Environment variables are set for this session." -ForegroundColor Yellow
    Write-Host "Run: .\start.ps1" -ForegroundColor Cyan
}
