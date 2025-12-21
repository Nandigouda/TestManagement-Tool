package com.qaautomation.services;

import com.qaautomation.agents.TestCaseGenerationAgent;
import com.qaautomation.models.TestCase;
import com.qaautomation.models.TestCaseGenerationRequest;
import com.qaautomation.repositories.TestCaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for test case generation operations.
 */
@Slf4j
@Service
public class TestCaseGenerationService {

    private final TestCaseRepository testCaseRepository;
    private final TestCaseGenerationAgent testCaseGenerationAgent;

    public TestCaseGenerationService(
        TestCaseRepository testCaseRepository,
        TestCaseGenerationAgent testCaseGenerationAgent) {
        this.testCaseRepository = testCaseRepository;
        this.testCaseGenerationAgent = testCaseGenerationAgent;
    }

    /**
     * Generates test cases from requirement text.
     */
    public List<TestCase> generateTestCases(TestCaseGenerationRequest request) throws Exception {
        log.info("Generating test cases from requirement: {}", request.getRequirementId());

        List<TestCase> testCases = testCaseGenerationAgent.generateTestCases(request, 3);

        // Set appName and module from request before saving
        String appName = request.getContext() != null ? request.getContext().getAppName() : "Unknown";
        String module = request.getContext() != null ? request.getContext().getModule() : "General";
        
        testCases.forEach(tc -> {
            tc.setAppName(appName);
            tc.setModule(module);
        });

        // Save generated test cases
        testCases = testCaseRepository.saveAll(testCases);

        log.info("Generated {} test cases for app: {}, module: {}", testCases.size(), appName, module);
        return testCases;
    }

    /**
     * Retrieves test cases by requirement ID.
     */
    public List<TestCase> getTestCasesByRequirement(String requirementId) {
        return testCaseRepository.findByRequirementId(requirementId);
    }

    /**
     * Retrieves test cases by application name.
     */
    public List<TestCase> getTestCasesByApplication(String appName) {
        return testCaseRepository.findByAppName(appName);
    }

    /**
     * Retrieves test cases by module.
     */
    public List<TestCase> getTestCasesByModule(String module) {
        return testCaseRepository.findByModule(module);
    }

    /**
     * Retrieves test cases by both application and module.
     */
    public List<TestCase> getTestCasesByApplicationAndModule(String appName, String module) {
        return testCaseRepository.findByAppNameAndModule(appName, module);
    }

    /**
     * Retrieves test cases by tag/label.
     */
    public List<TestCase> getTestCasesByTag(String tag) {
        return testCaseRepository.findByTag(tag);
    }

    /**
     * Retrieves all test cases.
     */
    public List<TestCase> getAllTestCases() {
        return testCaseRepository.findAll();
    }

    /**
     * Retrieves test case by ID.
     */
    public TestCase getTestCaseById(String testCaseId) {
        return testCaseRepository.findById(testCaseId).orElse(null);
    }
}
