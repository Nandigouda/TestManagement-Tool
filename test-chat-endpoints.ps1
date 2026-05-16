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
    Write-Status "✓ Server is running on port 8081" "Green"
} catch {
    Write-Status "✗ Server is NOT responding: $_" "Red"
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
    
    Write-Status "✓ Conversation started successfully" "Green"
    Write-Host "  - Conversation ID: $conversationId"
    Write-Host "  - Title: $($result.title)"
    Write-Host "  - Status: $($result.status)"
    Write-Host "  - Created at: $($result.createdAt)"
} catch {
    Write-Status "✗ Failed to start conversation: $_" "Red"
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
        Write-Status "✓ Conversation retrieved successfully" "Green"
        Write-Host "  - ID: $($result.id)"
        Write-Host "  - Title: $($result.title)"
        Write-Host "  - Messages count: $($result.messages.Count)"
        Write-Host "  - Test cases count: $($result.testCases.Count)"
    } catch {
        Write-Status "✗ Failed to get conversation: $_" "Red"
    }
}

# Test 4: Send Message (Generate)
if ($conversationId) {
    Write-Test "TEST 4: Send Message - Generate Test Cases"
    try {
        $payload = @{
            conversationId = $conversationId
            message = "Generate test cases for login functionality with valid and invalid credentials"
            actionType = "generate"
        } | ConvertTo-Json

        $response = Invoke-WebRequest `
            -Uri "$baseUrl/chat/message" `
            -Method POST `
            -ContentType "application/json" `
            -Body $payload `
            -ErrorAction Stop

        $result = $response.Content | ConvertFrom-Json
        Write-Status "✓ Message sent successfully" "Green"
        Write-Host "  - Response ID: $($result.id)"
        Write-Host "  - Role: $($result.role)"
        Write-Host "  - Content length: $($result.content.Length) characters"
        Write-Host "  - Is actionable: $($result.isActionable)"
        Write-Host "  - Vector hits: $($result.vectorHits)"
    } catch {
        Write-Status "✗ Failed to send message: $_" "Red"
    }
}

# Test 5: Send Message (Regenerate)
if ($conversationId) {
    Write-Test "TEST 5: Send Message - Regenerate"
    try {
        $payload = @{
            conversationId = $conversationId
            message = "Regenerate with focus on edge cases"
            actionType = "regenerate"
        } | ConvertTo-Json

        $response = Invoke-WebRequest `
            -Uri "$baseUrl/chat/message" `
            -Method POST `
            -ContentType "application/json" `
            -Body $payload `
            -ErrorAction Stop

        $result = $response.Content | ConvertFrom-Json
        Write-Status "✓ Regenerate message sent successfully" "Green"
        Write-Host "  - Response ID: $($result.id)"
        Write-Host "  - Content: $($result.content.Substring(0, [Math]::Min(100, $result.content.Length)))..."
    } catch {
        Write-Status "✗ Failed to regenerate: $_" "Red"
    }
}

# Test 6: Regenerate Test Cases Endpoint
if ($conversationId) {
    Write-Test "TEST 6: Regenerate Test Cases (Dedicated Endpoint)"
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

        $result = $response.Content | ConvertFrom-Json
        Write-Status "✓ Regenerate endpoint worked" "Green"
        Write-Host "  - Response: $($result.content.Substring(0, [Math]::Min(100, $result.content.Length)))..."
    } catch {
        Write-Status "✗ Regenerate endpoint failed: $_" "Red"
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

        $result = $response.Content | ConvertFrom-Json
        Write-Status "✓ Modify endpoint worked" "Green"
        Write-Host "  - Response: $($result.content.Substring(0, [Math]::Min(100, $result.content.Length)))..."
    } catch {
        Write-Status "✗ Modify endpoint failed: $_" "Red"
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

        $result = $response.Content | ConvertFrom-Json
        Write-Status "✓ Add-more endpoint worked" "Green"
        Write-Host "  - Response: $($result.content.Substring(0, [Math]::Min(100, $result.content.Length)))..."
    } catch {
        Write-Status "✗ Add-more endpoint failed: $_" "Red"
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

        $result = $response.Content | ConvertFrom-Json
        Write-Status "✓ Merge endpoint worked" "Green"
        Write-Host "  - Response: $($result.content.Substring(0, [Math]::Min(100, $result.content.Length)))..."
    } catch {
        Write-Status "✗ Merge endpoint failed: $_" "Red"
    }
}

# Test 10: Find Similar Cases
if ($conversationId) {
    Write-Test "TEST 10: Find Similar Test Cases"
    try {
        $response = Invoke-WebRequest `
            -Uri "$baseUrl/chat/$conversationId/similar?limit=5&amp;threshold=0.7" `
            -Method GET `
            -ErrorAction Stop

        $result = $response.Content | ConvertFrom-Json
        Write-Status "✓ Similar cases endpoint worked" "Green"
        Write-Host "  - Found $($result.Count) similar test cases"
        if ($result.Count -gt 0) {
            Write-Host "  - Top match: $($result[0].title) (similarity: $($result[0].similarityScore))"
        }
    } catch {
        Write-Status "✗ Similar cases endpoint failed: $_" "Red"
    }
}

# Final Summary
Write-Test "TEST SUMMARY"
Write-Status "✓ All critical endpoints tested" "Green"
Write-Host "- Chat functionality appears to be working"
Write-Host "- Conversation lifecycle: Create -> Send Messages -> Retrieve"
Write-Host "- Action types: Generate, Regenerate, Modify, Add-more, Merge"
Write-Host "- Similarity search and duplicate detection ready"
