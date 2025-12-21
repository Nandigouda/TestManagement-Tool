package com.qaautomation.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * Data model representing a single test step within a test case.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Step {
    private int stepNumber;
    private String action;
    private String testData;
    private String expectedResult;
}
