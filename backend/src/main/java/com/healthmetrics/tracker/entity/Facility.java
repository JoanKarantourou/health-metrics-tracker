package com.healthmetrics.tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a health facility in the system.
 * A facility can be a hospital, clinic, or health center that reports
 * health metrics data. Each facility has a unique code and tracks its
 * location and administrative region.
 */

@Entity
@Table(name = "facilities", indexes = {
        @Index(name = "idx_facility_region", columnList = "region"),
        @Index(name = "idx_facility_active", columnList = "active"),
        @Index(name = "idx_facility_type", columnList = "type"),
        @Index(name = "idx_facility_region_type", columnList = "region, type")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Facility {
    /// Primary key - auto-generated ID.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /// Full name of the facility. Example: "Athens General Hospital"
    @Column(nullable = false, length = 200)
    private String name;

    /// Type of facility: "Hospital", "Clinic", or "Health Center". (also can be used as an enum?)
    @Column(nullable = false, length = 50)
    private String type;

    /// Administrative region where the facility is located. Example: "Attica", "Central Macedonia"
    @Column(length = 100)
    private String region;

    /// District within the region. Example: "Athens", "Thessaloniki"
    @Column(length = 100)
    private String district;

    /// Geographic coordinates for mapping. Latitude coordinate (e.g., 37.9838).
    private Double latitude;

    /// Geographic coordinates for mapping.Longitude coordinate (e.g., 23.7275).
    private Double longitude;

    /// Indicates if the facility is currently active in the system.
    /// Inactive facilities won't appear in data entry but remain for historical data.
    @Column(nullable = false)
    private Boolean active = true;

    /// Timestamp when the facility was first created in the system. Automatically populated by Spring Data JPA.
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /// Timestamp when the facility was last modified. Automatically updated by Spring Data JPA on any change.
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}