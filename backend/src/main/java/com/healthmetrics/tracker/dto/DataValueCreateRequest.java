package com.healthmetrics.tracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for creating new DataValue entries.
 * Used when submitting health data through the API.
 * Unlike DataValueDTO (which includes nested objects for responses),
 * this request DTO accepts simple IDs to create the relationships.
 * Example JSON request body:
 * {
 *   "facilityId": 1,
 *   "indicatorId": 5,
 *   "periodStart": "2024-01-01",
 *   "periodEnd": "2024-01-31",
 *   "periodType": "MONTHLY",
 *   "value": 150,
 *   "comment": "Regular monthly report"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataValueCreateRequest {

    /// ID of the facility reporting this data.
    /// Must reference an existing, active facility.
    @NotNull(message = "Facility ID is required")
    @Positive(message = "Facility ID must be positive")
    private Long facilityId;

    /// ID of the health indicator being reported.
    /// Must reference an existing, active indicator.
    @NotNull(message = "Indicator ID is required")
    @Positive(message = "Indicator ID must be positive")
    private Long indicatorId;

    /// Start date of the reporting period.
    /// Must not be in the future.
    @NotNull(message = "Period start date is required")
    private LocalDate periodStart;

    /// End date of the reporting period.
    /// Must be on or after periodStart.
    @NotNull(message = "Period end date is required")
    private LocalDate periodEnd;

    /// Type of reporting period.
    /// Must be one of: DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    @NotNull(message = "Period type is required")
    @Pattern(
            regexp = "DAILY|WEEKLY|MONTHLY|QUARTERLY|YEARLY",
            message = "Period type must be DAILY, WEEKLY, MONTHLY, QUARTERLY, or YEARLY"
    )
    private String periodType;

    /// The data value being reported.
    /// Must be non-negative for most indicators (business logic will validate further).
    @NotNull(message = "Value is required")
    private BigDecimal value;

    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String comment;

    @Size(max = 100, message = "CreatedBy must not exceed 100 characters")
    private String createdBy;
}