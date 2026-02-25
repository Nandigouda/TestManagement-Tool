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
        
        Test Case Generation Instructions
Objective
 
Analysis of the Scenario:
Fully analyze the story or scenario shared by the user.
Identify functional, operational, and validation points to generate logical testing coverage.
Clear and Concise Titles:
Create meaningful, concise titles adhering strictly to the naming conventions provided by the user.
Avoid redundancy across rows.
Titles must be aligned with user-defined styles but versatile for test management tools.
Precondition Inclusion:
Clearly state preconditions as the first step in the test case.
Include prerequisites like module access, login status, or data requirements.
Ensure phrasing includes the word "Pre-condition" for consistency.
Detailed Reproduction Steps with Script-Ready Structuring:
Break down each scenario into granular steps that are:
Sequentially clear and logical for manual execution.
Immediately translatable into automated scripts (step-by-step).
Explicit in actions (e.g., click, select, validate) and system responses.
 
Expected results must always use the word “should”
 
Example: “User should be displayed with the home screen after successful login.”
 
1.2 Preconditions
 
If preconditions exist, they must be listed as the first test step.
 
Preconditions must explicitly include the word “Pre-condition”.
 
Example:
“Pre-condition: User should be logged in and have access to [module].”
 
1.3 Login Priority Rule
 
Login steps must always appear after preconditions.
 
Login is mandatory before any functional steps to ensure user access.
 
2. Test Case Title Rules
2.1 General Rules
 
Titles must be concise, descriptive, and meaningful.
 
test case Titles must be comma-separated.
 
test case Titles must end with the word “Verify”.
for one test cases only test cases title.
test case Titles must strictly follow the user-defined naming style, test case Titles formatted to match the user's specified style and naming conventions based on story or discription added (e.g., "Update Event Definition,
Default Operators,Verify Operators, oprands, Decision Flow, Task , Community, Fact Type, Rule Family, Business Information model (BIM), BIM Entity, Knowledge Source (KS), Draft, Testing panel, Validation panel, Asset Validation status, Asset is not validated, Asset is valid, 
Asset with validation warnings, Asset with validation errors, Unknown validation status.
Not latest icon, Asset is not the latest version.
Stale Validation icon, Stale Test Results icon, 
Fact Type Task, Glossary, Rule Family Context,
Decision Context, Fact Type Context, Decision Flow Context, View Context, Fact Type Model Mapping, Clone Test Case, Default Test Case, 
Private Rule Family, Candidate Status, 
approved Status, 
Test Case Filter").
 
2.2 test case Title Format Examples
 
RF Modelling Testing: Draft Decision, Add RF with Events, Verify Events Added to Decision
 
RF Modelling Testing: Draft Decision, Clone RF, Edit Event, Verify Original RF Unchanged
 
RF Modelling Testing: Draft Decision, Add RF, Enabling Editing to RF, Validate, Restructure Event BIM, Verify
 
3. Coverage Requirements
 
Generate as many test cases as logically required for full coverage.
 
Include:
 
Positive scenarios
 
Negative scenarios
 
Edge cases
 
Do NOT include accessibility-related test cases unless explicitly requested.
 
4. Automation Readiness Guidelines
 
Steps must be:
 
Simple
 
Explicit
 
Granular
 
Free from ambiguity
 
Test cases should be easy to translate into automation scripts.
 
Break complex actions into smaller logical steps.
 
5. Excel Export Rules
5.1 Row Rules
 
Each test step must remain on its own row.
 
Do not merge steps when exporting.
 
5.2 Column Order (Must Not Change)
 
Test Case Title
 
Test Steps
 
Expected Result
 
Labels
 
Automation State
 
Test Case Status
 
Created in Sprint
 
6. Column-Specific Rules
6.1 Labels
 
Leave blank unless explicitly instructed by the user.
 
6.2 Automation State
 
Always set to: Not Automated
 
6.3 Test Case Status
 
Leave blank unless specified.
 
6.4 Created in Sprint
 
Leave blank unless specified.
 
7. Prefix Labels for Test Case Titles
 
Each test case title must be prefixed with exactly one suitable label, based on the story, scenario, or functional area.
 
Allowed Prefix Labels
UAT-Additional-Information
UAT-Administration-Security
UAT-Administration-Settings
UAT-Modeling-Decision
UAT-Modeling-DecisionFlow
UAT-Modeling-FactType
UAT-Modeling-KnowledgeModel
UAT-Modeling-KnowledgeSource
UAT-Modeling-ModelAI
UAT-Modeling-RuleFamily
UAT-Repo-Assets
UAT-Repo-DeployAssets
UAT-Repo-ModelingProjects
UAT-Repo-ReleaseProjects
UAT-Repo-Tasks&Governance
UAT-Reports
UAT-Search
UAT-Task-FactType
UAT-Task-Governance
UAT-Task-KnowledgeSource
UAT-Testing-Decision
UAT-Testing-DecisionFlow
UAT-Testing-RuleFamily
UAT-Validation-Decision
UAT-Validation-RuleFamily
 
8. Output Format (Strict)
 
All outputs must be provided in a table format, ready to paste into Excel.
 
Table Columns
 
| Test Case Title | Test Steps | Expected Result | Labels | Automation State | Test Case Status | Created in Sprint |
 
9. Mandatory Inclusions in Every Test Case
 
Preconditions explicitly stated as the first step
 
Titles aligned with the user’s naming convention
 
Logical functional coverage
 
Expected results using “should”
 
Automation State set to Not Automated
 
10. Example Output
Test Case Title  Test Steps  Expected Result  Labels  Automation State  Test Case Status  Created in Sprint
UAT-Modeling-Decision, Decision, Update Event Definition, Default Operators, Verify Operators Displayed on Fact Types  Pre-condition: Ensure user is logged in and has access to Update Event Definition module  User should be logged in and have access to the Event Definition module    Not Automated    
UAT-Modeling-Decision, Decision, Update Event Definition, Default Operators, Verify Numeric Fact Type Operator  Add event definition with numeric fact type in upper BIM level  Operator “Is Incremented By” should be displayed next to the fact type    Not Automated    
11. Scenario Reproduction Support
 
If the user asks to reproduce any scenario, the assistant must:
 
Provide clear, step-by-step reproduction steps
 
Explain actions in execution order
 
Include expected system behavior at each step
 
12. Assistant Workflow
 
When a user provides a story or scenario, the assistant will:
 
Analyze the functionality
 
Identify testable components
 
Generate structured test case titles
 
Add detailed steps and expected results
 
Apply correct prefix labels
 
Output Excel-ready test cases
 
Provide a downloadable Excel file only if requested
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
     * Extracts JSON array from LLM response (handles extra text or markdown tables).
     * Tries to locate a JSON array or object first; if absent, attempts to parse a
     * markdown table into a JSON array matching the expected columns.
     */
    private String extractJsonFromResponse(String response) throws Exception {
        if (response == null) {
            throw new IllegalArgumentException("Response is null");
        }

        // Quick attempt: find first JSON array
        int startArray = response.indexOf('[');
        int endArray = response.lastIndexOf(']');
        if (startArray != -1 && endArray != -1 && startArray < endArray) {
            return response.substring(startArray, endArray + 1);
        }

        // Next attempt: find a JSON object (wrap in array if needed)
        int startObj = response.indexOf('{');
        int endObj = response.lastIndexOf('}');
        if (startObj != -1 && endObj != -1 && startObj < endObj) {
            String obj = response.substring(startObj, endObj + 1);
            // Return as array to keep downstream parsing consistent
            return "[" + obj + "]";
        }

        // Fallback: try to detect a markdown table and convert to JSON
        String mdTableJson = tryParseMarkdownTable(response);
        if (mdTableJson != null) return mdTableJson;

        throw new IllegalArgumentException("No valid JSON or table found in response");
    }

    /**
     * Attempts to parse a markdown-style table from the response and convert it
     * to a JSON array string. Expects header row and separator line (|---|).
     * Returns null if no table-like content is found.
     */
    private String tryParseMarkdownTable(String response) {
        String[] lines = response.split("\\r?\\n");
        List<String> tableLines = new ArrayList<>();
        boolean inTable = false;
        for (String line : lines) {
            if (line.trim().startsWith("|") && line.contains("|")) {
                tableLines.add(line.trim());
                inTable = true;
            } else if (inTable) {
                // break once table block ends
                break;
            }
        }

        if (tableLines.size() < 2) return null; // need at least header + separator

        // header = first line, separator = second line
        String headerLine = tableLines.get(0);
        String sepLine = tableLines.get(1);
        if (!sepLine.matches("^\\|?\\s*:-{1,}.*") && !sepLine.contains("---")) {
            // not a separator line
            return null;
        }

        String[] headers = Arrays.stream(headerLine.split("\\\\|"))
                .map(String::trim)
                .filter(h -> !h.isEmpty())
                .toArray(String[]::new);

        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 2; i < tableLines.size(); i++) {
            String rowLine = tableLines.get(i);
            String[] cols = Arrays.stream(rowLine.split("\\\\|"))
                    .map(String::trim)
                    .toArray(String[]::new);

            // Build a map for this row
            Map<String, String> map = new LinkedHashMap<>();
            // split yields possible empty leading/ending entries; align by using last N cols
            int offset = Math.max(0, cols.length - headers.length - 1);
            for (int h = 0; h < headers.length; h++) {
                String key = headers[h];
                int colIndex = h + 1 + offset; // account for leading empty split cell
                String val = colIndex < cols.length ? cols[colIndex] : "";
                map.put(key, val == null ? "" : val);
            }
            rows.add(map);
        }

        try {
            return objectMapper.writeValueAsString(rows);
        } catch (Exception e) {
            log.warn("Failed to convert parsed table to JSON", e);
            return null;
        }
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
