package com.healthmetrics.tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a single health data value reported by a facility.
 * This is the core data model - it links facilities and indicators with actual values.
 * Example DataValue:
 * - Facility: "Athens General Hospital"
 * - Indicator: "Malaria Cases"
 * - Period: January 2024
 * - Value: 150 cases
 * The unique constraint ensures no duplicate data for the same facility,
 * indicator, and period start date.
 */
@Entity
@Table(
        name = "data_values",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"facility_id", "indicator_id", "period_start"},
                name = "uk_facility_indicator_period"
        ),
        indexes = {
                @Index(name = "idx_dv_facility", columnList = "facility_id"),
                @Index(name = "idx_dv_indicator", columnList = "indicator_id"),
                @Index(name = "idx_dv_period_start", columnList = "period_start"),
                @Index(name = "idx_dv_facility_period", columnList = "facility_id, period_start, period_end")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataValue {

    /// Primary key - auto-generated ID.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /// The facility that reported this data.
    /// Many-to-One relationship: Many data values belong to one facility.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    /// The health indicator this data value represents.
    /// Many-to-One relationship: Many data values belong to one indicator.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private HealthIndicator indicator;

    /// Start date of the reporting period.
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    /// End date of the reporting period.
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    /// Type of reporting period.
    /// Valid values: "DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY"
    @Column(name = "period_type", nullable = false, length = 20)
    private String periodType;

    /// The actual reported value. Uses BigDecimal for precision.
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal value;

    /// Optional comment about this data value.
    @Column(length = 500)
    private String comment;

    /// Timestamp when this data value was entered.
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /// Timestamp when this data value was last modified.
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /// Username of the person who entered this data.
    @Column(name = "created_by", length = 100)
    private String createdBy;
}