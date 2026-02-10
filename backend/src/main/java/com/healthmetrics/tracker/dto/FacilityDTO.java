package com.healthmetrics.tracker.dto;

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

    /// Unique identifier for the facility.
    private Long id;

    /// Unique facility code. Example: "FAC001", "HOSP_ATH_001"
    private String code;

    /// Full name of the facility. Example: "Athens General Hospital"
    private String name;

    /// Type of facility: "Hospital", "Clinic", or "Health Center"
    private String type;

    /// Administrative region. Example: "Attica", "Central Macedonia"
    private String region;

    /// District within the region. Example: "Athens", "Thessaloniki"
    private String district;

    /// Geographic coordinates - Latitude (e.g., 37.9838)
    private Double latitude;

    /// Geographic coordinates - Longitude (e.g., 23.7275)
    private Double longitude;

    /// Whether the facility is currently active in the system.
    /// Inactive facilities remain in the database for historical data
    /// but won't appear in data entry forms.
    private Boolean active;

    /// When this facility was created in the system.
    /// Included in response for audit purposes.
    private LocalDateTime createdAt;

    /// When this facility was last updated.
    /// Useful for tracking changes and cache invalidation.
    private LocalDateTime updatedAt;
}