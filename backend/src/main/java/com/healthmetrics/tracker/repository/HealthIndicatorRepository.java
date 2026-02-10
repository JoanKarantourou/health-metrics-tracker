package com.healthmetrics.tracker.repository;

import com.healthmetrics.tracker.entity.HealthIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for HealthIndicator entity database operations.
 * Provides methods to query health indicators by various criteria.
 */
@Repository
public interface HealthIndicatorRepository extends JpaRepository<HealthIndicator, Long> {

    /**
     * Find a health indicator by its unique code.
     * @param code The indicator code to search for
     * @return Optional containing the indicator if found, empty otherwise
     */
    Optional<HealthIndicator> findByCode(String code);

    /**
     * Find all indicators in a specific category.
     * Useful for displaying grouped indicators in the UI.
     * Example: Show all "Maternal Health" indicators together.
     * @param category The category name to filter by
     * @return List of indicators in that category
     */
    List<HealthIndicator> findByCategory(String category);

    /**
     * Find all active or inactive indicators.
     * Active indicators appear in data entry forms.
     * Inactive indicators are hidden but data remains for historical reporting.
     * @param active True for active indicators, false for inactive
     * @return List of indicators matching the active status
     */
    List<HealthIndicator> findByActive(Boolean active);

    /**
     * Find all active indicators in a specific category.
     * Combines both filters - useful for populating data entry dropdowns
     * with only relevant, active indicators.
     * @param category The category to filter by
     * @param active The active status to filter by
     * @return List of indicators matching both criteria
     */
    List<HealthIndicator> findByCategoryAndActive(String category, Boolean active);
}