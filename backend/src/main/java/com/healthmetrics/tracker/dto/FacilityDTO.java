package com.healthmetrics.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Facility.
 * This DTO is used to send facility information to API clients.
 * Simplified version of the Facility entity (without JPA annotations).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityDTO {

    private Long id;

    @NotBlank(message = "Facility code is required")
    @Size(max = 50, message = "Facility code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Facility name is required")
    @Size(max = 200, message = "Facility name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Facility type is required")
    @Size(max = 50, message = "Facility type must not exceed 50 characters")
    private String type;

    @Size(max = 100, message = "Region must not exceed 100 characters")
    private String region;

    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;

    private Double latitude;
    private Double longitude;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
