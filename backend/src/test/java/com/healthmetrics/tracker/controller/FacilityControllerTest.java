package com.healthmetrics.tracker.controller;

import com.healthmetrics.tracker.dto.FacilityDTO;
import com.healthmetrics.tracker.service.FacilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import com.healthmetrics.tracker.config.SecurityConfig;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for FacilityController
 * Tests the REST API endpoints without starting the full application
 *
 * Similar to ASP.NET Core's WebApplicationFactory or TestServer for controller testing
 *
 * @WebMvcTest - Loads only the web layer (controller) for testing, not the full app context
 * This is faster than @SpringBootTest because it doesn't load the database, services, etc.
 */
@WebMvcTest(FacilityController.class)
@Import(SecurityConfig.class)
class FacilityControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simulates HTTP requests (like HttpClient in .NET integration tests)

    @MockitoBean
    private FacilityService facilityService; // Mock service (like Mock<IFacilityService> in Moq)

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext; // Mock JPA context for @EnableJpaAuditing

    @Autowired
    private ObjectMapper objectMapper; // Converts objects to/from JSON (like System.Text.Json or Newtonsoft.Json)

    /**
     * Test: GET /api/facilities
     * Verifies that getAllFacilities returns a paginated list of facilities
     *
     * Similar to testing a GET endpoint in ASP.NET Core:
     * var response = await client.GetAsync("/api/facilities");
     * response.StatusCode.Should().Be(HttpStatusCode.OK);
     */
    @Test
    void shouldReturnAllFacilities() throws Exception {
        // Arrange - Create test data (like the "Arrange" section in AAA pattern)
        FacilityDTO facility1 = new FacilityDTO(
                1L, "FAC001", "Athens General Hospital", "Hospital",
                "Attica", "Athens", null, null, true, null, null
        );
        FacilityDTO facility2 = new FacilityDTO(
                2L, "FAC002", "Thessaloniki Health Center", "Health Center",
                "Central Macedonia", "Thessaloniki", null, null, true, null, null
        );

        List<FacilityDTO> facilities = Arrays.asList(facility1, facility2);
        Page<FacilityDTO> page = new PageImpl<>(facilities, PageRequest.of(0, 10), 2);

        // Mock the service method - when this is called, return our test data
        // Similar to: mock.Setup(x => x.SearchFacilities(...)).Returns(page);
        when(facilityService.searchFacilitiesWithPagination(
                any(), any(), any(), any(), any(Pageable.class)
        )).thenReturn(page);

        // Act & Assert - Perform request and verify response
        mockMvc.perform(get("/api/facilities")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk()) // HTTP 200
                .andExpect(jsonPath("$.content").isArray()) // Check JSON structure
                .andExpect(jsonPath("$.content[0].code").value("FAC001"))
                .andExpect(jsonPath("$.content[1].code").value("FAC002"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    /**
     * Test: GET /api/facilities/{id}
     * Verifies that getting a facility by ID returns the correct data
     */
    @Test
    void shouldReturnFacilityById() throws Exception {
        // Arrange
        FacilityDTO facility = new FacilityDTO(
                1L, "FAC001", "Athens General Hospital", "Hospital",
                "Attica", "Athens", 37.9838, 23.7275, true, null, null
        );

        // Mock: When getFacilityById(1) is called, return our test facility
        when(facilityService.getFacilityById(1L)).thenReturn(facility);

        // Act & Assert
        mockMvc.perform(get("/api/facilities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("FAC001"))
                .andExpect(jsonPath("$.name").value("Athens General Hospital"))
                .andExpect(jsonPath("$.type").value("Hospital"))
                .andExpect(jsonPath("$.region").value("Attica"));
    }

    /**
     * Test: POST /api/facilities
     * Verifies that creating a new facility works correctly
     *
     * Tests the entire flow:
     * 1. Client sends JSON in request body
     * 2. Controller validates and calls service
     * 3. Service returns saved entity with ID
     * 4. Controller returns 201 Created with location header
     */
    @Test
    void shouldCreateFacility() throws Exception {
        // Arrange - New facility without ID (ID is assigned by database)
        FacilityDTO newFacility = new FacilityDTO(
                null, "FAC003", "Patras Medical Center", "Clinic",
                "Western Greece", "Patras", null, null, true, null, null
        );

        // Saved facility with ID assigned
        FacilityDTO savedFacility = new FacilityDTO(
                3L, "FAC003", "Patras Medical Center", "Clinic",
                "Western Greece", "Patras", null, null, true, null, null
        );

        // Mock: When createFacility is called with any FacilityDTO, return savedFacility
        when(facilityService.createFacility(any(FacilityDTO.class))).thenReturn(savedFacility);

        // Act & Assert - Send POST request with JSON body
        mockMvc.perform(post("/api/facilities")
                        .contentType(MediaType.APPLICATION_JSON) // Tell server we're sending JSON
                        .content(objectMapper.writeValueAsString(newFacility))) // Convert DTO to JSON string
                .andExpect(status().isCreated()) // HTTP 201
                .andExpect(jsonPath("$.id").value(3)) // Check ID was assigned
                .andExpect(jsonPath("$.code").value("FAC003"))
                .andExpect(jsonPath("$.name").value("Patras Medical Center"));

        // Verify that the service method was actually called once
        verify(facilityService, times(1)).createFacility(any(FacilityDTO.class));
    }

    /**
     * Test: PUT /api/facilities/{id}
     * Verifies that updating an existing facility works correctly
     */
    @Test
    void shouldUpdateFacility() throws Exception {
        // Arrange - Facility with updated name
        FacilityDTO updatedFacility = new FacilityDTO(
                1L, "FAC001", "Athens General Hospital - Updated", "Hospital",
                "Attica", "Athens", 37.9838, 23.7275, true, null, null
        );

        // Mock: When updateFacility(1, any DTO) is called, return updated facility
        when(facilityService.updateFacility(eq(1L), any(FacilityDTO.class))).thenReturn(updatedFacility);

        // Act & Assert
        mockMvc.perform(put("/api/facilities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFacility)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Athens General Hospital - Updated"));

        // Verify the update method was called
        verify(facilityService, times(1)).updateFacility(eq(1L), any(FacilityDTO.class));
    }

    /**
     * Test: DELETE /api/facilities/{id}
     * Verifies that deleting a facility returns HTTP 204 No Content
     */
    @Test
    void shouldDeleteFacility() throws Exception {
        // Act & Assert - DELETE doesn't return content, just status code
        mockMvc.perform(delete("/api/facilities/1"))
                .andExpect(status().isNoContent()); // HTTP 204

        // Verify deleteFacility was called with ID 1
        verify(facilityService, times(1)).deleteFacility(1L);
    }

    /**
     * Test: GET /api/facilities with filter parameters
     * Verifies that filtering by region and type works correctly
     */
    @Test
    void shouldFilterFacilitiesByRegionAndType() throws Exception {
        // Arrange - Single facility matching filters
        FacilityDTO facility = new FacilityDTO(
                1L, "FAC001", "Athens General Hospital", "Hospital",
                "Attica", "Athens", null, null, true, null, null
        );

        Page<FacilityDTO> page = new PageImpl<>(Arrays.asList(facility));

        // Mock with specific parameter values
        when(facilityService.searchFacilitiesWithPagination(
                eq("Attica"), eq("Hospital"), any(), any(), any(Pageable.class)
        )).thenReturn(page);

        // Act & Assert - Test with query parameters
        mockMvc.perform(get("/api/facilities")
                        .param("region", "Attica")
                        .param("type", "Hospital")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].region").value("Attica"))
                .andExpect(jsonPath("$.content[0].type").value("Hospital"));
    }

    /**
     * Test: GET /api/facilities with search query
     * Verifies that text search across facility names and codes works
     */
    @Test
    void shouldSearchFacilitiesByText() throws Exception {
        // Arrange - Facilities matching search query "Athens"
        FacilityDTO facility = new FacilityDTO(
                1L, "FAC001", "Athens General Hospital", "Hospital",
                "Attica", "Athens", null, null, true, null, null
        );

        Page<FacilityDTO> page = new PageImpl<>(Arrays.asList(facility));

        when(facilityService.searchFacilitiesWithPagination(
                any(), any(), any(), eq("Athens"), any(Pageable.class)
        )).thenReturn(page);

        // Act & Assert - Test search functionality
        mockMvc.perform(get("/api/facilities")
                        .param("search", "Athens")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Athens General Hospital"));
    }

    /**
     * Test: Verify pagination parameters are handled correctly
     * Tests custom page size and page number
     */
    @Test
    void shouldHandlePaginationParameters() throws Exception {
        // Arrange - Create multiple pages of data
        List<FacilityDTO> facilities = Arrays.asList(
                new FacilityDTO(1L, "FAC001", "Facility 1", "Hospital", "Region 1", "District 1", null, null, true, null, null),
                new FacilityDTO(2L, "FAC002", "Facility 2", "Clinic", "Region 2", "District 2", null, null, true, null, null)
        );

        // Page 1 (second page) with size 2, total 10 facilities
        Page<FacilityDTO> page = new PageImpl<>(facilities, PageRequest.of(1, 2), 10);

        when(facilityService.searchFacilitiesWithPagination(
                any(), any(), any(), any(), any(Pageable.class)
        )).thenReturn(page);

        // Act & Assert - Request page 1 with size 2
        mockMvc.perform(get("/api/facilities")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.totalPages").value(5))
                .andExpect(jsonPath("$.number").value(1)) // Current page number
                .andExpect(jsonPath("$.size").value(2)); // Page size
    }
}
