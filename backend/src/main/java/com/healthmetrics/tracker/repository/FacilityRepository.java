package com.healthmetrics.tracker.repository;

import com.healthmetrics.tracker.entity.Facility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * JPQL = Java Persistence Query Language (similar to SQL but works with entities)
     * LOWER() makes the search case-insensitive
     * CONCAT('%', :searchTerm, '%') adds wildcards for partial matching
     *
     * @param searchTerm The term to search for in name or code
     * @return List of matching facilities
     */
    @Query("SELECT f FROM Facility f WHERE " +
            "LOWER(f.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(f.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Facility> searchByNameOrCode(@Param("searchTerm") String searchTerm);

    /**
     * Advanced search with multiple optional filters (no pagination).
     * This query dynamically handles null parameters.
     * If a parameter is null, that condition is ignored (using OR with IS NULL check).
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

    /**
     * Advanced search with multiple optional filters AND pagination support.
     * Uses native SQL query to avoid JPQL parsing issues with PostgreSQL.
     *
     * @param region Optional region filter (null = ignore)
     * @param type Optional type filter (null = ignore)
     * @param active Optional active status filter (null = ignore)
     * @param searchTerm Optional search term for name/code (null = ignore)
     * @param pageable Pagination and sorting information
     * @return Page of matching facilities with pagination metadata
     */
    @Query(value = "SELECT * FROM facilities f WHERE " +
            "(:region IS NULL OR f.region = :region) AND " +
            "(:type IS NULL OR f.type = :type) AND " +
            "(:active IS NULL OR f.active = :active) AND " +
            "(:searchTerm IS NULL OR " +
            "LOWER(f.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(f.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')))",
            countQuery = "SELECT COUNT(*) FROM facilities f WHERE " +
                    "(:region IS NULL OR f.region = :region) AND " +
                    "(:type IS NULL OR f.type = :type) AND " +
                    "(:active IS NULL OR f.active = :active) AND " +
                    "(:searchTerm IS NULL OR " +
                    "LOWER(f.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                    "LOWER(f.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')))",
            nativeQuery = true)
    Page<Facility> searchWithFiltersPageable(
            @Param("region") String region,
            @Param("type") String type,
            @Param("active") Boolean active,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
}