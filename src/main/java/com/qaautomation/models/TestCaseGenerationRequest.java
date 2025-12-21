package com.qaautomation.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO for API requests to generate test cases.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseGenerationRequest {
    private String fileId;
    private String text;
    private String requirementId;
    private GenerationContext context;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerationContext {
        private String appName;
        private String module;
        private String priorityHint;
        private Integer expectedTestCases;
    }
}
