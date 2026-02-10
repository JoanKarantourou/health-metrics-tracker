package com.healthmetrics.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for DataValue.
 * Represents a complete health data value with embedded facility and indicator information.
 * Example: A DataValue showing that "Athens General Hospital" reported
 * "150 malaria cases" for "January 2024".
 * Note: Unlike the entity which uses lazy-loaded relationships,
 * this DTO includes nested FacilityDTO and HealthIndicatorDTO objects.
 * This provides all necessary information in a single response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataValueDTO {

    /// Unique identifier for this data value.
    private Long id;

    /// The facility that reported this data.
    /// Includes complete facility information (not just the ID).
    /// This allows the frontend to display facility details without additional API calls.
    private FacilityDTO facility;

    /// The health indicator this value represents.
    /// Includes complete indicator information (name, unit, category, etc.).
    private HealthIndicatorDTO indicator;

    /// Start date of the reporting period. Example: 2024-01-01 for monthly data
    private LocalDate periodStart;

    /// End date of the reporting period. Example: 2024-01-31 for monthly data
    private LocalDate periodEnd;

    /// Type of reporting period.
    /// Valid values: "DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY"
    private String periodType;

    /// The actual reported value.
    /// Uses BigDecimal for precision.
    /// Examples: 150 (for cases), 85.5 (for percentages), 1.0 (for boolean represented as number)
    private BigDecimal value;

    /// Optional comment or note about this data value.
    /// Example: "Includes data from mobile clinics" or "Partial data - power outage affected reporting"
    private String comment;

    /// When this data value was entered into the system.
    private LocalDateTime createdAt;

    /// When this data value was last modified.
    private LocalDateTime updatedAt;

    /// Username of the person who entered or last modified this data.
    /// Example: "john.doe" or "admin"
    private String createdBy;
}