package com.healthmetrics.tracker.service;

import com.healthmetrics.tracker.dto.DataValueCreateRequest;
import com.healthmetrics.tracker.dto.DataValueDTO;
import com.healthmetrics.tracker.dto.FacilityDTO;
import com.healthmetrics.tracker.dto.HealthIndicatorDTO;
import com.healthmetrics.tracker.entity.DataValue;
import com.healthmetrics.tracker.entity.Facility;
import com.healthmetrics.tracker.entity.HealthIndicator;
import com.healthmetrics.tracker.exception.ResourceNotFoundException;
import com.healthmetrics.tracker.exception.ValidationException;
import com.healthmetrics.tracker.repository.DataValueRepository;
import com.healthmetrics.tracker.repository.FacilityRepository;
import com.healthmetrics.tracker.repository.HealthIndicatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for DataValue operations.
 * DataValue represents the core data in the system - the actual health metrics
 * reported by facilities for specific indicators during specific time periods.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DataValueService {

    /// Repositories injected via constructor
    private final DataValueRepository dataValueRepository;
    private final FacilityRepository facilityRepository;
    private final HealthIndicatorRepository healthIndicatorRepository;

    /**
     * Retrieves all data values from the database.
     * For huge return amount use pagination or filtering.
     *
     * @return List of all data values as DTOs
     */
    public List<DataValueDTO> getAllDataValues() {
        return dataValueRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single data value by its ID.
     *
     * @param id The data value ID
     * @return DataValueDTO if found
     * @throws ResourceNotFoundException if data value doesn't exist
     */
    public DataValueDTO getDataValueById(Long id) {
        DataValue dataValue = dataValueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Data value not found with id: " + id));
        return convertToDTO(dataValue);
    }

    /**
     * Submits a new data value to the system.
     * This is the main method used when facilities report their health metrics.
     *
     * Validation performed:
     * - Facility exists and is active
     * - Indicator exists and is active
     * - Period end date is not before period start date
     * - No duplicate data for same facility, indicator, and period start
     * - Value is non-negative (for most indicators)
     *
     * @param request The data submission request
     * @return The created data value as DTO
     * @throws ResourceNotFoundException if facility or indicator doesn't exist
     * @throws ValidationException if validation rules are violated
     */
    public DataValueDTO submitDataValue(DataValueCreateRequest request) {
        // Step 1: Validate and fetch the facility
        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facility not found with id: " + request.getFacilityId()));

        if (!facility.getActive()) {
            throw new ValidationException(
                    "Cannot submit data for inactive facility: " + facility.getName());
        }

        // Step 2: Validate and fetch the health indicator
        HealthIndicator indicator = healthIndicatorRepository.findById(request.getIndicatorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health indicator not found with id: " + request.getIndicatorId()));

        if (!indicator.getActive()) {
            throw new ValidationException(
                    "Cannot submit data for inactive indicator: " + indicator.getName());
        }

        // Step 3: Validate the period dates
        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new ValidationException(
                    "Period end date cannot be before period start date");
        }

        // Step 4: Validate the period is not in the future
        if (request.getPeriodStart().isAfter(LocalDate.now())) {
            throw new ValidationException(
                    "Cannot submit data for future periods");
        }

        // Step 5: Check for duplicate data
        // The unique constraint is: facility_id + indicator_id + period_start
        List<DataValue> existingData = dataValueRepository.findByFacilityIdAndIndicatorIdAndPeriodStart(
                request.getFacilityId(),
                request.getIndicatorId(),
                request.getPeriodStart()
        );

        if (!existingData.isEmpty()) {
            throw new ValidationException(
                    String.format("Data already exists for facility '%s', indicator '%s', and period starting '%s'",
                            facility.getName(), indicator.getName(), request.getPeriodStart()));
        }

        // Step 6: Validate the value based on indicator type
        validateValue(request.getValue(), indicator);

        // Step 7: Create the data value entity
        DataValue dataValue = new DataValue();
        dataValue.setFacility(facility);
        dataValue.setIndicator(indicator);
        dataValue.setPeriodStart(request.getPeriodStart());
        dataValue.setPeriodEnd(request.getPeriodEnd());
        dataValue.setPeriodType(request.getPeriodType());
        dataValue.setValue(request.getValue());
        dataValue.setComment(request.getComment());
        dataValue.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "system");

        // Step 8: Save to database
        DataValue savedDataValue = dataValueRepository.save(dataValue);

        // Step 9: Convert to DTO and return
        return convertToDTO(savedDataValue);
    }

    /**
     * Retrieves all data values submitted by a specific facility.
     *
     * @param facilityId The facility ID
     * @return List of data values for that facility
     */
    public List<DataValueDTO> getDataValuesByFacility(Long facilityId) {
        // Verify facility exists
        if (!facilityRepository.existsById(facilityId)) {
            throw new ResourceNotFoundException("Facility not found with id: " + facilityId);
        }

        return dataValueRepository.findByFacilityId(facilityId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves data values for a specific facility within a date range.
     * Useful for facility-level reporting.
     *
     * @param facilityId The facility ID
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of data values for that facility in the date range
     */
    public List<DataValueDTO> getDataValuesByFacilityAndPeriod(
            Long facilityId,
            LocalDate startDate,
            LocalDate endDate) {

        // Verify facility exists
        if (!facilityRepository.existsById(facilityId)) {
            throw new ResourceNotFoundException("Facility not found with id: " + facilityId);
        }

        return dataValueRepository.findByFacilityAndPeriod(facilityId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all data values for a specific health indicator.
     *
     * @param indicatorId The indicator ID
     * @return List of data values for that indicator
     */
    public List<DataValueDTO> getDataValuesByIndicator(Long indicatorId) {
        // Verify indicator exists
        if (!healthIndicatorRepository.existsById(indicatorId)) {
            throw new ResourceNotFoundException("Health indicator not found with id: " + indicatorId);
        }

        return dataValueRepository.findByIndicatorId(indicatorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing data value.
     * Typically used for corrections or data quality improvements.
     *
     * @param id The data value ID
     * @param request The updated data
     * @return The updated data value as DTO
     * @throws ResourceNotFoundException if data value doesn't exist
     */
    public DataValueDTO updateDataValue(Long id, DataValueCreateRequest request) {
        // Fetch existing data value
        DataValue existingDataValue = dataValueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Data value not found with id: " + id));

        // Validate and fetch facility if changed
        if (!existingDataValue.getFacility().getId().equals(request.getFacilityId())) {
            Facility facility = facilityRepository.findById(request.getFacilityId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Facility not found with id: " + request.getFacilityId()));
            existingDataValue.setFacility(facility);
        }

        // Validate and fetch indicator if changed
        if (!existingDataValue.getIndicator().getId().equals(request.getIndicatorId())) {
            HealthIndicator indicator = healthIndicatorRepository.findById(request.getIndicatorId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Health indicator not found with id: " + request.getIndicatorId()));
            existingDataValue.setIndicator(indicator);
        }

        // Validate period dates
        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new ValidationException("Period end date cannot be before period start date");
        }

        // Validate value
        validateValue(request.getValue(), existingDataValue.getIndicator());

        // Update fields
        existingDataValue.setPeriodStart(request.getPeriodStart());
        existingDataValue.setPeriodEnd(request.getPeriodEnd());
        existingDataValue.setPeriodType(request.getPeriodType());
        existingDataValue.setValue(request.getValue());
        existingDataValue.setComment(request.getComment());
        // updatedAt is automatically set by JPA auditing

        // Save and return
        DataValue updatedDataValue = dataValueRepository.save(existingDataValue);
        return convertToDTO(updatedDataValue);
    }

    /**
     * Deletes a data value from the system.
     * Use with caution - this permanently removes data.
     *
     * @param id The data value ID to delete
     * @throws ResourceNotFoundException if data value doesn't exist
     */
    public void deleteDataValue(Long id) {
        if (!dataValueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Data value not found with id: " + id);
        }
        dataValueRepository.deleteById(id);
    }

    // ==================== AGGREGATION METHODS ====================
    // These methods provide summarized data for dashboards and reports

    /**
     * Aggregates data values by region for a specific indicator.
     * Useful for regional comparisons and dashboards.
     *
     * Example: "Show me total malaria cases by region for 2024"
     * Result: { "Attica": 150, "Central Macedonia": 230, ... }
     *
     * @param indicatorId The indicator to aggregate
     * @param region Optional region filter (null = all regions)
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return Map of region name to aggregated value
     */
    public Map<String, BigDecimal> aggregateByRegion(
            Long indicatorId,
            String region,
            LocalDate startDate,
            LocalDate endDate) {

        // Verify indicator exists
        HealthIndicator indicator = healthIndicatorRepository.findById(indicatorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health indicator not found with id: " + indicatorId));

        // Fetch data based on whether region filter is provided
        List<DataValue> dataValues;
        if (region != null && !region.trim().isEmpty()) {
            dataValues = dataValueRepository.findByIndicatorAndRegion(
                    indicatorId, region, startDate);
        } else {
            dataValues = dataValueRepository.findByIndicatorId(indicatorId);
        }

        // Filter by date range and aggregate by region
        Map<String, BigDecimal> regionAggregates = new HashMap<>();

        dataValues.stream()
                .filter(dv -> !dv.getPeriodStart().isBefore(startDate) &&
                        !dv.getPeriodEnd().isAfter(endDate))
                .forEach(dv -> {
                    String facilityRegion = dv.getFacility().getRegion();
                    BigDecimal currentValue = regionAggregates.getOrDefault(
                            facilityRegion, BigDecimal.ZERO);
                    regionAggregates.put(facilityRegion, currentValue.add(dv.getValue()));
                });

        return regionAggregates;
    }

    /**
     * Calculates the total/sum for a specific indicator across all facilities.
     *
     * @param indicatorId The indicator to sum
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return Total value
     */
    public BigDecimal getTotalForIndicator(
            Long indicatorId,
            LocalDate startDate,
            LocalDate endDate) {

        List<DataValue> dataValues = dataValueRepository.findByIndicatorId(indicatorId);

        return dataValues.stream()
                .filter(dv -> !dv.getPeriodStart().isBefore(startDate) &&
                        !dv.getPeriodEnd().isAfter(endDate))
                .map(DataValue::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the average value for a specific indicator.
     *
     * @param indicatorId The indicator to average
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return Average value
     */
    public BigDecimal getAverageForIndicator(
            Long indicatorId,
            LocalDate startDate,
            LocalDate endDate) {

        List<DataValue> dataValues = dataValueRepository.findByIndicatorId(indicatorId);

        List<DataValue> filteredValues = dataValues.stream()
                .filter(dv -> !dv.getPeriodStart().isBefore(startDate) &&
                        !dv.getPeriodEnd().isAfter(endDate))
                .collect(Collectors.toList());

        if (filteredValues.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = filteredValues.stream()
                .map(DataValue::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(
                BigDecimal.valueOf(filteredValues.size()),
                2,
                BigDecimal.ROUND_HALF_UP);
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates a data value based on the indicator's data type.
     *
     * Business rules:
     * - All values must be non-negative
     * - PERCENTAGE values must be between 0 and 100
     * - BOOLEAN values must be 0 or 1
     *
     * @param value The value to validate
     * @param indicator The indicator this value is for
     * @throws ValidationException if validation fails
     */
    private void validateValue(BigDecimal value, HealthIndicator indicator) {
        // Rule 1: Value cannot be null
        if (value == null) {
            throw new ValidationException("Value cannot be null");
        }

        // Rule 2: Value cannot be negative
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Value cannot be negative");
        }

        // Rule 3: Type-specific validation
        String dataType = indicator.getDataType().toUpperCase();

        switch (dataType) {
            case "PERCENTAGE":
                // Percentage must be between 0 and 100
                if (value.compareTo(BigDecimal.valueOf(100)) > 0) {
                    throw new ValidationException(
                            "Percentage value cannot exceed 100. Got: " + value);
                }
                break;

            case "BOOLEAN":
                // Boolean must be exactly 0 or 1
                if (value.compareTo(BigDecimal.ZERO) != 0 &&
                        value.compareTo(BigDecimal.ONE) != 0) {
                    throw new ValidationException(
                            "Boolean value must be 0 or 1. Got: " + value);
                }
                break;

            case "NUMBER":
                // No additional validation for numbers
                break;

            default:
                throw new ValidationException(
                        "Unknown data type: " + dataType);
        }
    }

    // ==================== MAPPING METHODS ====================

    /**
     * Converts a DataValue entity to DataValueDTO.
     * This triggers lazy loading of facility and indicator relationships.
     *
     * @param dataValue The entity from database
     * @return DTO for API response with nested facility and indicator details
     */
    private DataValueDTO convertToDTO(DataValue dataValue) {
        DataValueDTO dto = new DataValueDTO();
        dto.setId(dataValue.getId());

        // Convert nested facility to DTO
        // This triggers lazy loading of the facility relationship
        Facility facility = dataValue.getFacility();
        dto.setFacility(convertFacilityToDTO(facility));

        // Convert nested indicator to DTO
        // This triggers lazy loading of the indicator relationship
        HealthIndicator indicator = dataValue.getIndicator();
        dto.setIndicator(convertIndicatorToDTO(indicator));

        dto.setPeriodStart(dataValue.getPeriodStart());
        dto.setPeriodEnd(dataValue.getPeriodEnd());
        dto.setPeriodType(dataValue.getPeriodType());
        dto.setValue(dataValue.getValue());
        dto.setComment(dataValue.getComment());
        dto.setCreatedAt(dataValue.getCreatedAt());
        dto.setUpdatedAt(dataValue.getUpdatedAt());
        dto.setCreatedBy(dataValue.getCreatedBy());

        return dto;
    }

    /**
     * Helper method to convert Facility entity to FacilityDTO.
     *
     * @param facility The facility entity
     * @return FacilityDTO
     */
    private FacilityDTO convertFacilityToDTO(Facility facility) {
        FacilityDTO dto = new FacilityDTO();
        dto.setId(facility.getId());
        dto.setCode(facility.getCode());
        dto.setName(facility.getName());
        dto.setType(facility.getType());
        dto.setRegion(facility.getRegion());
        dto.setDistrict(facility.getDistrict());
        dto.setLatitude(facility.getLatitude());
        dto.setLongitude(facility.getLongitude());
        dto.setActive(facility.getActive());
        dto.setCreatedAt(facility.getCreatedAt());
        dto.setUpdatedAt(facility.getUpdatedAt());
        return dto;
    }

    /**
     * Helper method to convert HealthIndicator entity to HealthIndicatorDTO.
     *
     * @param indicator The indicator entity
     * @return HealthIndicatorDTO
     */
    private HealthIndicatorDTO convertIndicatorToDTO(HealthIndicator indicator) {
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
}