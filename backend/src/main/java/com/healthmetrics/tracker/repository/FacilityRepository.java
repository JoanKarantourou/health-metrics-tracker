package com.healthmetrics.tracker.repository;

import com.healthmetrics.tracker.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Facility entity database operations.
 */
@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    /**
     * Find a facility by its unique code.
     * @param code The facility code to search for
     * @return Optional containing the facility if found, empty otherwise
     */
    Optional<Facility> findByCode(String code);

    /**
     * Find all facilities in a specific region.
     * @param region The region name to filter by
     * @return List of facilities in that region
     */
    List<Facility> findByRegion(String region);

    /**
     * Find all active or inactive facilities.
     * Useful for filtering out inactive facilities from drop-downs
     * while keeping them available for historical data.
     * @param active True for active facilities, false for inactive
     * @return List of facilities matching the active status
     */
    List<Facility> findByActive(Boolean active);
}