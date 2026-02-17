package com.healthmetrics.tracker.service;

import com.healthmetrics.tracker.dto.HealthIndicatorDTO;
import com.healthmetrics.tracker.entity.HealthIndicator;
import com.healthmetrics.tracker.exception.ResourceNotFoundException;
import com.healthmetrics.tracker.repository.HealthIndicatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HealthIndicatorService
 *
 * Tests all CRUD operations and business logic for health indicators.
 * Uses Mockito to mock the repository layer and isolate service logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HealthIndicatorService Unit Tests")
class HealthIndicatorServiceTest {

    // Mock the repository
    @Mock
    private HealthIndicatorRepository healthIndicatorRepository;

    // Inject mocked repository into service
    @InjectMocks
    private HealthIndicatorService healthIndicatorService;

    // Test data
    private HealthIndicator testIndicator;
    private HealthIndicatorDTO testIndicatorDTO;

    /**
     * Set up test data before each test
     * Creates sample health indicator objects for testing
     */
    @BeforeEach
    void setUp() {
        // Create a sample health indicator entity
        testIndicator = new HealthIndicator();
        testIndicator.setId(1L);
        testIndicator.setCode("IND001");
        testIndicator.setName("Malaria Cases");
        testIndicator.setDescription("Number of confirmed malaria cases");
        testIndicator.setCategory("Disease");
        testIndicator.setDataType("NUMBER");
        testIndicator.setUnit("cases");
        testIndicator.setActive(true);

        // Create a sample health indicator DTO
        testIndicatorDTO = new HealthIndicatorDTO();
        testIndicatorDTO.setId(1L);
        testIndicatorDTO.setCode("IND001");
        testIndicatorDTO.setName("Malaria Cases");
        testIndicatorDTO.setDescription("Number of confirmed malaria cases");
        testIndicatorDTO.setCategory("Disease");
        testIndicatorDTO.setDataType("NUMBER");
        testIndicatorDTO.setUnit("cases");
        testIndicatorDTO.setActive(true);
    }

    // ==================== GET ALL INDICATORS TESTS ====================

    /**
     * Test: Get all health indicators successfully
     * Expected: Returns list of all indicators as DTOs
     */
    @Test
    @DisplayName("getAllIndicators - Should return list of all indicators")
    void getAllIndicators_ShouldReturnAllIndicators() {
        // Arrange - prepare mock data
        HealthIndicator indicator2 = new HealthIndicator();
        indicator2.setId(2L);
        indicator2.setCode("IND002");
        indicator2.setName("Vaccination Coverage");
        indicator2.setCategory("Child Health");
        indicator2.setDataType("PERCENTAGE");
        indicator2.setUnit("%");
        indicator2.setActive(true);

        List<HealthIndicator> mockIndicators = Arrays.asList(testIndicator, indicator2);

        // Mock the repository call
        when(healthIndicatorRepository.findAll()).thenReturn(mockIndicators);

        // Act - call the service method
        List<HealthIndicatorDTO> result = healthIndicatorService.getAllIndicators();

        // Assert - verify the results
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 indicators");
        assertEquals("IND001", result.get(0).getCode(), "First indicator code should match");
        assertEquals("IND002", result.get(1).getCode(), "Second indicator code should match");

        // Verify the repository method was called exactly once
        verify(healthIndicatorRepository, times(1)).findAll();
    }

    /**
     * Test: Get all indicators when database is empty
     * Expected: Returns empty list (not null)
     */
    @Test
    @DisplayName("getAllIndicators - Should return empty list when no indicators exist")
    void getAllIndicators_ShouldReturnEmptyList_WhenNoIndicators() {
        // Arrange
        when(healthIndicatorRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<HealthIndicatorDTO> result = healthIndicatorService.getAllIndicators();

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty list");
        verify(healthIndicatorRepository, times(1)).findAll();
    }

    // ==================== GET INDICATOR BY ID TESTS ====================

    /**
     * Test: Get indicator by ID successfully
     * Expected: Returns the indicator DTO
     */
    @Test
    @DisplayName("getIndicatorById - Should return indicator when ID exists")
    void getIndicatorById_ShouldReturnIndicator_WhenIdExists() {
        // Arrange
        Long indicatorId = 1L;
        when(healthIndicatorRepository.findById(indicatorId)).thenReturn(Optional.of(testIndicator));

        // Act
        HealthIndicatorDTO result = healthIndicatorService.getIndicatorById(indicatorId);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(indicatorId, result.getId(), "ID should match");
        assertEquals("IND001", result.getCode(), "Code should match");
        assertEquals("Malaria Cases", result.getName(), "Name should match");
        assertEquals("Disease", result.getCategory(), "Category should match");
        verify(healthIndicatorRepository, times(1)).findById(indicatorId);
    }

    /**
     * Test: Get indicator by ID when ID doesn't exist
     * Expected: Throws ResourceNotFoundException
     */
    @Test
    @DisplayName("getIndicatorById - Should throw exception when ID doesn't exist")
    void getIndicatorById_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        Long nonExistentId = 999L;
        when(healthIndicatorRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert - verify exception is thrown
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> healthIndicatorService.getIndicatorById(nonExistentId),
                "Should throw ResourceNotFoundException"
        );

        // Verify exception message
        assertTrue(exception.getMessage().contains("not found"),
                "Exception message should contain 'not found'");
        verify(healthIndicatorRepository, times(1)).findById(nonExistentId);
    }

    // ==================== CREATE INDICATOR TESTS ====================

    /**
     * Test: Create new indicator successfully
     * Expected: Returns created indicator DTO with ID
     */
    @Test
    @DisplayName("createIndicator - Should create and return new indicator")
    void createIndicator_ShouldCreateIndicator_WhenValidData() {
        // Arrange
        HealthIndicatorDTO newIndicatorDTO = new HealthIndicatorDTO();
        newIndicatorDTO.setCode("IND003");
        newIndicatorDTO.setName("Diabetes Cases");
        newIndicatorDTO.setDescription("Number of diagnosed diabetes cases");
        newIndicatorDTO.setCategory("Disease");
        newIndicatorDTO.setDataType("NUMBER");
        newIndicatorDTO.setUnit("cases");
        newIndicatorDTO.setActive(true);

        // Mock the repository to check code doesn't exist
        when(healthIndicatorRepository.findByCode("IND003")).thenReturn(Optional.empty());

        // Mock the save operation to return indicator with ID
        when(healthIndicatorRepository.save(any(HealthIndicator.class))).thenAnswer(invocation -> {
            HealthIndicator savedIndicator = invocation.getArgument(0);
            savedIndicator.setId(3L);
            return savedIndicator;
        });

        // Act
        HealthIndicatorDTO result = healthIndicatorService.createIndicator(newIndicatorDTO);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getId(), "ID should be generated");
        assertEquals("IND003", result.getCode(), "Code should match");
        assertEquals("Diabetes Cases", result.getName(), "Name should match");

        // Verify interactions
        verify(healthIndicatorRepository, times(1)).findByCode("IND003");
        verify(healthIndicatorRepository, times(1)).save(any(HealthIndicator.class));
    }

    /**
     * Test: Create indicator with duplicate code
     * Expected: Throws IllegalArgumentException
     */
    @Test
    @DisplayName("createIndicator - Should throw exception when code already exists")
    void createIndicator_ShouldThrowException_WhenCodeExists() {
        // Arrange
        HealthIndicatorDTO duplicateIndicatorDTO = new HealthIndicatorDTO();
        duplicateIndicatorDTO.setCode("IND001"); // Duplicate code
        duplicateIndicatorDTO.setName("Duplicate Indicator");
        duplicateIndicatorDTO.setCategory("Disease");
        duplicateIndicatorDTO.setDataType("NUMBER");
        duplicateIndicatorDTO.setActive(true);

        // Mock repository to return existing indicator with same code
        when(healthIndicatorRepository.findByCode("IND001")).thenReturn(Optional.of(testIndicator));

        // Act & Assert - expect DuplicateResourceException
        com.healthmetrics.tracker.exception.DuplicateResourceException exception = assertThrows(
                com.healthmetrics.tracker.exception.DuplicateResourceException.class,
                () -> healthIndicatorService.createIndicator(duplicateIndicatorDTO),
                "Should throw DuplicateResourceException"
        );

        assertTrue(exception.getMessage().contains("already exists"),
                "Exception message should mention duplicate");

        // Verify save was never called
        verify(healthIndicatorRepository, times(1)).findByCode("IND001");
        verify(healthIndicatorRepository, never()).save(any(HealthIndicator.class));
    }

    // ==================== UPDATE INDICATOR TESTS ====================

    /**
     * Test: Update existing indicator successfully
     * Expected: Returns updated indicator DTO
     */
    @Test
    @DisplayName("updateIndicator - Should update and return indicator when ID exists")
    void updateIndicator_ShouldUpdateIndicator_WhenIdExists() {
        // Arrange
        Long indicatorId = 1L;
        HealthIndicatorDTO updateDTO = new HealthIndicatorDTO();
        updateDTO.setCode("IND001");
        updateDTO.setName("Malaria Cases - Updated");
        updateDTO.setDescription("Updated description");
        updateDTO.setCategory("Disease");
        updateDTO.setDataType("NUMBER");
        updateDTO.setUnit("cases");
        updateDTO.setActive(true);

        // Mock repository calls
        when(healthIndicatorRepository.findById(indicatorId)).thenReturn(Optional.of(testIndicator));
        when(healthIndicatorRepository.save(any(HealthIndicator.class))).thenReturn(testIndicator);

        // Act
        HealthIndicatorDTO result = healthIndicatorService.updateIndicator(indicatorId, updateDTO);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(indicatorId, result.getId(), "ID should remain the same");

        // Verify interactions
        verify(healthIndicatorRepository, times(1)).findById(indicatorId);
        verify(healthIndicatorRepository, times(1)).save(any(HealthIndicator.class));
    }

    /**
     * Test: Update non-existent indicator
     * Expected: Throws ResourceNotFoundException
     */
    @Test
    @DisplayName("updateIndicator - Should throw exception when ID doesn't exist")
    void updateIndicator_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        Long nonExistentId = 999L;
        HealthIndicatorDTO updateDTO = new HealthIndicatorDTO();
        updateDTO.setName("Non-existent Indicator");

        when(healthIndicatorRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> healthIndicatorService.updateIndicator(nonExistentId, updateDTO),
                "Should throw ResourceNotFoundException"
        );

        verify(healthIndicatorRepository, times(1)).findById(nonExistentId);
        verify(healthIndicatorRepository, never()).save(any(HealthIndicator.class));
    }

    // ==================== DELETE INDICATOR TESTS ====================

    /**
     * Test: Delete indicator successfully
     * Expected: Indicator is deleted
     */
    @Test
    @DisplayName("deleteIndicator - Should delete indicator when ID exists")
    void deleteIndicator_ShouldDeleteIndicator_WhenIdExists() {
        // Arrange
        Long indicatorId = 1L;
        when(healthIndicatorRepository.existsById(indicatorId)).thenReturn(true);
        doNothing().when(healthIndicatorRepository).deleteById(indicatorId);

        // Act
        healthIndicatorService.deleteIndicator(indicatorId);

        // Assert
        verify(healthIndicatorRepository, times(1)).existsById(indicatorId);
        verify(healthIndicatorRepository, times(1)).deleteById(indicatorId);
    }

    /**
     * Test: Delete non-existent indicator
     * Expected: Throws ResourceNotFoundException
     */
    @Test
    @DisplayName("deleteIndicator - Should throw exception when ID doesn't exist")
    void deleteIndicator_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        Long nonExistentId = 999L;
        when(healthIndicatorRepository.existsById(nonExistentId)).thenReturn(false);

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> healthIndicatorService.deleteIndicator(nonExistentId),
                "Should throw ResourceNotFoundException"
        );

        verify(healthIndicatorRepository, times(1)).existsById(nonExistentId);
        verify(healthIndicatorRepository, never()).deleteById(anyLong());
    }

    // ==================== GET BY CATEGORY TESTS ====================

    /**
     * Test: Get indicators by category
     * Expected: Returns filtered list of indicators
     */
    @Test
    @DisplayName("getIndicatorsByCategory - Should return indicators for given category")
    void getIndicatorsByCategory_ShouldReturnIndicators_WhenCategoryExists() {
        // Arrange
        String category = "Disease";
        List<HealthIndicator> mockIndicators = Arrays.asList(testIndicator);
        when(healthIndicatorRepository.findByCategory(category)).thenReturn(mockIndicators);

        // Act
        List<HealthIndicatorDTO> result = healthIndicatorService.getIndicatorsByCategory(category);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should return 1 indicator");
        assertEquals("Disease", result.get(0).getCategory(), "Category should match");
        verify(healthIndicatorRepository, times(1)).findByCategory(category);
    }

    /**
     * Test: Get indicators by category when none exist
     * Expected: Returns empty list
     */
    @Test
    @DisplayName("getIndicatorsByCategory - Should return empty list when category has no indicators")
    void getIndicatorsByCategory_ShouldReturnEmptyList_WhenCategoryHasNoIndicators() {
        // Arrange
        String category = "NonExistent";
        when(healthIndicatorRepository.findByCategory(category)).thenReturn(Arrays.asList());

        // Act
        List<HealthIndicatorDTO> result = healthIndicatorService.getIndicatorsByCategory(category);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty");
        verify(healthIndicatorRepository, times(1)).findByCategory(category);
    }
}