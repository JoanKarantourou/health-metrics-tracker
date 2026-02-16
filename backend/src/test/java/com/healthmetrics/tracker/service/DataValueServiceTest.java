package com.healthmetrics.tracker.service;

import com.healthmetrics.tracker.dto.DataValueCreateRequest;
import com.healthmetrics.tracker.dto.DataValueDTO;
import com.healthmetrics.tracker.entity.DataValue;
import com.healthmetrics.tracker.entity.Facility;
import com.healthmetrics.tracker.entity.HealthIndicator;
import com.healthmetrics.tracker.exception.ResourceNotFoundException;
import com.healthmetrics.tracker.exception.ValidationException;
import com.healthmetrics.tracker.repository.DataValueRepository;
import com.healthmetrics.tracker.repository.FacilityRepository;
import com.healthmetrics.tracker.repository.HealthIndicatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DataValueService
 *
 * Tests data value submission, retrieval, and validation logic.
 * This service handles the core health metrics data with relationships
 * to facilities and indicators.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DataValueService Unit Tests")
class DataValueServiceTest {

    // Mock repositories
    @Mock
    private DataValueRepository dataValueRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private HealthIndicatorRepository healthIndicatorRepository;

    // Inject mocked repositories into service
    @InjectMocks
    private DataValueService dataValueService;

    // Test data
    private Facility testFacility;
    private HealthIndicator testIndicator;
    private DataValue testDataValue;
    private DataValueCreateRequest testCreateRequest;

    /**
     * Set up test data before each test
     * Creates sample facility, indicator, and data value objects
     */
    @BeforeEach
    void setUp() {
        // Create a sample facility
        testFacility = new Facility();
        testFacility.setId(1L);
        testFacility.setCode("FAC001");
        testFacility.setName("Athens General Hospital");
        testFacility.setType("Hospital");
        testFacility.setRegion("Attica");
        testFacility.setActive(true);

        // Create a sample health indicator
        testIndicator = new HealthIndicator();
        testIndicator.setId(1L);
        testIndicator.setCode("IND001");
        testIndicator.setName("Malaria Cases");
        testIndicator.setCategory("Disease");
        testIndicator.setDataType("NUMBER");
        testIndicator.setUnit("cases");
        testIndicator.setActive(true);

        // Create a sample data value
        testDataValue = new DataValue();
        testDataValue.setId(1L);
        testDataValue.setFacility(testFacility);
        testDataValue.setIndicator(testIndicator);
        testDataValue.setPeriodStart(LocalDate.of(2026, 1, 1));
        testDataValue.setPeriodEnd(LocalDate.of(2026, 1, 31));
        testDataValue.setPeriodType("MONTHLY");
        testDataValue.setValue(new BigDecimal("50"));
        testDataValue.setComment("Test data");

        // Create a sample create request
        testCreateRequest = new DataValueCreateRequest();
        testCreateRequest.setFacilityId(1L);
        testCreateRequest.setIndicatorId(1L);
        testCreateRequest.setPeriodStart(LocalDate.of(2026, 2, 1));
        testCreateRequest.setPeriodEnd(LocalDate.of(2026, 2, 28));
        testCreateRequest.setPeriodType("MONTHLY");
        testCreateRequest.setValue(new BigDecimal("75"));
        testCreateRequest.setComment("New test data");
    }

    // ==================== SUBMIT DATA VALUE TESTS ====================

    /**
     * Test: Submit new data value successfully
     * Expected: Returns created data value DTO with ID
     */
    @Test
    @DisplayName("submitDataValue - Should create and return new data value")
    void submitDataValue_ShouldCreateDataValue_WhenValidData() {
        // Arrange
        // Mock facility and indicator exist
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(testFacility));
        when(healthIndicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));

        // Mock no duplicate exists - returns EMPTY LIST (not Optional.empty)
        when(dataValueRepository.findByFacilityIdAndIndicatorIdAndPeriodStart(
                anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList()); // Empty list = no duplicates

        // Mock save operation
        when(dataValueRepository.save(any(DataValue.class))).thenAnswer(invocation -> {
            DataValue savedValue = invocation.getArgument(0);
            savedValue.setId(2L);
            return savedValue;
        });

        // Act
        DataValueDTO result = dataValueService.submitDataValue(testCreateRequest);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getId(), "ID should be generated");
        assertEquals(new BigDecimal("75"), result.getValue(), "Value should match");
        assertEquals("MONTHLY", result.getPeriodType(), "Period type should match");

        // Verify interactions
        verify(facilityRepository, times(1)).findById(1L);
        verify(healthIndicatorRepository, times(1)).findById(1L);
        verify(dataValueRepository, times(1)).save(any(DataValue.class));
    }

    /**
     * Test: Submit data value with non-existent facility
     * Expected: Throws ResourceNotFoundException
     */
    @Test
    @DisplayName("submitDataValue - Should throw exception when facility doesn't exist")
    void submitDataValue_ShouldThrowException_WhenFacilityNotFound() {
        // Arrange
        when(facilityRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> dataValueService.submitDataValue(testCreateRequest),
                "Should throw ResourceNotFoundException"
        );

        assertTrue(exception.getMessage().contains("Facility not found"),
                "Exception message should mention facility");
        verify(facilityRepository, times(1)).findById(1L);
        verify(dataValueRepository, never()).save(any(DataValue.class));
    }

    /**
     * Test: Submit data value with non-existent indicator
     * Expected: Throws ResourceNotFoundException
     */
    @Test
    @DisplayName("submitDataValue - Should throw exception when indicator doesn't exist")
    void submitDataValue_ShouldThrowException_WhenIndicatorNotFound() {
        // Arrange
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(testFacility));
        when(healthIndicatorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> dataValueService.submitDataValue(testCreateRequest),
                "Should throw ResourceNotFoundException"
        );

        assertTrue(exception.getMessage().contains("not found"),
                "Exception message should mention not found");
        verify(healthIndicatorRepository, times(1)).findById(1L);
        verify(dataValueRepository, never()).save(any(DataValue.class));
    }

    /**
     * Test: Submit duplicate data value
     * Expected: Throws ValidationException
     */
    @Test
    @DisplayName("submitDataValue - Should throw exception when duplicate exists")
    void submitDataValue_ShouldThrowException_WhenDuplicateExists() {
        // Arrange
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(testFacility));
        when(healthIndicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));

        // Mock duplicate exists - returns LIST with one item (not Optional.of)
        when(dataValueRepository.findByFacilityIdAndIndicatorIdAndPeriodStart(
                anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList(testDataValue)); // List with data = duplicate exists

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dataValueService.submitDataValue(testCreateRequest),
                "Should throw ValidationException"
        );

        assertTrue(exception.getMessage().contains("already exists"),
                "Exception message should mention duplicate");
        verify(dataValueRepository, never()).save(any(DataValue.class));
    }

    /**
     * Test: Submit data value with inactive facility
     * Expected: Throws ValidationException
     */
    @Test
    @DisplayName("submitDataValue - Should throw exception when facility is inactive")
    void submitDataValue_ShouldThrowException_WhenFacilityInactive() {
        // Arrange
        testFacility.setActive(false); // Make facility inactive
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(testFacility));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dataValueService.submitDataValue(testCreateRequest),
                "Should throw ValidationException"
        );

        assertTrue(exception.getMessage().contains("inactive"),
                "Exception message should mention inactive status");
        verify(dataValueRepository, never()).save(any(DataValue.class));
    }

    /**
     * Test: Submit data value with inactive indicator
     * Expected: Throws ValidationException
     */
    @Test
    @DisplayName("submitDataValue - Should throw exception when indicator is inactive")
    void submitDataValue_ShouldThrowException_WhenIndicatorInactive() {
        // Arrange
        testIndicator.setActive(false); // Make indicator inactive
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(testFacility));
        when(healthIndicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dataValueService.submitDataValue(testCreateRequest),
                "Should throw ValidationException"
        );

        assertTrue(exception.getMessage().contains("inactive"),
                "Exception message should mention inactive indicator");
        verify(dataValueRepository, never()).save(any(DataValue.class));
    }

    /**
     * Test: Submit data value with negative value
     * Expected: Throws ValidationException
     */
    @Test
    @DisplayName("submitDataValue - Should throw exception when value is negative")
    void submitDataValue_ShouldThrowException_WhenValueIsNegative() {
        // Arrange
        testCreateRequest.setValue(new BigDecimal("-10")); // Negative value
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(testFacility));
        when(healthIndicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));
        when(dataValueRepository.findByFacilityIdAndIndicatorIdAndPeriodStart(
                anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> dataValueService.submitDataValue(testCreateRequest),
                "Should throw ValidationException"
        );

        assertTrue(exception.getMessage().contains("negative"),
                "Exception message should mention negative value");
        verify(dataValueRepository, never()).save(any(DataValue.class));
    }

    // ==================== GET DATA VALUES TESTS ====================

    /**
     * Test: Get data values by facility ID
     * Expected: Returns list of data values for that facility
     */
    @Test
    @DisplayName("getDataValuesByFacility - Should return data values for given facility")
    void getDataValuesByFacility_ShouldReturnDataValues_WhenFacilityExists() {
        // Arrange
        Long facilityId = 1L;
        List<DataValue> mockDataValues = Arrays.asList(testDataValue);

        // Mock facility exists (uses existsById, not findById)
        when(facilityRepository.existsById(facilityId)).thenReturn(true);
        when(dataValueRepository.findByFacilityId(facilityId)).thenReturn(mockDataValues);

        // Act
        List<DataValueDTO> result = dataValueService.getDataValuesByFacility(facilityId);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should return 1 data value");
        verify(facilityRepository, times(1)).existsById(facilityId);
        verify(dataValueRepository, times(1)).findByFacilityId(facilityId);
    }

    /**
     * Test: Get data values by facility when facility doesn't exist
     * Expected: Throws ResourceNotFoundException
     */
    @Test
    @DisplayName("getDataValuesByFacility - Should throw exception when facility doesn't exist")
    void getDataValuesByFacility_ShouldThrowException_WhenFacilityNotFound() {
        // Arrange
        Long facilityId = 999L;
        when(facilityRepository.existsById(facilityId)).thenReturn(false);

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> dataValueService.getDataValuesByFacility(facilityId),
                "Should throw ResourceNotFoundException"
        );

        verify(facilityRepository, times(1)).existsById(facilityId);
        verify(dataValueRepository, never()).findByFacilityId(anyLong());
    }

    /**
     * Test: Get data values by facility when none exist
     * Expected: Returns empty list
     */
    @Test
    @DisplayName("getDataValuesByFacility - Should return empty list when no data exists")
    void getDataValuesByFacility_ShouldReturnEmptyList_WhenNoDataExists() {
        // Arrange
        Long facilityId = 1L;
        when(facilityRepository.existsById(facilityId)).thenReturn(true);
        when(dataValueRepository.findByFacilityId(facilityId)).thenReturn(Arrays.asList());

        // Act
        List<DataValueDTO> result = dataValueService.getDataValuesByFacility(facilityId);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty");
        verify(facilityRepository, times(1)).existsById(facilityId);
        verify(dataValueRepository, times(1)).findByFacilityId(facilityId);
    }

    /**
     * Test: Get data values by indicator ID
     * Expected: Returns list of data values for that indicator
     */
    @Test
    @DisplayName("getDataValuesByIndicator - Should return data values for given indicator")
    void getDataValuesByIndicator_ShouldReturnDataValues_WhenIndicatorExists() {
        // Arrange
        Long indicatorId = 1L;
        List<DataValue> mockDataValues = Arrays.asList(testDataValue);

        // Mock indicator exists (uses existsById, not findById)
        when(healthIndicatorRepository.existsById(indicatorId)).thenReturn(true);
        when(dataValueRepository.findByIndicatorId(indicatorId)).thenReturn(mockDataValues);

        // Act
        List<DataValueDTO> result = dataValueService.getDataValuesByIndicator(indicatorId);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.size(), "Should return 1 data value");
        verify(healthIndicatorRepository, times(1)).existsById(indicatorId);
        verify(dataValueRepository, times(1)).findByIndicatorId(indicatorId);
    }

    /**
     * Test: Get data values by indicator when indicator doesn't exist
     * Expected: Throws ResourceNotFoundException
     */
    @Test
    @DisplayName("getDataValuesByIndicator - Should throw exception when indicator doesn't exist")
    void getDataValuesByIndicator_ShouldThrowException_WhenIndicatorNotFound() {
        // Arrange
        Long indicatorId = 999L;
        when(healthIndicatorRepository.existsById(indicatorId)).thenReturn(false);

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> dataValueService.getDataValuesByIndicator(indicatorId),
                "Should throw ResourceNotFoundException"
        );

        verify(healthIndicatorRepository, times(1)).existsById(indicatorId);
        verify(dataValueRepository, never()).findByIndicatorId(anyLong());
    }

    /**
     * Test: Delete data value successfully
     * Expected: Data value is deleted
     */
    @Test
    @DisplayName("deleteDataValue - Should delete data value when ID exists")
    void deleteDataValue_ShouldDeleteDataValue_WhenIdExists() {
        // Arrange
        Long dataValueId = 1L;
        when(dataValueRepository.existsById(dataValueId)).thenReturn(true);
        doNothing().when(dataValueRepository).deleteById(dataValueId);

        // Act
        dataValueService.deleteDataValue(dataValueId);

        // Assert
        verify(dataValueRepository, times(1)).existsById(dataValueId);
        verify(dataValueRepository, times(1)).deleteById(dataValueId);
    }

    /**
     * Test: Delete non-existent data value
     * Expected: Throws ResourceNotFoundException
     */
    @Test
    @DisplayName("deleteDataValue - Should throw exception when ID doesn't exist")
    void deleteDataValue_ShouldThrowException_WhenIdDoesNotExist() {
        // Arrange
        Long nonExistentId = 999L;
        when(dataValueRepository.existsById(nonExistentId)).thenReturn(false);

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> dataValueService.deleteDataValue(nonExistentId),
                "Should throw ResourceNotFoundException"
        );

        verify(dataValueRepository, times(1)).existsById(nonExistentId);
        verify(dataValueRepository, never()).deleteById(anyLong());
    }
}