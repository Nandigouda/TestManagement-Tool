package com.qaautomation.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qaautomation.models.Step;
import com.qaautomation.models.TestCase;
import com.qaautomation.models.TestCaseGenerationRequest;
import com.qaautomation.services.LLMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Test Case Generation Agent - Converts requirements text into structured test cases.
 * Responsibilities:
 * - Accept requirement text
 * - Generate multiple test case types (positive, negative, boundary)
 * - Produce structured test cases with steps and expected results
 * - Return test cases in JSON format
 */
@Slf4j
@Component
public class TestCaseGenerationAgent implements Agent {

    private final LLMService llmService;
    private final ObjectMapper objectMapper;

    private static final String PROMPT_TEMPLATE = """
        You are an expert QA engineer and test case designer. Your task is to analyze software requirements 
        and generate comprehensive test cases that follow a strict table format with specific rules.
        
        REQUIREMENT:
        %s
        
        CONTEXT:
        Application: %s
        Module: %s
        Priority Hint: %s
        
        ✔ MANDATORY TABLE RULES (MUST FOLLOW STRICTLY):
        
        1. Each test step must be on a separate row.
           - Do not combine multiple steps into one row
           - Each step is independent
        
        2. The expected result for each step must be on the same row as its step.
           - Expected results MUST be paired with the corresponding step
           - Do not list all expected results at the end
        
        3. If there are preconditions, list them as the first test steps and explicitly include the word "Pre-condition".
           - Format: "Pre-condition: [description]"
           - Preconditions always come first (first step of the test case)
           - Example: "Pre-condition: User should be on login page"
        
        4. Login steps should come after pre-condition.
           - Order: Preconditions → Login Steps → Other Steps
           - If a test case requires login, login must be step 2 (after preconditions)
        
        5. Expected results must use the word "should" consistently.
           - Format: "[Subject] should [action/state]"
           - Example: "User should be displayed with home screen after successful login"
           - Example: "Login button should be enabled"
           - Example: "Error message should appear in red text"
           - Make sure EVERY expected result contains "should"
        
        6. Test case titles should be concise but descriptive.
           - Keep titles under 50 characters if possible
           - Use action-oriented naming: "User Login", "Password Reset", "Add to Cart"
           - Avoid: "Test 1", "Verify", "Check System"
        
        7. Generate as many test cases as logically needed for full coverage.
           - Cover happy path (success scenarios)
           - Cover error paths (validation errors, failures)
           - Cover edge cases (boundary conditions, special inputs)
           - Example: For login, generate: Valid Login, Invalid Email, Wrong Password, Empty Fields, etc.
           - Generate AT LEAST 5-8 test cases for comprehensive coverage
        
        8. Do not include accessibility-related test cases unless explicitly requested.
           - Skip: Screen reader compatibility, keyboard navigation, color contrast
           - Unless user specifically asks for accessibility testing
        
        9. When exporting to Excel:
           - Each step remains on its own row (multiple rows per test case)
           - The export format will have 7 columns:
             1. Test Case Title (only on first row of each test case)
             2. Test Steps (the step text)
             3. Expected Result (paired with step)
             4. Labels (empty)
             5. Automation State (always "Not Automated")
             6. Test Case Status (empty)
             7. Created in Sprint (empty)
        
        INSTRUCTIONS:
        1. Generate test cases covering positive, negative, and boundary scenarios
        2. For each test case, provide:
           - A clear, descriptive title with 'Verify' as prifix (concise, under 50 chars)
           - Preconditions as the FIRST step with "Pre-condition: " prefix
           - Login steps AFTER preconditions (if applicable)
           - Step-by-step actions with expected results
           - EACH step must have an expected result containing "should"
           - Priority level (HIGH, MEDIUM, LOW)
           - Relevant tags
        3. Return ONLY a valid JSON array matching this exact schema:
        [
          {
            "title": "Test Case Title",
            "description": "Brief description",
            "preconditions": "User should be on login page",
            "steps": [
              {"stepNumber": 1, "action": "Pre-condition: User should be on login page", "testData": "", "expectedResult": "Login page should be displayed"},
              {"stepNumber": 2, "action": "Enter valid email and password", "testData": "email: test@example.com, password: Test123", "expectedResult": "Form should accept input"},
              {"stepNumber": 3, "action": "Click login button", "testData": "", "expectedResult": "User should be redirected to home screen"}
            ],
            "expectedResults": ["User should be logged in successfully"],
            "priority": "HIGH|MEDIUM|LOW",
            "tags": ["login", "authentication"],
            "estimatedComplexity": 0.5
          }
        ]
        
        CRITICAL REQUIREMENTS:
        - Generate 5-8 test cases for comprehensive coverage
        - Each step MUST have expectedResult with "should" in it
        - Order steps: Preconditions → Login → Other steps
        - Test case titles must be concise and descriptive
        - Return ONLY the JSON array, no additional text.
        """;

    public TestCaseGenerationAgent(LLMService llmService, ObjectMapper objectMapper) {
        this.llmService = llmService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getDescription() {
        return "Generates structured test cases from requirement text using AI";
    }

    @Override
    public boolean canHandle(Object input) {
        return input instanceof TestCaseGenerationRequest;
    }

    @Override
    public boolean execute() {
        // Implementation handled by service layer
        return true;
    }

    /**
     * Generates test cases from requirement text.
     */
    public List<TestCase> generateTestCases(TestCaseGenerationRequest request, int maxRetries) throws Exception {
        String text = request.getText();
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Requirement text cannot be empty");
        }

        String appName = request.getContext() != null ? request.getContext().getAppName() : "Unknown";
        String module = request.getContext() != null ? request.getContext().getModule() : "General";
        String priorityHint = request.getContext() != null ? request.getContext().getPriorityHint() : "MEDIUM";

        String prompt = String.format(PROMPT_TEMPLATE, text, appName, module, priorityHint);

        String response = null;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Generating test cases - Attempt {}/{}", attempt, maxRetries);
                response = llmService.callLLM(prompt, 0.3f, 2000);
                response = extractJsonFromResponse(response);
                break;
            } catch (Exception e) {
                log.warn("Attempt {} failed: {}", attempt, e.getMessage());
                lastException = e;
                if (attempt < maxRetries) {
                    Thread.sleep(1000 * attempt); // Exponential backoff
                }
            }
        }

        if (response == null) {
            throw new RuntimeException("Failed to generate test cases after " + maxRetries + " attempts", lastException);
        }

        return parseTestCasesFromJson(response, request.getRequirementId());
    }

    /**
     * Extracts JSON array from LLM response (handles extra text).
     */
    private String extractJsonFromResponse(String response) {
        if (response == null) {
            throw new IllegalArgumentException("Response is null");
        }

        int startIdx = response.indexOf('[');
        int endIdx = response.lastIndexOf(']');

        if (startIdx == -1 || endIdx == -1 || startIdx >= endIdx) {
            throw new IllegalArgumentException("No valid JSON array found in response");
        }

        return response.substring(startIdx, endIdx + 1);
    }

    /**
     * Parses test cases from JSON response.
     */
    private List<TestCase> parseTestCasesFromJson(String jsonString, String requirementId) throws Exception {
        List<TestCase> testCases = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);

            if (!rootNode.isArray()) {
                throw new IllegalArgumentException("Response is not a JSON array");
            }

            for (JsonNode node : rootNode) {
                TestCase testCase = mapJsonToTestCase(node, requirementId);
                testCases.add(testCase);
            }

            log.info("Successfully parsed {} test cases from JSON response", testCases.size());
        } catch (Exception e) {
            log.error("Error parsing test cases from JSON: {}", jsonString, e);
            throw e;
        }

        return testCases;
    }

    /**
     * Maps a JSON node to a TestCase entity.
     */
    private TestCase mapJsonToTestCase(JsonNode node, String requirementId) {
        String title = getStringField(node, "title", "Untitled Test Case");
        String description = getStringField(node, "description", "");
        String preconditions = getStringField(node, "preconditions", "");
        double complexity = getDoubleField(node, "estimatedComplexity", 0.5);

        // Parse steps
        List<Step> steps = new ArrayList<>();
        JsonNode stepsNode = node.get("steps");
        if (stepsNode != null && stepsNode.isArray()) {
            for (int i = 0; i < stepsNode.size(); i++) {
                JsonNode stepNode = stepsNode.get(i);
                Step step = Step.builder()
                    .stepNumber(i + 1)
                    .action(getStringField(stepNode, "action", ""))
                    .testData(getStringField(stepNode, "testData", ""))
                    .expectedResult(getStringField(stepNode, "expectedResult", ""))
                    .build();
                steps.add(step);
            }
        }

        // Parse expected results
        List<String> expectedResults = new ArrayList<>();
        JsonNode resultsNode = node.get("expectedResults");
        if (resultsNode != null && resultsNode.isArray()) {
            for (JsonNode result : resultsNode) {
                expectedResults.add(result.asText());
            }
        }

        // Parse tags
        List<String> tags = new ArrayList<>();
        JsonNode tagsNode = node.get("tags");
        if (tagsNode != null && tagsNode.isArray()) {
            for (JsonNode tag : tagsNode) {
                tags.add(tag.asText());
            }
        }

        // Parse priority
        String priorityStr = getStringField(node, "priority", "MEDIUM");
        TestCase.Priority priority = TestCase.Priority.valueOf(priorityStr.toUpperCase());

        return TestCase.builder()
            .title(title)
            .description(description)
            .preconditions(preconditions)
            .steps(steps)
            .expectedResults(expectedResults)
            .priority(priority)
            .tags(tags)
            .requirementId(requirementId)
            .estimatedComplexity(complexity)
            .build();
    }

    private String getStringField(JsonNode node, String fieldName, String defaultValue) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asText() : defaultValue;
    }

    private double getDoubleField(JsonNode node, String fieldName, double defaultValue) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asDouble() : defaultValue;
    }
}
