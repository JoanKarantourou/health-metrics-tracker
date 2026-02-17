package com.healthmetrics.tracker.controller;

import com.healthmetrics.tracker.dto.FacilityDTO;
import com.healthmetrics.tracker.service.FacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.healthmetrics.tracker.exception.ResourceNotFoundException;
import com.healthmetrics.tracker.exception.ValidationException;
import com.healthmetrics.tracker.exception.DuplicateResourceException;

/**
 * REST Controller for Facility endpoints.
 * Handles HTTP requests for creating, reading, updating, and deleting health facilities.
 *
 * Base URL: /api/facilities
 */
@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
public class FacilityController {

    /// Service is injected via constructor
    private final FacilityService facilityService;

    /**
     * GET /api/facilities
     * Retrieves facilities with optional filtering and pagination.
     *
     * Query Parameters:
     * - region: Filter by region (optional)
     * - type: Filter by facility type (optional)
     * - active: Filter by active status (optional)
     * - search: Search by name/code (optional)
     * - page: Page number (default: 0)
     * - size: Page size (default: 10)
     * - sort: Sort field (default: name)
     * - direction: Sort direction (default: asc)
     *
     * @return Paginated list of facilities with HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<Page<FacilityDTO>> getAllFacilities(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        // Create Sort object based on direction
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Create Pageable object (page number, page size, sorting)
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // Get paginated results from service
        Page<FacilityDTO> facilitiesPage = facilityService.searchFacilitiesWithPagination(
                region, type, active, search, pageable
        );

        return ResponseEntity.ok(facilitiesPage);
    }

    /**
     * GET /api/facilities/{id}
     * Retrieves a single facility by its ID.
     *
     * @param id The facility ID from the URL path
     * @return FacilityDTO with HTTP 200 OK if found
     * @throws ResourceNotFoundException if facility doesn't exist (returns 404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<FacilityDTO> getFacilityById(@PathVariable Long id) {
        FacilityDTO facility = facilityService.getFacilityById(id);
        return ResponseEntity.ok(facility);
    }

    /**
     * GET /api/facilities/code/{code}
     * Retrieves a facility by its unique code (e.g., "FAC001").
     *
     * @param code The facility code from the URL path
     * @return FacilityDTO with HTTP 200 OK if found
     * @throws ResourceNotFoundException if facility doesn't exist (returns 404)
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<FacilityDTO> getFacilityByCode(@PathVariable String code) {
        FacilityDTO facility = facilityService.getFacilityByCode(code);
        return ResponseEntity.ok(facility);
    }

    /**
     * GET /api/facilities/region/{region}
     * Retrieves all facilities in a specific region.
     *
     * @param region The region name (e.g., "Attica", "Central Macedonia")
     * @return List of facilities in that region with HTTP 200 OK
     */
    @GetMapping("/region/{region}")
    public ResponseEntity<List<FacilityDTO>> getFacilitiesByRegion(@PathVariable String region) {
        List<FacilityDTO> facilities = facilityService.getFacilitiesByRegion(region);
        return ResponseEntity.ok(facilities);
    }

    /**
     * GET /api/facilities/active
     * Retrieves all active facilities (active = true).
     *
     * @return List of active facilities with HTTP 200 OK
     */
    @GetMapping("/active")
    public ResponseEntity<List<FacilityDTO>> getActiveFacilities() {
        List<FacilityDTO> facilities = facilityService.getActiveFacilities();
        return ResponseEntity.ok(facilities);
    }

    /**
     * POST /api/facilities
     * Creates a new facility in the system.
     *
     * @param facilityDTO The facility data from request body (automatically converted from JSON)
     * @return Created facility with HTTP 201 Created
     * @throws ValidationException if data is invalid
     * @throws DuplicateResourceException if code already exists
     */
    @PostMapping
    public ResponseEntity<FacilityDTO> createFacility(@Valid @RequestBody FacilityDTO facilityDTO) {
        FacilityDTO createdFacility = facilityService.createFacility(facilityDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFacility);
    }

    /**
     * PUT /api/facilities/{id}
     * Updates an existing facility.
     *
     * @param id The facility ID to update
     * @param facilityDTO The updated facility data from request body
     * @return Updated facility with HTTP 200 OK
     * @throws ResourceNotFoundException if facility doesn't exist
     * @throws ValidationException if data is invalid
     */
    @PutMapping("/{id}")
    public ResponseEntity<FacilityDTO> updateFacility(
            @PathVariable Long id,
            @Valid @RequestBody FacilityDTO facilityDTO) {
        FacilityDTO updatedFacility = facilityService.updateFacility(id, facilityDTO);
        return ResponseEntity.ok(updatedFacility);
    }

    /**
     * DELETE /api/facilities/{id}
     * Deletes a facility from the system. (Hard Delete)
     *
     * @param id The facility ID to delete
     * @return HTTP 204 No Content (successful deletion with no response body)
     * @throws ResourceNotFoundException if facility doesn't exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacility(@PathVariable Long id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.noContent().build();
    }
}