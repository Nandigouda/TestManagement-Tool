package com.qaautomation.controllers;

import com.qaautomation.models.TestCase;
import com.qaautomation.models.TestCaseGenerationRequest;
import com.qaautomation.models.TestCaseGenerationResponse;
import com.qaautomation.services.TestCaseGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for test case generation endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/agents/testcases")
@CrossOrigin(origins = "*")
public class TestCaseGenerationController {

    private final TestCaseGenerationService testCaseGenerationService;

    public TestCaseGenerationController(TestCaseGenerationService testCaseGenerationService) {
        this.testCaseGenerationService = testCaseGenerationService;
    }

    /**
     * POST /agents/testcases/generate - Generates test cases from requirement text.
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateTestCases(@RequestBody TestCaseGenerationRequest request) {
        String requestId = UUID.randomUUID().toString();

        try {
            log.info("Received test case generation request: {}", requestId);

            if (request.getText() == null || request.getText().isEmpty()) {
                if (request.getFileId() == null || request.getFileId().isEmpty()) {
                    return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Either 'text' or 'fileId' must be provided"));
                }
                // TODO: Fetch text from file service using fileId
            }

            List<TestCase> testCases = testCaseGenerationService.generateTestCases(request);

            TestCaseGenerationResponse response = TestCaseGenerationResponse.builder()
                .requestId(requestId)
                .status("SUCCESS")
                .testCases(testCases)
                .totalGenerated(testCases.size())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();

            log.info("Successfully generated {} test cases for request {}", testCases.size(), requestId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request {}: {}", requestId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error generating test cases for request {}: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to generate test cases: " + e.getMessage()));
        }
    }

    /**
     * POST /agents/testcases - Also supports generation for backwards compatibility.
     */
    @PostMapping
    public ResponseEntity<?> generateTestCasesAlt(@RequestBody TestCaseGenerationRequest request) {
        return generateTestCases(request);
    }

    /**
     * GET /agents/testcases/{testCaseId} - Retrieves a test case.
     */
    @GetMapping("/{testCaseId}")
    public ResponseEntity<?> getTestCase(@PathVariable String testCaseId) {
        try {
            TestCase testCase = testCaseGenerationService.getTestCaseById(testCaseId);

            if (testCase == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(testCase);

        } catch (Exception e) {
            log.error("Error retrieving test case {}: {}", testCaseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to retrieve test case"));
        }
    }

    /**
     * GET /agents/testcases - Retrieves all test cases.
     */
    @GetMapping
    public ResponseEntity<?> getAllTestCases() {
        try {
            List<TestCase> testCases = testCaseGenerationService.getAllTestCases();
            return ResponseEntity.ok(testCases);
        } catch (Exception e) {
            log.error("Error retrieving test cases: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to retrieve test cases"));
        }
    }

    /**
     * Error response DTO.
     */
    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
