package com.healthmetrics.tracker.controller;

import com.healthmetrics.tracker.config.SecurityConfig;
import com.healthmetrics.tracker.dto.HealthIndicatorDTO;
import com.healthmetrics.tracker.service.HealthIndicatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for HealthIndicatorController
 * Tests the REST API endpoints without starting the full application
 *
 * @WebMvcTest - Loads only the web layer (controller) for testing
 * @Import(SecurityConfig.class) - Applies our security config (permits all requests in dev)
 */
@WebMvcTest(HealthIndicatorController.class)
@Import(SecurityConfig.class)
class HealthIndicatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthIndicatorService indicatorService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET ALL INDICATORS ====================

    /**
     * Test: GET /api/indicators
     * Expected: Returns 200 OK with list of all indicators
     */
    @Test
    void shouldReturnAllIndicators() throws Exception {
        // Arrange
        HealthIndicatorDTO indicator1 = new HealthIndicatorDTO(
                1L, "IND001", "Malaria Cases", "Number of confirmed malaria cases",
                "Disease", "NUMBER", "cases", true, null, null
        );
        HealthIndicatorDTO indicator2 = new HealthIndicatorDTO(
                2L, "IND002", "Vaccination Coverage", "Percentage of vaccinated population",
                "Child Health", "PERCENTAGE", "%", true, null, null
        );

        List<HealthIndicatorDTO> indicators = Arrays.asList(indicator1, indicator2);
        when(indicatorService.getAllIndicators()).thenReturn(indicators);

        // Act & Assert
        mockMvc.perform(get("/api/indicators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("IND001"))
                .andExpect(jsonPath("$[0].name").value("Malaria Cases"))
                .andExpect(jsonPath("$[1].code").value("IND002"))
                .andExpect(jsonPath("$.length()").value(2));

        verify(indicatorService, times(1)).getAllIndicators();
    }

    /**
     * Test: GET /api/indicators - empty list
     * Expected: Returns 200 OK with empty array
     */
    @Test
    void shouldReturnEmptyListWhenNoIndicators() throws Exception {
        // Arrange
        when(indicatorService.getAllIndicators()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/indicators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================== GET INDICATOR BY ID ====================

    /**
     * Test: GET /api/indicators/{id}
     * Expected: Returns 200 OK with indicator details
     */
    @Test
    void shouldReturnIndicatorById() throws Exception {
        // Arrange
        HealthIndicatorDTO indicator = new HealthIndicatorDTO(
                1L, "IND001", "Malaria Cases", "Number of confirmed malaria cases",
                "Disease", "NUMBER", "cases", true, null, null
        );
        when(indicatorService.getIndicatorById(1L)).thenReturn(indicator);

        // Act & Assert
        mockMvc.perform(get("/api/indicators/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("IND001"))
                .andExpect(jsonPath("$.name").value("Malaria Cases"))
                .andExpect(jsonPath("$.category").value("Disease"))
                .andExpect(jsonPath("$.dataType").value("NUMBER"));

        verify(indicatorService, times(1)).getIndicatorById(1L);
    }

    /**
     * Test: GET /api/indicators/{id} - not found
     * Expected: Returns 404 Not Found
     */
    @Test
    void shouldReturn404WhenIndicatorNotFound() throws Exception {
        // Arrange
        when(indicatorService.getIndicatorById(999L))
                .thenThrow(new com.healthmetrics.tracker.exception.ResourceNotFoundException(
                        "Health indicator not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/indicators/999"))
                .andExpect(status().isNotFound());

        verify(indicatorService, times(1)).getIndicatorById(999L);
    }

    // ==================== GET BY CODE ====================

    /**
     * Test: GET /api/indicators/code/{code}
     * Expected: Returns 200 OK with matching indicator
     */
    @Test
    void shouldReturnIndicatorByCode() throws Exception {
        // Arrange
        HealthIndicatorDTO indicator = new HealthIndicatorDTO(
                1L, "IND001", "Malaria Cases", "Number of confirmed malaria cases",
                "Disease", "NUMBER", "cases", true, null, null
        );
        when(indicatorService.getIndicatorByCode("IND001")).thenReturn(indicator);

        // Act & Assert
        mockMvc.perform(get("/api/indicators/code/IND001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("IND001"))
                .andExpect(jsonPath("$.name").value("Malaria Cases"));

        verify(indicatorService, times(1)).getIndicatorByCode("IND001");
    }

    // ==================== GET BY CATEGORY ====================

    /**
     * Test: GET /api/indicators/category/{category}
     * Expected: Returns 200 OK with indicators in that category
     */
    @Test
    void shouldReturnIndicatorsByCategory() throws Exception {
        // Arrange
        HealthIndicatorDTO indicator = new HealthIndicatorDTO(
                1L, "IND001", "Malaria Cases", "Number of confirmed malaria cases",
                "Disease", "NUMBER", "cases", true, null, null
        );
        when(indicatorService.getIndicatorsByCategory("Disease"))
                .thenReturn(Arrays.asList(indicator));

        // Act & Assert
        mockMvc.perform(get("/api/indicators/category/Disease"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Disease"));

        verify(indicatorService, times(1)).getIndicatorsByCategory("Disease");
    }

    // ==================== GET BY DATA TYPE ====================

    /**
     * Test: GET /api/indicators/data-type/{dataType}
     * Expected: Returns 200 OK with indicators of that data type
     */
    @Test
    void shouldReturnIndicatorsByDataType() throws Exception {
        // Arrange
        HealthIndicatorDTO indicator = new HealthIndicatorDTO(
                1L, "IND001", "Malaria Cases", "Number of confirmed malaria cases",
                "Disease", "NUMBER", "cases", true, null, null
        );
        when(indicatorService.getIndicatorsByDataType("NUMBER"))
                .thenReturn(Arrays.asList(indicator));

        // Act & Assert
        mockMvc.perform(get("/api/indicators/data-type/NUMBER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].dataType").value("NUMBER"));

        verify(indicatorService, times(1)).getIndicatorsByDataType("NUMBER");
    }

    // ==================== GET ACTIVE INDICATORS ====================

    /**
     * Test: GET /api/indicators/active
     * Expected: Returns 200 OK with only active indicators
     */
    @Test
    void shouldReturnActiveIndicators() throws Exception {
        // Arrange
        HealthIndicatorDTO indicator = new HealthIndicatorDTO(
                1L, "IND001", "Malaria Cases", "Number of confirmed malaria cases",
                "Disease", "NUMBER", "cases", true, null, null
        );
        when(indicatorService.getActiveIndicators()).thenReturn(Arrays.asList(indicator));

        // Act & Assert
        mockMvc.perform(get("/api/indicators/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true));

        verify(indicatorService, times(1)).getActiveIndicators();
    }

    // ==================== CREATE INDICATOR ====================

    /**
     * Test: POST /api/indicators
     * Expected: Returns 201 Created with the created indicator
     */
    @Test
    void shouldCreateIndicator() throws Exception {
        // Arrange - new indicator without ID
        HealthIndicatorDTO newIndicator = new HealthIndicatorDTO(
                null, "IND003", "Diabetes Cases", "Number of diagnosed diabetes cases",
                "Disease", "NUMBER", "cases", true, null, null
        );

        // Saved indicator with ID assigned by database
        HealthIndicatorDTO savedIndicator = new HealthIndicatorDTO(
                3L, "IND003", "Diabetes Cases", "Number of diagnosed diabetes cases",
                "Disease", "NUMBER", "cases", true, null, null
        );

        when(indicatorService.createIndicator(any(HealthIndicatorDTO.class)))
                .thenReturn(savedIndicator);

        // Act & Assert
        mockMvc.perform(post("/api/indicators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newIndicator)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.code").value("IND003"))
                .andExpect(jsonPath("$.name").value("Diabetes Cases"));

        verify(indicatorService, times(1)).createIndicator(any(HealthIndicatorDTO.class));
    }

    // ==================== UPDATE INDICATOR ====================

    /**
     * Test: PUT /api/indicators/{id}
     * Expected: Returns 200 OK with updated indicator
     */
    @Test
    void shouldUpdateIndicator() throws Exception {
        // Arrange
        HealthIndicatorDTO updatedIndicator = new HealthIndicatorDTO(
                1L, "IND001", "Malaria Cases - Updated", "Updated description",
                "Disease", "NUMBER", "cases", true, null, null
        );

        when(indicatorService.updateIndicator(eq(1L), any(HealthIndicatorDTO.class)))
                .thenReturn(updatedIndicator);

        // Act & Assert
        mockMvc.perform(put("/api/indicators/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedIndicator)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Malaria Cases - Updated"));

        verify(indicatorService, times(1)).updateIndicator(eq(1L), any(HealthIndicatorDTO.class));
    }

    /**
     * Test: PUT /api/indicators/{id} - not found
     * Expected: Returns 404 Not Found
     */
    @Test
    void shouldReturn404WhenUpdatingNonExistentIndicator() throws Exception {
        // Arrange
        HealthIndicatorDTO updateDTO = new HealthIndicatorDTO(
                999L, "IND999", "Non-existent", "Non-existent",
                "Disease", "NUMBER", "cases", true, null, null
        );

        when(indicatorService.updateIndicator(eq(999L), any(HealthIndicatorDTO.class)))
                .thenThrow(new com.healthmetrics.tracker.exception.ResourceNotFoundException(
                        "Health indicator not found with id: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/indicators/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(indicatorService, times(1)).updateIndicator(eq(999L), any(HealthIndicatorDTO.class));
    }

    // ==================== DELETE INDICATOR ====================

    /**
     * Test: DELETE /api/indicators/{id}
     * Expected: Returns 204 No Content
     */
    @Test
    void shouldDeleteIndicator() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/indicators/1"))
                .andExpect(status().isNoContent());

        verify(indicatorService, times(1)).deleteIndicator(1L);
    }

    /**
     * Test: DELETE /api/indicators/{id} - not found
     * Expected: Returns 404 Not Found
     */
    @Test
    void shouldReturn404WhenDeletingNonExistentIndicator() throws Exception {
        // Arrange
        doThrow(new com.healthmetrics.tracker.exception.ResourceNotFoundException(
                "Health indicator not found with id: 999"))
                .when(indicatorService).deleteIndicator(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/indicators/999"))
                .andExpect(status().isNotFound());

        verify(indicatorService, times(1)).deleteIndicator(999L);
    }
}