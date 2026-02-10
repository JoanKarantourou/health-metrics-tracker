package com.healthmetrics.tracker.repository;

import com.healthmetrics.tracker.entity.DataValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for DataValue entity database operations.
 */
@Repository
public interface DataValueRepository extends JpaRepository<DataValue, Long> {

    /**
     * Find all data values submitted by a specific facility.
     */
    List<DataValue> findByFacilityId(Long facilityId);

    /**
     * Find all data values for a specific health indicator.
     */
    List<DataValue> findByIndicatorId(Long indicatorId);

    /**
     * Find data values for a facility within a date range.
     */
    @Query("SELECT dv FROM DataValue dv WHERE dv.facility.id = :facilityId " +
            "AND dv.periodStart >= :startDate AND dv.periodEnd <= :endDate")
    List<DataValue> findByFacilityAndPeriod(
            @Param("facilityId") Long facilityId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find data values for an indicator in a specific region.
     */
    @Query("SELECT dv FROM DataValue dv WHERE dv.indicator.id = :indicatorId " +
            "AND dv.facility.region = :region " +
            "AND dv.periodStart >= :startDate")
    List<DataValue> findByIndicatorAndRegion(
            @Param("indicatorId") Long indicatorId,
            @Param("region") String region,
            @Param("startDate") LocalDate startDate
    );
}