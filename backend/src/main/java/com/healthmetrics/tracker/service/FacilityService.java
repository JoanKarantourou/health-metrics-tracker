package com.healthmetrics.tracker.service;

import com.healthmetrics.tracker.dto.FacilityDTO;
import com.healthmetrics.tracker.entity.Facility;
import com.healthmetrics.tracker.exception.ResourceNotFoundException;
import com.healthmetrics.tracker.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Facility operations.
 * Handles business logic, validation, and data transformation between entities and DTOs.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FacilityService {

    /// The repository is injected via constructor
    private final FacilityRepository facilityRepository;

    /**
     * Retrieves all facilities from the database.
     * Converts each Facility entity to FacilityDTO for API response.
     *
     * @return List of all facilities as DTOs
     */
    public List<FacilityDTO> getAllFacilities() {
        return facilityRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single facility by its ID.
     *
     * @param id The facility ID
     * @return FacilityDTO if found
     * @throws ResourceNotFoundException if facility doesn't exist
     */
    public FacilityDTO getFacilityById(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facility not found with id: " + id));
        return convertToDTO(facility);
    }

    /**
     * Retrieves a facility by its unique code.
     * Useful for lookups when you have the facility code but not the ID.
     *
     * @param code The facility code (e.g., "FAC001")
     * @return FacilityDTO if found
     * @throws ResourceNotFoundException if facility doesn't exist
     */
    public FacilityDTO getFacilityByCode(String code) {
        Facility facility = facilityRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facility not found with code: " + code));
        return convertToDTO(facility);
    }

    /**
     * Retrieves all facilities in a specific region.
     * Useful for regional reporting and filtering.
     *
     * @param region The region name (e.g., "Attica")
     * @return List of facilities in that region
     */
    public List<FacilityDTO> getFacilitiesByRegion(String region) {
        return facilityRepository.findByRegion(region).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all active facilities.
     * Inactive facilities are typically excluded from data entry but kept for historical data.
     *
     * @return List of active facilities
     */
    public List<FacilityDTO> getActiveFacilities() {
        return facilityRepository.findByActive(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new facility in the system.
     * Validates that the facility code doesn't already exist.
     *
     * @param facilityDTO The facility data to create
     * @return The created facility as DTO
     * @throws IllegalArgumentException if facility code already exists
     */
    public FacilityDTO createFacility(FacilityDTO facilityDTO) {
        // Business rule: Facility code must be unique
        if (facilityRepository.findByCode(facilityDTO.getCode()).isPresent()) {
            throw new IllegalArgumentException(
                    "Facility with code '" + facilityDTO.getCode() + "' already exists");
        }

        // Convert DTO to entity
        Facility facility = convertToEntity(facilityDTO);

        // Set default values for new facilities
        facility.setActive(true);

        // Save to database
        Facility savedFacility = facilityRepository.save(facility);

        // Convert back to DTO and return
        return convertToDTO(savedFacility);
    }

    /**
     * Updates an existing facility.
     * Only updates fields that are provided (non-null).
     *
     * @param id The ID of the facility to update
     * @param facilityDTO The updated facility data
     * @return The updated facility as DTO
     * @throws ResourceNotFoundException if facility doesn't exist
     */
    public FacilityDTO updateFacility(Long id, FacilityDTO facilityDTO) {
        // Check if facility exists
        Facility existingFacility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facility not found with id: " + id));

        // Business rule: If changing code, ensure new code doesn't already exist
        if (facilityDTO.getCode() != null &&
                !facilityDTO.getCode().equals(existingFacility.getCode())) {

            if (facilityRepository.findByCode(facilityDTO.getCode()).isPresent()) {
                throw new IllegalArgumentException(
                        "Facility with code '" + facilityDTO.getCode() + "' already exists");
            }
            existingFacility.setCode(facilityDTO.getCode());
        }

        // Update fields (only if provided)
        if (facilityDTO.getName() != null) {
            existingFacility.setName(facilityDTO.getName());
        }
        if (facilityDTO.getType() != null) {
            existingFacility.setType(facilityDTO.getType());
        }
        if (facilityDTO.getRegion() != null) {
            existingFacility.setRegion(facilityDTO.getRegion());
        }
        if (facilityDTO.getDistrict() != null) {
            existingFacility.setDistrict(facilityDTO.getDistrict());
        }
        if (facilityDTO.getLatitude() != null) {
            existingFacility.setLatitude(facilityDTO.getLatitude());
        }
        if (facilityDTO.getLongitude() != null) {
            existingFacility.setLongitude(facilityDTO.getLongitude());
        }
        if (facilityDTO.getActive() != null) {
            existingFacility.setActive(facilityDTO.getActive());
        }

        // Save and return
        Facility updatedFacility = facilityRepository.save(existingFacility);
        return convertToDTO(updatedFacility);
    }

    /**
     * Deletes a facility from the system.
     *
     * @param id The ID of the facility to delete
     * @throws ResourceNotFoundException if facility doesn't exist
     */
    public void deleteFacility(Long id) {
        // Check if facility exists
        if (!facilityRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Facility not found with id: " + id);
        }

        // Delete the facility
        facilityRepository.deleteById(id);
    }

    /**
     * Deactivates a facility (soft delete).
     * This is preferred over hard delete because it preserves historical data.
     * Inactive facilities won't show in data entry but their past data remains accessible.
     *
     * @param id The ID of the facility to deactivate
     * @return The deactivated facility as DTO
     * @throws ResourceNotFoundException if facility doesn't exist
     */
    public FacilityDTO deactivateFacility(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facility not found with id: " + id));

        facility.setActive(false);
        Facility deactivated = facilityRepository.save(facility);
        return convertToDTO(deactivated);
    }

    /**
     * Reactivates a previously deactivated facility.
     *
     * @param id The ID of the facility to reactivate
     * @return The reactivated facility as DTO
     * @throws ResourceNotFoundException if facility doesn't exist
     */
    public FacilityDTO reactivateFacility(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facility not found with id: " + id));

        facility.setActive(true);
        Facility reactivated = facilityRepository.save(facility);
        return convertToDTO(reactivated);
    }

    // ==================== MAPPING METHODS ====================
    // These methods convert between Entity and DTO

    /**
     * Converts a Facility entity to FacilityDTO.
     * This is called when sending data TO the API client.
     *
     * @param facility The entity from database
     * @return DTO for API response
     */
    private FacilityDTO convertToDTO(Facility facility) {
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
     * Converts a FacilityDTO to Facility entity.
     * This is called when receiving data FROM the API client.
     *
     * Note: This creates a NEW entity. For updates, we fetch the existing entity
     * and modify it (see updateFacility method).
     *
     * @param dto The DTO from API request
     * @return Entity ready to save to database
     */
    private Facility convertToEntity(FacilityDTO dto) {
        Facility facility = new Facility();
        // Don't set ID - it's auto-generated for new entities
        facility.setCode(dto.getCode());
        facility.setName(dto.getName());
        facility.setType(dto.getType());
        facility.setRegion(dto.getRegion());
        facility.setDistrict(dto.getDistrict());
        facility.setLatitude(dto.getLatitude());
        facility.setLongitude(dto.getLongitude());
        facility.setActive(dto.getActive() != null ? dto.getActive() : true);
        // createdAt and updatedAt are set automatically by JPA auditing
        return facility;
    }
}