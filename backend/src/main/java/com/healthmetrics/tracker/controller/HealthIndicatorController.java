package com.healthmetrics.tracker.controller;

import com.healthmetrics.tracker.dto.HealthIndicatorDTO;
import com.healthmetrics.tracker.service.HealthIndicatorService;
import com.healthmetrics.tracker.exception.ResourceNotFoundException;
import com.healthmetrics.tracker.exception.ValidationException;
import com.healthmetrics.tracker.exception.DuplicateResourceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for HealthIndicator endpoints.
 * Handles HTTP requests for managing health indicators (metrics tracked across facilities).
 *
 * Base URL: /api/indicators
 *
 * Examples of indicators: Malaria Cases, Vaccination Coverage, Child Mortality Rate
 */
@RestController
@RequestMapping("/api/indicators")
@RequiredArgsConstructor
public class HealthIndicatorController {

    /// Service is injected via constructor (using @RequiredArgsConstructor)
    private final HealthIndicatorService indicatorService;

    /**
     * GET /api/indicators
     * Retrieves all health indicators in the system.
     *
     * @return List of all indicators with HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<HealthIndicatorDTO>> getAllIndicators() {
        List<HealthIndicatorDTO> indicators = indicatorService.getAllIndicators();
        return ResponseEntity.ok(indicators);
    }

    /**
     * GET /api/indicators/{id}
     * Retrieves a single health indicator by its ID.
     *
     * @param id The indicator ID from the URL path
     * @return HealthIndicatorDTO with HTTP 200 OK if found
     * @throws ResourceNotFoundException if indicator doesn't exist (returns 404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<HealthIndicatorDTO> getIndicatorById(@PathVariable Long id) {
        HealthIndicatorDTO indicator = indicatorService.getIndicatorById(id);
        return ResponseEntity.ok(indicator);
    }

    /**
     * GET /api/indicators/code/{code}
     * Retrieves an indicator by its unique code (e.g., "IND001").
     *
     * @param code The indicator code from the URL path
     * @return HealthIndicatorDTO with HTTP 200 OK if found
     * @throws ResourceNotFoundException if indicator doesn't exist (returns 404)
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<HealthIndicatorDTO> getIndicatorByCode(@PathVariable String code) {
        HealthIndicatorDTO indicator = indicatorService.getIndicatorByCode(code);
        return ResponseEntity.ok(indicator);
    }

    /**
     * GET /api/indicators/category/{category}
     * Retrieves all indicators in a specific category.
     *
     * @param category The category name (e.g., "Maternal Health", "Child Health", "Disease")
     * @return List of indicators in that category with HTTP 200 OK
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<HealthIndicatorDTO>> getIndicatorsByCategory(@PathVariable String category) {
        List<HealthIndicatorDTO> indicators = indicatorService.getIndicatorsByCategory(category);
        return ResponseEntity.ok(indicators);
    }

    /**
     * GET /api/indicators/data-type/{dataType}
     * Retrieves all indicators with a specific data type.
     *
     * @param dataType The data type (e.g., "NUMBER", "PERCENTAGE", "BOOLEAN")
     * @return List of indicators with that data type with HTTP 200 OK
     */
    @GetMapping("/data-type/{dataType}")
    public ResponseEntity<List<HealthIndicatorDTO>> getIndicatorsByDataType(@PathVariable String dataType) {
        List<HealthIndicatorDTO> indicators = indicatorService.getIndicatorsByDataType(dataType);
        return ResponseEntity.ok(indicators);
    }

    /**
     * GET /api/indicators/active
     * Retrieves all active indicators (active = true).
     *
     * @return List of active indicators with HTTP 200 OK
     */
    @GetMapping("/active")
    public ResponseEntity<List<HealthIndicatorDTO>> getActiveIndicators() {
        List<HealthIndicatorDTO> indicators = indicatorService.getActiveIndicators();
        return ResponseEntity.ok(indicators);
    }

    /**
     * POST /api/indicators
     * Creates a new health indicator in the system.
     *
     * @param indicatorDTO The indicator data from request body (automatically converted from JSON)
     * @return Created indicator with HTTP 201 Created
     * @throws ValidationException if data is invalid
     * @throws DuplicateResourceException if code already exists
     */
    @PostMapping
    public ResponseEntity<HealthIndicatorDTO> createIndicator(@Valid @RequestBody HealthIndicatorDTO indicatorDTO) {
        HealthIndicatorDTO createdIndicator = indicatorService.createIndicator(indicatorDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIndicator);
    }

    /**
     * PUT /api/indicators/{id}
     * Updates an existing health indicator.
     *
     * @param id The indicator ID to update
     * @param indicatorDTO The updated indicator data from request body
     * @return Updated indicator with HTTP 200 OK
     * @throws ResourceNotFoundException if indicator doesn't exist
     * @throws ValidationException if data is invalid
     */
    @PutMapping("/{id}")
    public ResponseEntity<HealthIndicatorDTO> updateIndicator(
            @PathVariable Long id,
            @Valid @RequestBody HealthIndicatorDTO indicatorDTO) {
        HealthIndicatorDTO updatedIndicator = indicatorService.updateIndicator(id, indicatorDTO);
        return ResponseEntity.ok(updatedIndicator);
    }

    /**
     * DELETE /api/indicators/{id}
     * Deletes a health indicator from the system. (Hard Delete)
     *
     * @param id The indicator ID to delete
     * @return HTTP 204 No Content (successful deletion with no response body)
     * @throws ResourceNotFoundException if indicator doesn't exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndicator(@PathVariable Long id) {
        indicatorService.deleteIndicator(id);
        return ResponseEntity.noContent().build();
    }
}