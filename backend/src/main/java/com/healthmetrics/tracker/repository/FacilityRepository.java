package com.healthmetrics.tracker.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Find facilities by type (Hospital, Clinic, Health Center).
     * @param type The facility type to filter by
     * @return List of facilities of that type
     */
    List<Facility> findByType(String type);

    /**
     * Find facilities by region AND type.
     * Multiple conditions combined with "And"
     * @param region The region to filter by
     * @param type The facility type to filter by
     * @return List of facilities matching both criteria
     */
    List<Facility> findByRegionAndType(String region, String type);

    /**
     * Search facilities by name or code (case-insensitive).
     * Uses JPQL query for complex search logic.
     *
     * @param searchTerm The term to search for in name or code
     * @return List of matching facilities
     */
    @Query("SELECT f FROM Facility f WHERE " +
            "LOWER(f.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(f.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Facility> searchByNameOrCode(@Param("searchTerm") String searchTerm);

    /**
     * Advanced search with multiple optional filters.
     * This query dynamically handles null parameters.
     * If a parameter is null, that condition is ignored (using OR with IS NULL check).
     *
     * This allows flexible searching:
     *
     * @param region Optional region filter (null = ignore)
     * @param type Optional type filter (null = ignore)
     * @param active Optional active status filter (null = ignore)
     * @param searchTerm Optional search term for name/code (null = ignore)
     * @return List of matching facilities
     */
    @Query("SELECT f FROM Facility f WHERE " +
            "(:region IS NULL OR f.region = :region) AND " +
            "(:type IS NULL OR f.type = :type) AND " +
            "(:active IS NULL OR f.active = :active) AND " +
            "(:searchTerm IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(f.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Facility> searchWithFilters(
            @Param("region") String region,
            @Param("type") String type,
            @Param("active") Boolean active,
            @Param("searchTerm") String searchTerm
    );
}