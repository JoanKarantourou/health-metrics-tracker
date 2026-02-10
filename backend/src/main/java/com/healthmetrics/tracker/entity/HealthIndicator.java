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
 * Entity representing a health indicator/metric that facilities report on.
 * Examples of health indicators:
 * - Malaria cases
 * - Vaccination coverage percentage
 * - Number of deliveries
 * - TB detection rate
 * Each indicator has a specific data type (number, percentage, boolean)
 * and belongs to a category (Maternal Health, Disease Control, etc.).
 */
@Entity
@Table(name = "health_indicators")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthIndicator {

    /// Primary key - auto-generated ID.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /// Unique code for the indicator. Used for consistent identification across the system.
    /// Example: "MAL_001", "VAC_BCG", "TB_DETECT".
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /// Human-readable name of the indicator.
    /// Example: "Malaria Cases Reported", "BCG Vaccination Coverage"
    @Column(nullable = false, length = 200)
    private String name;

    /// Detailed description of what this indicator measures.
    /// Example: "Total number of confirmed malaria cases in the reporting period"
    @Column(length = 500)
    private String description;

    /// Category/domain this indicator belongs to. Used for grouping indicators in the UI and reports.
    /// Examples: "Maternal Health", "Child Health", "Disease Control", "Immunization"
    @Column(nullable = false, length = 100)
    private String category;

    /// The type of data this indicator expects. This helps with validation and proper display formatting.
    /// Valid values: "NUMBER" (e.g., 150 cases), "PERCENTAGE" (e.g., 85.5%), "BOOLEAN" (e.g., facility has ambulance: true/false)
    @Column(nullable = false, length = 20)
    private String dataType;

    /// Unit of measurement for this indicator. Can be null for boolean indicators.
    /// Examples: "cases", "%", "persons", "visits", "deliveries"
    @Column(length = 50)
    private String unit;

    /// Indicates if this indicator is currently active. Inactive indicators won't show in data entry but historical data remains.
    /// Useful when an indicator is retired or replaced.
    @Column(nullable = false)
    private Boolean active = true;

    /// Timestamp when the indicator was created in the system.
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /// Timestamp when the indicator was last modified.
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}