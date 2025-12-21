package com.qaautomation.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for API requests to generate automation code.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeGenerationRequest {
    private String scenarioText;
    private CodeArtifact.Framework framework;
    private CodeArtifact.Language language;
    private String outputType; // "skeleton" or "runnable"
    private CodeGenerationOptions options;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CodeGenerationOptions {
        private Boolean usePageObjectModel;
        private Boolean includeSetupTeardown;
        private Boolean includeSampleAssertions;
        private Boolean wrapIntoProject;
    }
}
