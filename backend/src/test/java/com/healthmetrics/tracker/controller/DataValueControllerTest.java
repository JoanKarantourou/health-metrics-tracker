package com.healthmetrics.tracker.controller;

import com.healthmetrics.tracker.config.SecurityConfig;
import com.healthmetrics.tracker.dto.DataValueCreateRequest;
import com.healthmetrics.tracker.dto.DataValueDTO;
import com.healthmetrics.tracker.dto.FacilityDTO;
import com.healthmetrics.tracker.dto.HealthIndicatorDTO;
import com.healthmetrics.tracker.exception.ResourceNotFoundException;
import com.healthmetrics.tracker.exception.ValidationException;
import com.healthmetrics.tracker.service.DataValueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DataValueController
 * Tests the REST API endpoints without starting the full application
 *
 * @WebMvcTest - Loads only the web layer (controller) for testing
 * @Import(SecurityConfig.class) - Applies our security config (permits all requests in dev)
 */
@WebMvcTest(DataValueController.class)
@Import(SecurityConfig.class)
class DataValueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DataValueService dataValueService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    // Reusable test objects
    private FacilityDTO testFacility;
    private HealthIndicatorDTO testIndicator;
    private DataValueDTO testDataValueDTO;
    private DataValueCreateRequest testCreateRequest;

    /**
     * Set up reusable test data before each test
     */
    @BeforeEach
    void setUp() {
        // Sample nested facility (included in DataValueDTO responses)
        testFacility = new FacilityDTO(
                1L, "FAC001", "Athens General Hospital", "Hospital",
                "Attica", "Athens", null, null, true, null, null
        );

        // Sample nested indicator (included in DataValueDTO responses)
        testIndicator = new HealthIndicatorDTO(
                1L, "IND001", "Malaria Cases", "Number of confirmed malaria cases",
                "Disease", "NUMBER", "cases", true, null, null
        );

        // Sample data value DTO (used in GET responses)
        testDataValueDTO = new DataValueDTO(
                1L, testFacility, testIndicator,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                "MONTHLY", new BigDecimal("50"), "Test data",
                null, null, "system"
        );

        // Sample create request (used in POST requests)
        testCreateRequest = new DataValueCreateRequest(
                1L, 1L,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28),
                "MONTHLY", new BigDecimal("75"), "New test data", "system"
        );
    }

    // ==================== SUBMIT DATA VALUE ====================

    /**
     * Test: POST /api/data-values
     * Expected: Returns 201 Created with created data value
     */
    @Test
    void shouldSubmitDataValue() throws Exception {
        // Arrange
        DataValueDTO createdDTO = new DataValueDTO(
                2L, testFacility, testIndicator,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28),
                "MONTHLY", new BigDecimal("75"), "New test data",
                null, null, "system"
        );

        when(dataValueService.submitDataValue(any(DataValueCreateRequest.class)))
                .thenReturn(createdDTO);

        // Act & Assert
        mockMvc.perform(post("/api/data-values")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.value").value(75))
                .andExpect(jsonPath("$.periodType").value("MONTHLY"));

        verify(dataValueService, times(1)).submitDataValue(any(DataValueCreateRequest.class));
    }

    /**
     * Test: POST /api/data-values - validation failure
     * Expected: Returns 400 Bad Request when service throws ValidationException
     */
    @Test
    void shouldReturn400WhenSubmitValidationFails() throws Exception {
        // Arrange
        when(dataValueService.submitDataValue(any(DataValueCreateRequest.class)))
                .thenThrow(new ValidationException("Period end date cannot be before period start date"));

        // Act & Assert
        mockMvc.perform(post("/api/data-values")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateRequest)))
                .andExpect(status().isBadRequest());

        verify(dataValueService, times(1)).submitDataValue(any(DataValueCreateRequest.class));
    }

    /**
     * Test: POST /api/data-values - facility not found
     * Expected: Returns 404 Not Found when facility doesn't exist
     */
    @Test
    void shouldReturn404WhenFacilityNotFoundOnSubmit() throws Exception {
        // Arrange
        when(dataValueService.submitDataValue(any(DataValueCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Facility not found with id: 1"));

        // Act & Assert
        mockMvc.perform(post("/api/data-values")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateRequest)))
                .andExpect(status().isNotFound());

        verify(dataValueService, times(1)).submitDataValue(any(DataValueCreateRequest.class));
    }

    // ==================== GET DATA VALUE BY ID ====================

    /**
     * Test: GET /api/data-values/{id}
     * Expected: Returns 200 OK with data value details
     */
    @Test
    void shouldReturnDataValueById() throws Exception {
        // Arrange
        when(dataValueService.getDataValueById(1L)).thenReturn(testDataValueDTO);

        // Act & Assert
        mockMvc.perform(get("/api/data-values/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.value").value(50))
                .andExpect(jsonPath("$.periodType").value("MONTHLY"))
                .andExpect(jsonPath("$.facility.code").value("FAC001"))
                .andExpect(jsonPath("$.indicator.code").value("IND001"));

        verify(dataValueService, times(1)).getDataValueById(1L);
    }

    /**
     * Test: GET /api/data-values/{id} - not found
     * Expected: Returns 404 Not Found
     */
    @Test
    void shouldReturn404WhenDataValueNotFound() throws Exception {
        // Arrange
        when(dataValueService.getDataValueById(999L))
                .thenThrow(new ResourceNotFoundException("Data value not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/data-values/999"))
                .andExpect(status().isNotFound());

        verify(dataValueService, times(1)).getDataValueById(999L);
    }

    // ==================== GET BY FACILITY ====================

    /**
     * Test: GET /api/data-values/facility/{facilityId}
     * Expected: Returns 200 OK with list of data values for that facility
     */
    @Test
    void shouldReturnDataValuesByFacility() throws Exception {
        // Arrange
        List<DataValueDTO> dataValues = Arrays.asList(testDataValueDTO);
        when(dataValueService.getDataValuesByFacility(1L)).thenReturn(dataValues);

        // Act & Assert
        mockMvc.perform(get("/api/data-values/facility/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].value").value(50))
                .andExpect(jsonPath("$[0].facility.code").value("FAC001"));

        verify(dataValueService, times(1)).getDataValuesByFacility(1L);
    }

    /**
     * Test: GET /api/data-values/facility/{facilityId} - not found
     * Expected: Returns 404 Not Found when facility doesn't exist
     */
    @Test
    void shouldReturn404WhenFacilityNotFoundOnGet() throws Exception {
        // Arrange
        when(dataValueService.getDataValuesByFacility(999L))
                .thenThrow(new ResourceNotFoundException("Facility not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/data-values/facility/999"))
                .andExpect(status().isNotFound());

        verify(dataValueService, times(1)).getDataValuesByFacility(999L);
    }

    // ==================== GET BY FACILITY AND PERIOD ====================

    /**
     * Test: GET /api/data-values/facility/{facilityId}/period
     * Expected: Returns 200 OK with date-filtered data values
     */
    @Test
    void shouldReturnDataValuesByFacilityAndPeriod() throws Exception {
        // Arrange
        List<DataValueDTO> dataValues = Arrays.asList(testDataValueDTO);
        when(dataValueService.getDataValuesByFacilityAndPeriod(
                eq(1L),
                eq(LocalDate.of(2026, 1, 1)),
                eq(LocalDate.of(2026, 1, 31))))
                .thenReturn(dataValues);

        // Act & Assert
        mockMvc.perform(get("/api/data-values/facility/1/period")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].periodType").value("MONTHLY"));

        verify(dataValueService, times(1))
                .getDataValuesByFacilityAndPeriod(
                        eq(1L),
                        eq(LocalDate.of(2026, 1, 1)),
                        eq(LocalDate.of(2026, 1, 31)));
    }

    // ==================== GET BY INDICATOR ====================

    /**
     * Test: GET /api/data-values/indicator/{indicatorId}
     * Expected: Returns 200 OK with data values for that indicator
     */
    @Test
    void shouldReturnDataValuesByIndicator() throws Exception {
        // Arrange
        List<DataValueDTO> dataValues = Arrays.asList(testDataValueDTO);
        when(dataValueService.getDataValuesByIndicator(1L)).thenReturn(dataValues);

        // Act & Assert
        mockMvc.perform(get("/api/data-values/indicator/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].indicator.code").value("IND001"));

        verify(dataValueService, times(1)).getDataValuesByIndicator(1L);
    }

    // ==================== AGGREGATION ENDPOINTS ====================

    /**
     * Test: GET /api/data-values/aggregate/region
     * Expected: Returns 200 OK with aggregated values by region
     */
    @Test
    void shouldReturnAggregatedDataByRegion() throws Exception {
        // Arrange
        Map<String, BigDecimal> aggregatedData = new HashMap<>();
        aggregatedData.put("Attica", new BigDecimal("250"));
        aggregatedData.put("Central Macedonia", new BigDecimal("180"));

        when(dataValueService.aggregateByRegion(
                eq(1L), any(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(aggregatedData);

        // Act & Assert
        mockMvc.perform(get("/api/data-values/aggregate/region")
                        .param("indicatorId", "1")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Attica").value(250))
                .andExpect(jsonPath("$.['Central Macedonia']").value(180));

        verify(dataValueService, times(1))
                .aggregateByRegion(eq(1L), any(), any(LocalDate.class), any(LocalDate.class));
    }

    /**
     * Test: GET /api/data-values/total
     * Expected: Returns 200 OK with total value
     */
    @Test
    void shouldReturnTotalForIndicator() throws Exception {
        // Arrange
        when(dataValueService.getTotalForIndicator(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("430"));

        // Act & Assert
        mockMvc.perform(get("/api/data-values/total")
                        .param("indicatorId", "1")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(430));

        verify(dataValueService, times(1))
                .getTotalForIndicator(eq(1L), any(LocalDate.class), any(LocalDate.class));
    }

    /**
     * Test: GET /api/data-values/average
     * Expected: Returns 200 OK with average value
     */
    @Test
    void shouldReturnAverageForIndicator() throws Exception {
        // Arrange
        when(dataValueService.getAverageForIndicator(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("67.25"));

        // Act & Assert
        mockMvc.perform(get("/api/data-values/average")
                        .param("indicatorId", "1")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(67.25));

        verify(dataValueService, times(1))
                .getAverageForIndicator(eq(1L), any(LocalDate.class), any(LocalDate.class));
    }

    // ==================== DELETE DATA VALUE ====================

    /**
     * Test: DELETE /api/data-values/{id}
     * Expected: Returns 204 No Content
     */
    @Test
    void shouldDeleteDataValue() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/data-values/1"))
                .andExpect(status().isNoContent());

        verify(dataValueService, times(1)).deleteDataValue(1L);
    }

    /**
     * Test: DELETE /api/data-values/{id} - not found
     * Expected: Returns 404 Not Found
     */
    @Test
    void shouldReturn404WhenDeletingNonExistentDataValue() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Data value not found with id: 999"))
                .when(dataValueService).deleteDataValue(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/data-values/999"))
                .andExpect(status().isNotFound());

        verify(dataValueService, times(1)).deleteDataValue(999L);
    }
}