package com.healthmetrics.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for HealthIndicator.
 * Represents a health metric or indicator that facilities report on.
 * Examples of indicators:
 * - Malaria cases reported
 * - BCG vaccination coverage percentage
 * - Number of antenatal care visits
 * This DTO is used when listing indicators for data entry forms
 * or when displaying indicator metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthIndicatorDTO {

    /// Unique identifier for the indicator.
    private Long id;

    /// Unique indicator code. Example: "MAL_001", "VAC_BCG", "ANC_VISITS"
    private String code;

    /// Human-readable name. Example: "Malaria Cases Reported"
    private String name;

    /// Detailed description of what this indicator measures.
    /// Example: "Total number of confirmed malaria cases during the reporting period"
    private String description;

    /// Category for grouping related indicators.
    /// Examples: "Maternal Health", "Child Health", "Disease Control", "Immunization"
    private String category;

    /// Expected data type for this indicator.
    /// Valid values: "NUMBER" (150), "PERCENTAGE" (85.5), "BOOLEAN" (true/false)
    /// This helps with validation and proper display formatting.
    private String dataType;

    /// Unit of measurement for this indicator.
    /// Examples: "cases", "%", "persons", "visits", "deliveries"
    /// Can be null for boolean indicators.
    private String unit;

    /// Whether this indicator is currently active.
    /// Inactive indicators won't show in data entry but historical data remains accessible.
    private Boolean active;

    /// When this indicator was created.
    private LocalDateTime createdAt;

    /// When this indicator was last updated.
    private LocalDateTime updatedAt;
}