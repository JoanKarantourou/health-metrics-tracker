package com.healthmetrics.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for HealthIndicator.
 * Represents a health metric or indicator that facilities report on.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthIndicatorDTO {

    private Long id;

    @NotBlank(message = "Indicator code is required")
    @Size(max = 50, message = "Indicator code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Indicator name is required")
    @Size(max = 200, message = "Indicator name must not exceed 200 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @NotBlank(message = "Data type is required")
    @Size(max = 20, message = "Data type must not exceed 20 characters")
    private String dataType;

    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
