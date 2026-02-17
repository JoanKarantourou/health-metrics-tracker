package com.healthmetrics.tracker.controller;

import com.healthmetrics.tracker.dto.DataValueDTO;
import com.healthmetrics.tracker.dto.DataValueCreateRequest;
import com.healthmetrics.tracker.service.DataValueService;
import com.healthmetrics.tracker.exception.ResourceNotFoundException;
import com.healthmetrics.tracker.exception.ValidationException;
import com.healthmetrics.tracker.exception.DuplicateResourceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for DataValue endpoints.
 * Handles HTTP requests for submitting health data and retrieving aggregated statistics.
 *
 * Base URL: /api/data-values
 *
 * This is the core controller for the application - it handles:
 * - Data submission from health facilities
 * - Retrieving data by facility and time period
 * - Aggregating data by region for dashboard analytics
 */
@RestController
@RequestMapping("/api/data-values")
@RequiredArgsConstructor
public class DataValueController {

    /// Service is injected via constructor (using @RequiredArgsConstructor)
    private final DataValueService dataValueService;

    /**
     * POST /api/data-values
     * Submits a new data value (e.g., "50 malaria cases at Athens Hospital in January").
     *
     * @param request The data submission from request body (contains facilityId, indicatorId, dates, value)
     * @return Created data value with HTTP 201 Created
     * @throws ResourceNotFoundException if facility or indicator doesn't exist
     * @throws ValidationException if data is invalid (wrong data type, invalid dates, etc.)
     * @throws DuplicateResourceException if data for this facility/indicator/period already exists
     *
     * Example JSON request body:
     * {
     *   "facilityId": 1,
     *   "indicatorId": 2,
     *   "periodStart": "2024-01-01",
     *   "periodEnd": "2024-01-31",
     *   "periodType": "MONTHLY",
     *   "value": 50.0,
     *   "comment": "Data verified by supervisor"
     * }
     */
    @PostMapping
    public ResponseEntity<DataValueDTO> submitDataValue(@Valid @RequestBody DataValueCreateRequest request) {
        DataValueDTO createdDataValue = dataValueService.submitDataValue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDataValue);
    }

    /**
     * GET /api/data-values/{id}
     * Retrieves a single data value by its ID.
     *
     * @param id The data value ID
     * @return DataValueDTO with HTTP 200 OK if found
     * @throws ResourceNotFoundException if data value doesn't exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<DataValueDTO> getDataValueById(@PathVariable Long id) {
        DataValueDTO dataValue = dataValueService.getDataValueById(id);
        return ResponseEntity.ok(dataValue);
    }

    /**
     * GET /api/data-values/facility/{facilityId}
     * Retrieves all data values for a specific facility (without date filtering).
     * For date-filtered results, use the /facility/{facilityId}/period endpoint.
     *
     * @param facilityId The facility ID
     * @return List of all data values for that facility with HTTP 200 OK
     * @throws ResourceNotFoundException if facility doesn't exist
     *
     * Example: GET /api/data-values/facility/1
     */
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<DataValueDTO>> getDataValuesByFacility(@PathVariable Long facilityId) {
        List<DataValueDTO> dataValues = dataValueService.getDataValuesByFacility(facilityId);
        return ResponseEntity.ok(dataValues);
    }

    /**
     * GET /api/data-values/facility/{facilityId}/period
     * Retrieves data values for a specific facility within a date range.
     * Useful for viewing a facility's reporting history for a specific time period.
     *
     * @param facilityId The facility ID
     * @param startDate Start of date range (format: YYYY-MM-DD)
     * @param endDate End of date range (format: YYYY-MM-DD)
     * @return List of data values with HTTP 200 OK
     * @throws ResourceNotFoundException if facility doesn't exist
     *
     * Example: GET /api/data-values/facility/1/period?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/facility/{facilityId}/period")
    public ResponseEntity<List<DataValueDTO>> getDataValuesByFacilityAndPeriod(
            @PathVariable Long facilityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DataValueDTO> dataValues = dataValueService.getDataValuesByFacilityAndPeriod(
                facilityId, startDate, endDate);
        return ResponseEntity.ok(dataValues);
    }

    /**
     * GET /api/data-values/indicator/{indicatorId}
     * Retrieves all data values for a specific indicator across all facilities.
     * Useful for analyzing how one indicator trends across the entire health system.
     *
     * @param indicatorId The indicator ID
     * @return List of all data values for that indicator with HTTP 200 OK
     * @throws ResourceNotFoundException if indicator doesn't exist
     *
     * Example: GET /api/data-values/indicator/2
     */
    @GetMapping("/indicator/{indicatorId}")
    public ResponseEntity<List<DataValueDTO>> getDataValuesByIndicator(@PathVariable Long indicatorId) {
        List<DataValueDTO> dataValues = dataValueService.getDataValuesByIndicator(indicatorId);
        return ResponseEntity.ok(dataValues);
    }

    /**
     * GET /api/data-values/aggregate/region
     * Aggregates data values by region for dashboard visualizations.
     * Returns the SUM of values grouped by region.
     *
     * @param indicatorId The indicator to aggregate
     * @param region Optional region filter (if null/empty, aggregates all regions)
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return Map of region names to aggregated values with HTTP 200 OK
     * @throws ResourceNotFoundException if indicator doesn't exist
     *
     * Example: GET /api/data-values/aggregate/region?indicatorId=2&startDate=2024-01-01&endDate=2024-12-31
     * Example with region filter: GET /api/data-values/aggregate/region?indicatorId=2&region=Attica&startDate=2024-01-01&endDate=2024-12-31
     *
     * Response example:
     * {
     *   "Attica": 1250.0,
     *   "Central Macedonia": 980.0,
     *   "Crete": 450.0
     * }
     */
    @GetMapping("/aggregate/region")
    public ResponseEntity<Map<String, BigDecimal>> aggregateByRegion(
            @RequestParam Long indicatorId,
            @RequestParam(required = false) String region,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, BigDecimal> aggregatedData = dataValueService.aggregateByRegion(
                indicatorId, region, startDate, endDate);
        return ResponseEntity.ok(aggregatedData);
    }

    /**
     * GET /api/data-values/total
     * Calculates the total (sum) for a specific indicator across all facilities.
     * Useful for dashboard summary cards.
     *
     * @param indicatorId The indicator to sum
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return Total value with HTTP 200 OK
     * @throws ResourceNotFoundException if indicator doesn't exist
     *
     * Example: GET /api/data-values/total?indicatorId=2&startDate=2024-01-01&endDate=2024-12-31
     * Response example: 2680.0
     */
    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalForIndicator(
            @RequestParam Long indicatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        BigDecimal total = dataValueService.getTotalForIndicator(indicatorId, startDate, endDate);
        return ResponseEntity.ok(total);
    }

    /**
     * GET /api/data-values/average
     * Calculates the average value for a specific indicator.
     * Useful for percentage-based indicators or averaging across facilities.
     *
     * @param indicatorId The indicator to average
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return Average value with HTTP 200 OK
     * @throws ResourceNotFoundException if indicator doesn't exist
     *
     * Example: GET /api/data-values/average?indicatorId=2&startDate=2024-01-01&endDate=2024-12-31
     * Response example: 67.25
     */
    @GetMapping("/average")
    public ResponseEntity<BigDecimal> getAverageForIndicator(
            @RequestParam Long indicatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        BigDecimal average = dataValueService.getAverageForIndicator(indicatorId, startDate, endDate);
        return ResponseEntity.ok(average);
    }

    /**
     * DELETE /api/data-values/{id}
     * Deletes a data value from the system.
     * This allows correcting mistakes in data entry.
     *
     * @param id The data value ID to delete
     * @return HTTP 204 No Content (successful deletion with no response body)
     * @throws ResourceNotFoundException if data value doesn't exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDataValue(@PathVariable Long id) {
        dataValueService.deleteDataValue(id);
        return ResponseEntity.noContent().build();
    }
}