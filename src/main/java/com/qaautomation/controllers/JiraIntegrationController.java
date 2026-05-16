package com.qaautomation.controllers;

import com.qaautomation.agents.JiraIntegrationAgent;
import com.qaautomation.models.TestCase;
import com.qaautomation.services.JiraIntegrationService;
import com.qaautomation.services.TestCaseGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Jira integration endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/integrations/jira")
@CrossOrigin(origins = "*")
public class JiraIntegrationController {

    private final JiraIntegrationService jiraIntegrationService;
    private final TestCaseGenerationService testCaseGenerationService;

    public JiraIntegrationController(
        JiraIntegrationService jiraIntegrationService,
        TestCaseGenerationService testCaseGenerationService) {
        this.jiraIntegrationService = jiraIntegrationService;
        this.testCaseGenerationService = testCaseGenerationService;
    }

    /**
     * POST /integrations/jira/push - Pushes test cases to Jira.
     */
    @PostMapping("/push")
    public ResponseEntity<?> pushToJira(@RequestBody PushRequest request) {
        try {
            log.info("Received Jira push request for {} test cases", request.testCaseIds != null ? request.testCaseIds.size() : 0);

            if (request.testCaseIds == null || request.testCaseIds.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Test case IDs cannot be empty"));
            }

            if (request.jiraProjectKey == null || request.jiraProjectKey.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Jira project key is required"));
            }

            // Fetch test cases
            List<TestCase> testCases = request.testCaseIds.stream()
                .map(testCaseGenerationService::getTestCaseById)
                .filter(tc -> tc != null)
                .toList();

            if (testCases.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("No valid test cases found"));
            }

            // Push to Jira
            List<JiraIntegrationAgent.JiraPushResult> results =
                jiraIntegrationService.pushTestCasesToJira(testCases, request.jiraProjectKey);

            return ResponseEntity.ok(new PushResponse(results));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid push request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error pushing to Jira: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to push to Jira: " + e.getMessage()));
        }
    }

    /**
     * Request DTO for Jira push.
     */
    public static class PushRequest {
        public List<String> testCaseIds;
        public String jiraProjectKey;
        public String jiraAuth;

        // Getters and setters
        public List<String> getTestCaseIds() { return testCaseIds; }
        public void setTestCaseIds(List<String> testCaseIds) { this.testCaseIds = testCaseIds; }
        
        public String getJiraProjectKey() { return jiraProjectKey; }
        public void setJiraProjectKey(String jiraProjectKey) { this.jiraProjectKey = jiraProjectKey; }
        
        public String getJiraAuth() { return jiraAuth; }
        public void setJiraAuth(String jiraAuth) { this.jiraAuth = jiraAuth; }
    }

    /**
     * Response DTO for Jira push.
     */
    public static class PushResponse {
        public List<JiraIntegrationAgent.JiraPushResult> results;

        public PushResponse(List<JiraIntegrationAgent.JiraPushResult> results) {
            this.results = results;
        }

        public List<JiraIntegrationAgent.JiraPushResult> getResults() { return results; }
        public void setResults(List<JiraIntegrationAgent.JiraPushResult> results) { this.results = results; }
    }

    /**
     * Error response DTO.
     */
    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
