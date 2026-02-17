package com.healthmetrics.tracker.service;

import com.healthmetrics.tracker.dto.FacilityDTO;
import com.healthmetrics.tracker.entity.Facility;
import com.healthmetrics.tracker.exception.DuplicateResourceException;
import com.healthmetrics.tracker.exception.ResourceNotFoundException;
import com.healthmetrics.tracker.repository.FacilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FacilityService
 *
 * Similar to C# unit tests with xUnit and Moq:
 * - @ExtendWith(MockitoExtension.class) = enables Mockito (like [Collection] in xUnit)
 * - @Mock = creates mock object (like Mock<T> in C#)
 * - @InjectMocks = injects mocks into service (like constructor injection in C#)
 * - @Test = marks test method (like [Fact] in xUnit)
 * - @BeforeEach = runs before each test (like constructor or [SetUp] in NUnit)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FacilityService Unit Tests")
class FacilityServiceTest {

    // Mock the repository (we don't want to hit the real database)
    // Similar to: Mock<IFacilityRepository> in C#
    @Mock
    private FacilityRepository facilityRepository;

    // Inject the mocked repository into the service
    // Similar to: new FacilityService(mockRepo.Object) in C#
    @InjectMocks
    private FacilityService facilityService;

    // Test data - created once and reused
    private Facility testFacility;
    private FacilityDTO testFacilityDTO;

    /**
     * Set up test data before each test
     * Similar to constructor in xUnit or [SetUp] in NUnit
     */
    @BeforeEach
    void setUp() {
        // Create a sample facility for testing
        testFacility = new Facility();
        testFacility.setId(1L);
        testFacility.setCode("FAC001");
        testFacility.setName("Athens General Hospital");
        testFacility.setType("Hospital");
        testFacility.setRegion("Attica");
        testFacility.setDistrict("Athens");
        testFacility.setLatitude(37.9838);
        testFacility.setLongitude(23.7275);
        testFacility.setActive(true);
        testFacility.setCreatedAt(LocalDateTime.now());
        testFacility.setUpdatedAt(LocalDateTime.now());

        // Create a sample DTO for testing
        testFacilityDTO = new FacilityDTO();
        testFacilityDTO.setId(1L);
        testFacilityDTO.setCode("FAC001");
        testFacilityDTO.setName("Athens General Hospital");
        testFacilityDTO.setType("Hospital");
        testFacilityDTO.setRegion("Attica");
        testFacilityDTO.setDistrict("Athens");
        testFacilityDTO.setLatitude(37.9838);
        testFacilityDTO.setLongitude(23.7275);
        testFacilityDTO.setActive(true);
    }

    // ==================== GET ALL FACILITIES TESTS ====================

    /**
     * Test: Get all facilities successfully
     * Expected: Returns list of all facilities as DTOs
     */
    @Test
    @DisplayName("getAllFacilities - Should return list of all facilities")
    void getAllFacilities_ShouldReturnAllFacilities() {
        // Arrange - prepare mock data
        Facility facility2 = new Facility();
        facility2.setId(2L);
        facility2.setCode("FAC002");
        facility2.setName("Thessaloniki Health Center");
        facility2.setType("Health Center");
        facility2.setRegion("Central Macedonia");
        facility2.setDistrict("Thessaloniki");
        facility2.setActive(true);

        List<Facility> mockFacilities = Arrays.asList(testFacility, facility2);

        // Mock the repository call
        // Similar to: mockRepo.Setup(r => r.GetAll()).Returns(mockFacilities) in C#
        when(facilityRepository.findAll()).thenReturn(mockFacilities);

        // Act - call the service method
        List<FacilityDTO> result = facilityService.getAllFacilities();

        // Assert - verify the results
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 facilities");
        assertEquals("FAC001", result.get(0).getCode(), "First facility code should match");
        assertEquals("FAC002", result.get(1).getCode(), "Second facility code should match");

        // Verify the repository method was called exactly once
        // Similar to: mockRepo.Verify(r => r.GetAll(), Times.Once) in C#
        verify(facilityRepository, times(1)).findAll();
    }

    /**
     * Test: Get all facilities when database is empty
     * Expected: Returns empty list (not null)
     */
    @Test
    @DisplayName("getAllFacilities - Should return empty list when no facilities exist")
    void getAllFacilities_ShouldReturnEmptyList_WhenNoFacilities() {
        // Arrange
        when(facilityRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<FacilityDTO> result = facilityService.getAllFacilities();

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty list");
        verify(facilityRepository, times(1)).findAll();
    }

    // ==================== GET FACILITY BY ID TESTS ====================

    /**
     * Test: Get facility by ID successfully
     * Expected: Returns the facility DTO
     */
    @Test
    @DisplayName("getFacilityById - Should return facility when ID exists")
    void getFacilityById_ShouldReturnFacility_WhenIdExists() {
        // Arrange
        Long facilityId = 1L;

        // Mock repository to return Optional containing the facility
        // Similar to: mockRepo.Setup(r => r.GetById(1)).Returns(facility) in C#
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(testFacility));

        // Act
        FacilityDTO result = facilityService.getFacilityById(facilityId);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(facilityId, result.getId(), "ID should match");
        assertEquals("FAC001", result.getCode(), "Code should match");
        assertEquals("Athens General Hospital", result.getName(), "Name should match");
        verify(facilityRepository, times(1)).findById(facilityId);
    }

    /**
     * Test: Get facility by ID when ID doesn't exist
     * Expected: Throws ResourceNotFoundException
     */
    @Test
    @DisplayName("getFacilityById - Should throw exception when ID doesn't exist")
    void getFacilityById_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        Long nonExistentId = 999L;

        // Mock repository to return empty Optional
        when(facilityRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert - verify exception is thrown
        // Similar to: Assert.Throws<ResourceNotFoundException>(() => ...) in C#
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> facilityService.getFacilityById(nonExistentId),
                "Should throw ResourceNotFoundException"
        );

        // Verify exception message
        assertTrue(exception.getMessage().contains("Facility not found"),
                "Exception message should contain 'Facility not found'");
        verify(facilityRepository, times(1)).findById(nonExistentId);
    }

    // ==================== CREATE FACILITY TESTS ====================

    /**
     * Test: Create new facility successfully
     * Expected: Returns created facility DTO with ID
     */
    @Test
    @DisplayName("createFacility - Should create and return new facility")
    void createFacility_ShouldCreateFacility_WhenValidData() {
        // Arrange
        FacilityDTO newFacilityDTO = new FacilityDTO();
        newFacilityDTO.setCode("FAC003");
        newFacilityDTO.setName("Patras Clinic");
        newFacilityDTO.setType("Clinic");
        newFacilityDTO.setRegion("Western Greece");
        newFacilityDTO.setDistrict("Patras");
        newFacilityDTO.setActive(true);

        // Mock the repository to check code doesn't exist
        when(facilityRepository.findByCode("FAC003")).thenReturn(Optional.empty());

        // Mock the save operation to return facility with ID
        when(facilityRepository.save(any(Facility.class))).thenAnswer(invocation -> {
            Facility savedFacility = invocation.getArgument(0);
            savedFacility.setId(3L);
            savedFacility.setCreatedAt(LocalDateTime.now());
            savedFacility.setUpdatedAt(LocalDateTime.now());
            return savedFacility;
        });

        // Act
        FacilityDTO result = facilityService.createFacility(newFacilityDTO);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getId(), "ID should be generated");
        assertEquals("FAC003", result.getCode(), "Code should match");
        assertEquals("Patras Clinic", result.getName(), "Name should match");

        // Verify interactions
        verify(facilityRepository, times(1)).findByCode("FAC003");
        verify(facilityRepository, times(1)).save(any(Facility.class));
    }

    /**
     * Test: Create facility with duplicate code
     * Expected: Throws IllegalArgumentException
     */
    @Test
    @DisplayName("createFacility - Should throw exception when code already exists")
    void createFacility_ShouldThrowException_WhenCodeExists() {
        // Arrange
        FacilityDTO duplicateFacilityDTO = new FacilityDTO();
        duplicateFacilityDTO.setCode("FAC001"); // Duplicate code
        duplicateFacilityDTO.setName("Duplicate Hospital");
        duplicateFacilityDTO.setType("Hospital");
        duplicateFacilityDTO.setRegion("Attica");
        duplicateFacilityDTO.setActive(true);

        // Mock repository to return existing facility with same code
        when(facilityRepository.findByCode("FAC001")).thenReturn(Optional.of(testFacility));

        // Act & Assert - expect DuplicateResourceException
        com.healthmetrics.tracker.exception.DuplicateResourceException exception = assertThrows(
                com.healthmetrics.tracker.exception.DuplicateResourceException.class,
                () -> facilityService.createFacility(duplicateFacilityDTO),
                "Should throw DuplicateResourceException"
        );

        assertTrue(exception.getMessage().contains("already exists"),
                "Exception message should mention duplicate");

        // Verify save was never called
        verify(facilityRepository, times(1)).findByCode("FAC001");
        verify(facilityRepository, never()).save(any(Facility.class));
    }

    // ==================== UPDATE FACILITY TESTS ====================

    /**
     * Test: Update existing facility successfully
     * Expected: Returns updated facility DTO
     */
    @Test
    @DisplayName("updateFacility - Should update and return facility when ID exists")
    void updateFacility_ShouldUpdateFacility_WhenIdExists() {
        // Arrange
        Long facilityId = 1L;
        FacilityDTO updateDTO = new FacilityDTO();
        updateDTO.setCode("FAC001");
        updateDTO.setName("Athens General Hospital - Updated");
        updateDTO.setType("Hospital");
        updateDTO.setRegion("Attica");
        updateDTO.setDistrict("Athens Central");
        updateDTO.setActive(true);

        // Mock repository calls
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(testFacility));
        when(facilityRepository.save(any(Facility.class))).thenReturn(testFacility);

        // Act
        FacilityDTO result = facilityService.updateFacility(facilityId, updateDTO);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(facilityId, result.getId(), "ID should remain the same");

        // Verify interactions
        verify(facilityRepository, times(1)).findById(facilityId);
        verify(facilityRepository, times(1)).save(any(Facility.class));
    }

    /**
     * Test: Update non-existent facility
     * Expected: Throws ResourceNotFoundException
     */
    @Test
    @DisplayName("updateFacility - Should throw exception when ID doesn't exist")
    void updateFacility_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        Long nonExistentId = 999L;
        FacilityDTO updateDTO = new FacilityDTO();
        updateDTO.setName("Non-existent Hospital");

        when(facilityRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> facilityService.updateFacility(nonExistentId, updateDTO),
                "Should throw ResourceNotFoundException"
        );

        verify(facilityRepository, times(1)).findById(nonExistentId);
        verify(facilityRepository, never()).save(any(Facility.class));
    }

    // ==================== DELETE FACILITY TESTS ====================

    /**
     * Test: Delete facility successfully
     * Expected: Facility is deleted
     */
    @Test
    @DisplayName("deleteFacility - Should delete facility when ID exists")
    void deleteFacility_ShouldDeleteFacility_WhenIdExists() {
        // Arrange
        Long facilityId = 1L;

        // Mock existsById to return true (facility exists)
        when(facilityRepository.existsById(facilityId)).thenReturn(true);
        doNothing().when(facilityRepository).deleteById(facilityId);

        // Act
        facilityService.deleteFacility(facilityId);

        // Assert - verify the methods were called
        verify(facilityRepository, times(1)).existsById(facilityId);
        verify(facilityRepository, times(1)).deleteById(facilityId);
    }

    /**
     * Test: Delete non-existent facility
     * Expected: Throws ResourceNotFoundException
     */
    @Test
    @DisplayName("deleteFacility - Should throw exception when ID doesn't exist")
    void deleteFacility_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        Long nonExistentId = 999L;

        // Mock existsById to return false (facility doesn't exist)
        when(facilityRepository.existsById(nonExistentId)).thenReturn(false);

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> facilityService.deleteFacility(nonExistentId),
                "Should throw ResourceNotFoundException"
        );

        // Verify existsById was called, but deleteById was NOT called
        verify(facilityRepository, times(1)).existsById(nonExistentId);
        verify(facilityRepository, never()).deleteById(anyLong());
    }

    // ==================== SEARCH FACILITIES TESTS ====================

    /**
     * Test: Search facilities with filters
     * Expected: Returns filtered list of facilities
     */
    @Test
    @DisplayName("searchFacilities - Should return filtered facilities")
    void searchFacilities_ShouldReturnFilteredResults() {
        // Arrange
        String region = "Attica";
        String type = "Hospital";
        Boolean active = true;
        String search = null;

        List<Facility> filteredFacilities = Arrays.asList(testFacility);
        when(facilityRepository.searchWithFilters(region, type, active, search))
                .thenReturn(filteredFacilities);

        // Act
        List<FacilityDTO> result = facilityService.searchFacilities(region, type, active, search);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should return 1 facility");
        assertEquals("Attica", result.get(0).getRegion(), "Region should match filter");
        assertEquals("Hospital", result.get(0).getType(), "Type should match filter");

        verify(facilityRepository, times(1)).searchWithFilters(region, type, active, search);
    }

    /**
     * Test: Search facilities with pagination
     * Expected: Returns paginated results
     */
    @Test
    @DisplayName("searchFacilitiesWithPagination - Should return paginated results")
    void searchFacilitiesWithPagination_ShouldReturnPagedResults() {
        // Arrange
        String region = "Attica";
        String type = null;
        Boolean active = true;
        String search = null;
        Pageable pageable = PageRequest.of(0, 10);

        // Create a Page object (similar to PagedList in C#)
        List<Facility> facilities = Arrays.asList(testFacility);
        Page<Facility> facilityPage = new PageImpl<>(facilities, pageable, 1);

        when(facilityRepository.searchWithFiltersPageable(region, type, active, search, pageable))
                .thenReturn(facilityPage);

        // Act
        Page<FacilityDTO> result = facilityService.searchFacilitiesWithPagination(
                region, type, active, search, pageable
        );

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.getTotalElements(), "Should have 1 total element");
        assertEquals(1, result.getTotalPages(), "Should have 1 page");
        assertEquals(1, result.getContent().size(), "Should have 1 facility in content");

        verify(facilityRepository, times(1))
                .searchWithFiltersPageable(region, type, active, search, pageable);
    }
}