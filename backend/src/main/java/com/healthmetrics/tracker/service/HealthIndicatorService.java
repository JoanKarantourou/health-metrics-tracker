package com.healthmetrics.tracker.service;

import com.healthmetrics.tracker.dto.HealthIndicatorDTO;
import com.healthmetrics.tracker.entity.HealthIndicator;
import com.healthmetrics.tracker.exception.DuplicateResourceException;
import com.healthmetrics.tracker.exception.ResourceNotFoundException;
import com.healthmetrics.tracker.exception.ValidationException;
import com.healthmetrics.tracker.repository.HealthIndicatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for HealthIndicator operations.
 * Manages health indicators (metrics) that facilities report on.
 * <p>
 * Examples of indicators:
 * - Malaria cases
 * - BCG vaccination coverage
 * - Antenatal care visits
 */
@Service
@RequiredArgsConstructor
@Transactional
public class HealthIndicatorService {

    /// Repository injected via constructor
    private final HealthIndicatorRepository healthIndicatorRepository;

    /**
     * Retrieves all health indicators from the database.
     *
     * @return List of all indicators as DTOs
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "indicators", key = "'all'")
    public List<HealthIndicatorDTO> getAllIndicators() {
        return healthIndicatorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single indicator by its ID.
     *
     * @param id The indicator ID
     * @return HealthIndicatorDTO if found
     * @throws ResourceNotFoundException if indicator doesn't exist
     */
    @Transactional(readOnly = true)
    public HealthIndicatorDTO getIndicatorById(Long id) {
        HealthIndicator indicator = healthIndicatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health indicator not found with id: " + id));
        return convertToDTO(indicator);
    }

    /**
     * Retrieves an indicator by its unique code.
     * Useful for lookups when you have the indicator code but not the ID.
     *
     * @param code The indicator code (e.g., "MAL_001", "VAC_BCG")
     * @return HealthIndicatorDTO if found
     * @throws ResourceNotFoundException if indicator doesn't exist
     */
    @Transactional(readOnly = true)
    public HealthIndicatorDTO getIndicatorByCode(String code) {
        HealthIndicator indicator = healthIndicatorRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health indicator not found with code: " + code));
        return convertToDTO(indicator);
    }

    /**
     * Retrieves all indicators in a specific category.
     * Useful for grouping indicators in the UI (e.g., all "Maternal Health" indicators).
     *
     * @param category The category name (e.g., "Maternal Health", "Disease Control")
     * @return List of indicators in that category
     */
    @Transactional(readOnly = true)
    public List<HealthIndicatorDTO> getIndicatorsByCategory(String category) {
        return healthIndicatorRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all active indicators.
     * Active indicators are available for data entry.
     * Inactive indicators are typically retired but kept for historical data.
     *
     * @return List of active indicators
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "indicators", key = "'active'")
    public List<HealthIndicatorDTO> getActiveIndicators() {
        return healthIndicatorRepository.findByActive(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all indicators of a specific data type.
     * Useful for filtering indicators by their expected value type.
     *
     * @param dataType The data type (e.g., "NUMBER", "PERCENTAGE", "BOOLEAN")
     * @return List of indicators with that data type
     */
    @Transactional(readOnly = true)
    public List<HealthIndicatorDTO> getIndicatorsByDataType(String dataType) {
        return healthIndicatorRepository.findByDataType(dataType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new health indicator in the system.
     * Validates that the indicator code doesn't already exist.
     *
     * @param indicatorDTO The indicator data to create
     * @return The created indicator as DTO
     * @throws IllegalArgumentException if indicator code already exists
     */
    @CacheEvict(value = "indicators", allEntries = true)
    public HealthIndicatorDTO createIndicator(HealthIndicatorDTO indicatorDTO) {
        // Business rule: Indicator code must be unique
        if (healthIndicatorRepository.findByCode(indicatorDTO.getCode()).isPresent()) {
            throw new DuplicateResourceException(
                    "Health indicator with code '" + indicatorDTO.getCode() + "' already exists");
        }

        // Validate data type
        validateDataType(indicatorDTO.getDataType());

        // Convert DTO to entity
        HealthIndicator indicator = convertToEntity(indicatorDTO);

        // Set default values for new indicators
        indicator.setActive(true);

        // Save to database
        HealthIndicator savedIndicator = healthIndicatorRepository.save(indicator);

        // Convert back to DTO and return
        return convertToDTO(savedIndicator);
    }

    /**
     * Updates an existing health indicator.
     * Only updates fields that are provided (non-null).
     *
     * @param id           The ID of the indicator to update
     * @param indicatorDTO The updated indicator data
     * @return The updated indicator as DTO
     * @throws ResourceNotFoundException if indicator doesn't exist
     */
    @CacheEvict(value = "indicators", allEntries = true)
    public HealthIndicatorDTO updateIndicator(Long id, HealthIndicatorDTO indicatorDTO) {
        // Check if indicator exists
        HealthIndicator existingIndicator = healthIndicatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health indicator not found with id: " + id));

        // Business rule: If changing code, ensure new code doesn't already exist
        if (indicatorDTO.getCode() != null &&
                !indicatorDTO.getCode().equals(existingIndicator.getCode())) {

            if (healthIndicatorRepository.findByCode(indicatorDTO.getCode()).isPresent()) {
                throw new IllegalArgumentException(
                        "Health indicator with code '" + indicatorDTO.getCode() + "' already exists");
            }
            existingIndicator.setCode(indicatorDTO.getCode());
        }

        // Update fields (only if provided)
        if (indicatorDTO.getName() != null) {
            existingIndicator.setName(indicatorDTO.getName());
        }
        if (indicatorDTO.getDescription() != null) {
            existingIndicator.setDescription(indicatorDTO.getDescription());
        }
        if (indicatorDTO.getCategory() != null) {
            existingIndicator.setCategory(indicatorDTO.getCategory());
        }
        if (indicatorDTO.getDataType() != null) {
            validateDataType(indicatorDTO.getDataType());
            existingIndicator.setDataType(indicatorDTO.getDataType());
        }
        if (indicatorDTO.getUnit() != null) {
            existingIndicator.setUnit(indicatorDTO.getUnit());
        }
        if (indicatorDTO.getActive() != null) {
            existingIndicator.setActive(indicatorDTO.getActive());
        }

        // Save and return
        HealthIndicator updatedIndicator = healthIndicatorRepository.save(existingIndicator);
        return convertToDTO(updatedIndicator);
    }

    /**
     * Deletes a health indicator from the system.
     *
     * @param id The ID of the indicator to delete
     * @throws ResourceNotFoundException if indicator doesn't exist
     */
    @CacheEvict(value = "indicators", allEntries = true)
    public void deleteIndicator(Long id) {
        // Check if indicator exists
        if (!healthIndicatorRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Health indicator not found with id: " + id);
        }

        /// Delete the indicator
        /// WARNING: This will fail if there are DataValues referencing this indicator
        /// due to foreign key constraints
        healthIndicatorRepository.deleteById(id);
    }

    /**
     * Deactivates a health indicator (soft delete).
     * Preferred over hard delete because it preserves historical data.
     * Inactive indicators won't show in data entry but their past data remains accessible.
     *
     * @param id The ID of the indicator to deactivate
     * @return The deactivated indicator as DTO
     * @throws ResourceNotFoundException if indicator doesn't exist
     */
    @CacheEvict(value = "indicators", allEntries = true)
    public HealthIndicatorDTO deactivateIndicator(Long id) {
        HealthIndicator indicator = healthIndicatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health indicator not found with id: " + id));

        indicator.setActive(false);
        HealthIndicator deactivated = healthIndicatorRepository.save(indicator);
        return convertToDTO(deactivated);
    }

    /**
     * Reactivates a previously deactivated indicator.
     *
     * @param id The ID of the indicator to reactivate
     * @return The reactivated indicator as DTO
     * @throws ResourceNotFoundException if indicator doesn't exist
     */
    @CacheEvict(value = "indicators", allEntries = true)
    public HealthIndicatorDTO reactivateIndicator(Long id) {
        HealthIndicator indicator = healthIndicatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health indicator not found with id: " + id));

        indicator.setActive(true);
        HealthIndicator reactivated = healthIndicatorRepository.save(indicator);
        return convertToDTO(reactivated);
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates that the data type is one of the allowed values.
     * Business rule: Data type must be NUMBER, PERCENTAGE, or BOOLEAN.
     *
     * @param dataType The data type to validate
     * @throws IllegalArgumentException if data type is invalid
     */
    private void validateDataType(String dataType) {
        if (dataType == null || dataType.trim().isEmpty()) {
            throw new ValidationException("Data type is required");
        }

        String upperDataType = dataType.toUpperCase();
        if (!upperDataType.equals("NUMBER") &&
                !upperDataType.equals("PERCENTAGE") &&
                !upperDataType.equals("BOOLEAN")) {
            throw new ValidationException(
                    "Data type must be NUMBER, PERCENTAGE, or BOOLEAN. Got: " + dataType);
        }
    }

    // ==================== MAPPING METHODS ====================

    /**
     * Converts a HealthIndicator entity to HealthIndicatorDTO.
     * Called when sending data TO the API client.
     *
     * @param indicator The entity from database
     * @return DTO for API response
     */
    private HealthIndicatorDTO convertToDTO(HealthIndicator indicator) {
        HealthIndicatorDTO dto = new HealthIndicatorDTO();
        dto.setId(indicator.getId());
        dto.setCode(indicator.getCode());
        dto.setName(indicator.getName());
        dto.setDescription(indicator.getDescription());
        dto.setCategory(indicator.getCategory());
        dto.setDataType(indicator.getDataType());
        dto.setUnit(indicator.getUnit());
        dto.setActive(indicator.getActive());
        dto.setCreatedAt(indicator.getCreatedAt());
        dto.setUpdatedAt(indicator.getUpdatedAt());
        return dto;
    }

    /**
     * Converts a HealthIndicatorDTO to HealthIndicator entity.
     * This is called when receiving data FROM the API client.
     * <p>
     * Note: Creates a NEW entity. For updates, we fetch existing and modify.
     *
     * @param dto The DTO from API request
     * @return Entity ready to save to database
     */
    private HealthIndicator convertToEntity(HealthIndicatorDTO dto) {
        HealthIndicator indicator = new HealthIndicator();
        // Don't set ID - it's auto-generated for new entities
        indicator.setCode(dto.getCode());
        indicator.setName(dto.getName());
        indicator.setDescription(dto.getDescription());
        indicator.setCategory(dto.getCategory());
        indicator.setDataType(dto.getDataType().toUpperCase()); // Normalize to uppercase
        indicator.setUnit(dto.getUnit());
        indicator.setActive(dto.getActive() != null ? dto.getActive() : true);
        // createdAt and updatedAt are set automatically by JPA auditing
        return indicator;
    }
}