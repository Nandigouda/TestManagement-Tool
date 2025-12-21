package com.qaautomation.services;

import com.qaautomation.agents.JiraIntegrationAgent;
import com.qaautomation.models.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for Jira integration operations.
 */
@Slf4j
@Service
public class JiraIntegrationService {

    private final JiraIntegrationAgent jiraIntegrationAgent;

    @Value("${external-apis.jira.base-url}")
    private String jiraBaseUrl;

    @Value("${external-apis.jira.api-token}")
    private String jiraApiToken;

    public JiraIntegrationService(JiraIntegrationAgent jiraIntegrationAgent) {
        this.jiraIntegrationAgent = jiraIntegrationAgent;
    }

    /**
     * Pushes test cases to Jira project.
     */
    public List<JiraIntegrationAgent.JiraPushResult> pushTestCasesToJira(
        List<TestCase> testCases,
        String jiraProjectKey) {

        log.info("Pushing {} test cases to Jira project: {}", testCases.size(), jiraProjectKey);

        if (jiraApiToken == null || jiraApiToken.isEmpty()) {
            throw new RuntimeException("Jira API token not configured");
        }

        List<JiraIntegrationAgent.JiraPushResult> results =
            jiraIntegrationAgent.pushTestCasesToJira(testCases, jiraProjectKey, jiraBaseUrl, jiraApiToken, 3);

        long successCount = results.stream().filter(r -> "SUCCESS".equals(r.status)).count();
        log.info("Jira push completed: {} succeeded, {} failed", successCount, results.size() - successCount);

        return results;
    }
}
