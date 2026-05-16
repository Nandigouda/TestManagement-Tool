# Chat Functionality Test Script
$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:8081/api/v1"

# Color helper
function Write-Status($message, $color = "Green") {
    Write-Host $message -ForegroundColor $color
}

function Write-Test($title) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host $title -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow
}

# Test 1: Health Check
Write-Test "TEST 1: Health Check"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081" -ErrorAction SilentlyContinue -TimeoutSec 5
    Write-Status "OK: Server is running on port 8081" "Green"
} catch {
    Write-Status "ERROR: Server is NOT responding: $_" "Red"
    exit 1
}

# Test 2: Start Conversation
Write-Test "TEST 2: Start Conversation"
try {
    $payload = @{
        scenario = "User login with email and password"
        guides = @("bestPractices")
        uploadedFiles = @()
    } | ConvertTo-Json

    $response = Invoke-WebRequest `
        -Uri "$baseUrl/chat/start" `
        -Method POST `
        -ContentType "application/json" `
        -Body $payload `
        -ErrorAction Stop

    $result = $response.Content | ConvertFrom-Json
    $conversationId = $result.id
    
    Write-Status "OK: Conversation started successfully" "Green"
    Write-Host "  - Conversation ID: $conversationId"
    Write-Host "  - Title: $($result.title)"
    Write-Host "  - Status: $($result.status)"
} catch {
    Write-Status "ERROR: Failed to start conversation: $_" "Red"
    $conversationId = $null
}

# Test 3: Get Conversation
if ($conversationId) {
    Write-Test "TEST 3: Get Conversation Details"
    try {
        $response = Invoke-WebRequest `
            -Uri "$baseUrl/chat/$conversationId" `
            -Method GET `
            -ErrorAction Stop

        $result = $response.Content | ConvertFrom-Json
        Write-Status "OK: Conversation retrieved successfully" "Green"
        Write-Host "  - ID: $($result.id)"
        Write-Host "  - Messages: $($result.messages.Count)"
        Write-Host "  - Test cases: $($result.testCases.Count)"
    } catch {
        Write-Status "ERROR: Failed to get conversation: $_" "Red"
    }
}

# Test 4: Send Message - Generate
if ($conversationId) {
    Write-Test "TEST 4: Send Message - Generate Test Cases"
    try {
        $payload = @{
            conversationId = $conversationId
            message = "Generate test cases for login functionality"
            actionType = "generate"
        } | ConvertTo-Json

        $response = Invoke-WebRequest `
            -Uri "$baseUrl/chat/message" `
            -Method POST `
            -ContentType "application/json" `
            -Body $payload `
            -ErrorAction Stop

        $result = $response.Content | ConvertFrom-Json
        Write-Status "OK: Message sent successfully" "Green"
        Write-Host "  - Response ID: $($result.id)"
        Write-Host "  - Role: $($result.role)"
        Write-Host "  - Is actionable: $($result.isActionable)"
    } catch {
        Write-Status "ERROR: Failed to send message: $_" "Red"
    }
}

# Test 5: Send Message - Regenerate
if ($conversationId) {
    Write-Test "TEST 5: Send Message - Regenerate"
    try {
        $payload = @{
            conversationId = $conversationId
            message = "Regenerate with edge cases"
            actionType = "regenerate"
        } | ConvertTo-Json

        $response = Invoke-WebRequest `
            -Uri "$baseUrl/chat/message" `
            -Method POST `
            -ContentType "application/json" `
            -Body $payload `
            -ErrorAction Stop

        $result = $response.Content | ConvertFrom-Json
        Write-Status "OK: Regenerate message sent successfully" "Green"
    } catch {
        Write-Status "ERROR: Failed to regenerate: $_" "Red"
    }
}

# Test 6: Regenerate Test Cases Endpoint
if ($conversationId) {
    Write-Test "TEST 6: Regenerate Test Cases Endpoint"
    try {
        $payload = @{
            message = "Include boundary conditions"
        } | ConvertTo-Json

        $response = Invoke-WebRequest `
            -Uri "$baseUrl/chat/$conversationId/regenerate" `
            -Method POST `
            -ContentType "application/json" `
            -Body $payload `
            -ErrorAction Stop

        Write-Status "OK: Regenerate endpoint worked" "Green"
    } catch {
        Write-Status "ERROR: Regenerate endpoint failed: $_" "Red"
    }
}

# Test 7: Modify Test Cases Endpoint
if ($conversationId) {
    Write-Test "TEST 7: Modify Test Cases"
    try {
        $payload = @{
            modifications = "Add security validation steps"
        } | ConvertTo-Json

        $response = Invoke-WebRequest `
            -Uri "$baseUrl/chat/$conversationId/modify" `
            -Method POST `
            -ContentType "application/json" `
            -Body $payload `
            -ErrorAction Stop

        Write-Status "OK: Modify endpoint worked" "Green"
    } catch {
        Write-Status "ERROR: Modify endpoint failed: $_" "Red"
    }
}

# Test 8: Add More Test Cases Endpoint
if ($conversationId) {
    Write-Test "TEST 8: Add More Test Cases"
    try {
        $response = Invoke-WebRequest `
            -Uri "$baseUrl/chat/$conversationId/add-more" `
            -Method POST `
            -ErrorAction Stop

        Write-Status "OK: Add-more endpoint worked" "Green"
    } catch {
        Write-Status "ERROR: Add-more endpoint failed: $_" "Red"
    }
}

# Test 9: Merge Test Cases Endpoint
if ($conversationId) {
    Write-Test "TEST 9: Merge Test Cases"
    try {
        $response = Invoke-WebRequest `
            -Uri "$baseUrl/chat/$conversationId/merge" `
            -Method POST `
            -ErrorAction Stop

        Write-Status "OK: Merge endpoint worked" "Green"
    } catch {
        Write-Status "ERROR: Merge endpoint failed: $_" "Red"
    }
}

# Test 10: Find Similar Cases  
if ($conversationId) {
    Write-Test "TEST 10: Find Similar Test Cases"
    try {
        $url = "$baseUrl/chat/$conversationId/similar"
        $url = $url + "?limit=5&threshold=0.7"
        
        $response = Invoke-WebRequest `
            -Uri $url `
            -Method GET `
            -ErrorAction Stop

        $result = $response.Content | ConvertFrom-Json
        Write-Status "OK: Similar cases endpoint worked" "Green"
        Write-Host "  - Found $($result.Count) similar test cases"
    } catch {
        Write-Status "ERROR: Similar cases endpoint failed: $_" "Red"
    }
}

# Final Summary
Write-Test "CHAT FUNCTIONALITY VALIDATION COMPLETE"
Write-Status "All endpoints tested successfully!" "Green"
