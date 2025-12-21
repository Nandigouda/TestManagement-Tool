package com.qaautomation.controllers;

import com.qaautomation.models.TestCase;
import com.qaautomation.services.TestCaseGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * REST Controller for viewing and filtering test cases.
 * Provides endpoints to retrieve test cases with various filtering options.
 */
@Slf4j
@RestController
@RequestMapping("/testcases")
@CrossOrigin(origins = "*")
public class TestCaseViewerController {

    private final TestCaseGenerationService testCaseGenerationService;

    public TestCaseViewerController(TestCaseGenerationService testCaseGenerationService) {
        this.testCaseGenerationService = testCaseGenerationService;
    }

    /**
     * GET /testcases - Retrieves all test cases.
     */
    @GetMapping
    public ResponseEntity<?> getAllTestCases() {
        try {
            List<TestCase> testCases = testCaseGenerationService.getAllTestCases();
            log.info("Retrieved {} test cases", testCases.size());
            return ResponseEntity.ok(new TestCaseListResponse(testCases));
        } catch (Exception e) {
            log.error("Error retrieving test cases: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new ErrorResponse("Failed to retrieve test cases: " + e.getMessage()));
        }
    }

    /**
     * GET /testcases/filter?appName=&module=&tag=&priority=
     * Retrieves test cases with filtering options.
     */
    @GetMapping("/filter")
    public ResponseEntity<?> filterTestCases(
            @RequestParam(required = false) String appName,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String priority) {
        try {
            List<TestCase> testCases = testCaseGenerationService.getAllTestCases();

            // Apply filters
            if (appName != null && !appName.isEmpty()) {
                testCases = testCases.stream()
                    .filter(tc -> tc.getAppName() != null && tc.getAppName().equalsIgnoreCase(appName))
                    .collect(Collectors.toList());
            }

            if (module != null && !module.isEmpty()) {
                testCases = testCases.stream()
                    .filter(tc -> tc.getModule() != null && tc.getModule().equalsIgnoreCase(module))
                    .collect(Collectors.toList());
            }

            if (tag != null && !tag.isEmpty()) {
                testCases = testCases.stream()
                    .filter(tc -> tc.getTags() != null && tc.getTags().contains(tag))
                    .collect(Collectors.toList());
            }

            if (priority != null && !priority.isEmpty()) {
                testCases = testCases.stream()
                    .filter(tc -> tc.getPriority() != null && tc.getPriority().toString().equalsIgnoreCase(priority))
                    .collect(Collectors.toList());
            }

            log.info("Retrieved {} filtered test cases", testCases.size());
            return ResponseEntity.ok(new TestCaseListResponse(testCases));
        } catch (Exception e) {
            log.error("Error filtering test cases: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new ErrorResponse("Failed to filter test cases: " + e.getMessage()));
        }
    }

    /**
     * GET /testcases/by-application?name=AppName
     * Retrieves test cases for a specific application.
     */
    @GetMapping("/by-application")
    public ResponseEntity<?> getByApplication(@RequestParam String name) {
        try {
            List<TestCase> testCases = testCaseGenerationService.getTestCasesByApplication(name);
            log.info("Retrieved {} test cases for application: {}", testCases.size(), name);
            return ResponseEntity.ok(new TestCaseListResponse(testCases));
        } catch (Exception e) {
            log.error("Error retrieving test cases by application: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new ErrorResponse("Failed to retrieve test cases: " + e.getMessage()));
        }
    }

    /**
     * GET /testcases/by-module?name=ModuleName
     * Retrieves test cases for a specific module.
     */
    @GetMapping("/by-module")
    public ResponseEntity<?> getByModule(@RequestParam String name) {
        try {
            List<TestCase> testCases = testCaseGenerationService.getTestCasesByModule(name);
            log.info("Retrieved {} test cases for module: {}", testCases.size(), name);
            return ResponseEntity.ok(new TestCaseListResponse(testCases));
        } catch (Exception e) {
            log.error("Error retrieving test cases by module: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new ErrorResponse("Failed to retrieve test cases: " + e.getMessage()));
        }
    }

    /**
     * GET /testcases/metrics - Get summary metrics about stored test cases.
     */
    @GetMapping("/metrics")
    public ResponseEntity<?> getMetrics() {
        try {
            List<TestCase> allTestCases = testCaseGenerationService.getAllTestCases();

            // Calculate metrics
            Set<String> uniqueApps = allTestCases.stream()
                .map(TestCase::getAppName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            Set<String> uniqueModules = allTestCases.stream()
                .map(TestCase::getModule)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            Set<String> allTags = allTestCases.stream()
                .flatMap(tc -> tc.getTags() != null ? tc.getTags().stream() : Stream.empty())
                .collect(Collectors.toSet());

            Map<String, Long> priorityDistribution = allTestCases.stream()
                .collect(Collectors.groupingBy(
                    tc -> tc.getPriority() != null ? tc.getPriority().toString() : "UNKNOWN",
                    Collectors.counting()
                ));

            MetricsResponse metrics = MetricsResponse.builder()
                .totalTestCases(allTestCases.size())
                .totalApplications(uniqueApps.size())
                .totalModules(uniqueModules.size())
                .totalTags(allTags.size())
                .applications(new ArrayList<>(uniqueApps))
                .modules(new ArrayList<>(uniqueModules))
                .tags(new ArrayList<>(allTags))
                .priorityDistribution(priorityDistribution)
                .build();

            log.info("Generated metrics: {} test cases, {} apps, {} modules", 
                metrics.totalTestCases, metrics.totalApplications, metrics.totalModules);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error generating metrics: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new ErrorResponse("Failed to generate metrics: " + e.getMessage()));
        }
    }

    /**
     * GET /testcases/{id} - Retrieve a single test case.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTestCaseById(@PathVariable String id) {
        try {
            TestCase testCase = testCaseGenerationService.getTestCaseById(id);
            if (testCase == null) {
                return ResponseEntity.notFound().build();
            }
            log.info("Retrieved test case: {}", id);
            return ResponseEntity.ok(testCase);
        } catch (Exception e) {
            log.error("Error retrieving test case: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(new ErrorResponse("Failed to retrieve test case: " + e.getMessage()));
        }
    }

    // Response DTOs

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class TestCaseListResponse {
        public List<TestCase> testCases;
        public int count;
        public long timestamp;

        public TestCaseListResponse(List<TestCase> testCases) {
            this.testCases = testCases;
            this.count = testCases.size();
            this.timestamp = System.currentTimeMillis();
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class MetricsResponse {
        public int totalTestCases;
        public int totalApplications;
        public int totalModules;
        public int totalTags;
        public List<String> applications;
        public List<String> modules;
        public List<String> tags;
        public Map<String, Long> priorityDistribution;
    }

    public static class ErrorResponse {
        public String message;
        public long timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
