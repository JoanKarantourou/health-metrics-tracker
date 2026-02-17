/**
 * FacilityList Component Tests
 * Tests the facility list with filtering, searching and pagination
 * We mock the API service and test component behavior
 */

import React from "react";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import FacilityList from "./FacilityList";
import { facilityService } from "../../services/api";

// Mock the facility service module
// Similar to Mock<IFacilityService> in Moq
jest.mock("../../services/api", () => ({
  facilityService: {
    getAll: jest.fn(),
  },
}));

// Sample test data
const mockFacilitiesPage = {
  data: {
    content: [
      {
        id: 1,
        code: "FAC001",
        name: "Athens General Hospital",
        type: "Hospital",
        region: "Attica",
        district: "Athens",
        active: true,
      },
      {
        id: 2,
        code: "FAC002",
        name: "Thessaloniki Health Center",
        type: "Health Center",
        region: "Central Macedonia",
        district: "Thessaloniki",
        active: true,
      },
    ],
    totalPages: 1,
    totalElements: 2,
    number: 0,
    size: 10,
  },
};

const mockEmptyPage = {
  data: {
    content: [],
    totalPages: 0,
    totalElements: 0,
    number: 0,
    size: 10,
  },
};

describe("FacilityList Component", () => {
  // Reset mocks before each test
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ==================== LOADING STATE ====================

  /**
   * Test: Shows loading state initially
   * Expected: Loading message appears while data is being fetched
   */
  test("shows loading state while fetching facilities", () => {
    // Arrange - Make API call never resolve so we stay in loading state
    facilityService.getAll.mockReturnValue(new Promise(() => {}));

    // Act
    render(<FacilityList />);

    // Assert
    expect(screen.getByText(/loading facilities/i)).toBeInTheDocument();
  });

  // ==================== DATA DISPLAY ====================

  /**
   * Test: Renders facility list after successful API call
   * Expected: Facility names and codes appear in the table
   */
  test("renders facilities after successful fetch", async () => {
    // Arrange - Mock successful API response
    facilityService.getAll.mockResolvedValue(mockFacilitiesPage);

    // Act
    render(<FacilityList />);

    // Assert - Wait for data to load and appear
    await waitFor(() => {
      expect(screen.getByText("Athens General Hospital")).toBeInTheDocument();
    });

    expect(screen.getByText("FAC001")).toBeInTheDocument();
    expect(screen.getByText("Thessaloniki Health Center")).toBeInTheDocument();
    expect(screen.getByText("FAC002")).toBeInTheDocument();
  });

  /**
   * Test: Shows correct facility details in table
   * Expected: Type, region, district and status are displayed
   */
  test("displays correct facility details in table", async () => {
    // Arrange
    facilityService.getAll.mockResolvedValue(mockFacilitiesPage);

    // Act
    render(<FacilityList />);

    // Assert - use getAllByText since values may appear in both table cells and filter dropdowns
    await waitFor(() => {
      expect(screen.getAllByText("Hospital").length).toBeGreaterThan(0);
    });

    expect(screen.getAllByText("Attica").length).toBeGreaterThan(0);
    expect(screen.getByText("Athens")).toBeInTheDocument();
  });

  /**
   * Test: Shows active status badge correctly
   * Expected: Active facilities show "Active" badge
   */
  test("shows active status badge for active facilities", async () => {
    // Arrange
    facilityService.getAll.mockResolvedValue(mockFacilitiesPage);

    // Act
    render(<FacilityList />);

    // Assert
    await waitFor(() => {
      const activeBadges = screen.getAllByText("Active");
      expect(activeBadges.length).toBeGreaterThan(0);
    });
  });

  // ==================== EMPTY STATE ====================

  /**
   * Test: Shows empty state when no facilities match filters
   * Expected: "No facilities found" message appears
   */
  test("shows empty state when no facilities found", async () => {
    // Arrange - Mock empty response
    facilityService.getAll.mockResolvedValue(mockEmptyPage);

    // Act
    render(<FacilityList />);

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/no facilities found/i)).toBeInTheDocument();
    });
  });

  // ==================== ERROR STATE ====================

  /**
   * Test: Shows error message when API call fails
   * Expected: Error message appears when fetch fails
   */
  test("shows error message when fetch fails", async () => {
    // Arrange - Mock failed API call
    facilityService.getAll.mockRejectedValue(new Error("Network error"));

    // Act
    render(<FacilityList />);

    // Assert
    await waitFor(() => {
      expect(
        screen.getByText(/failed to load facilities/i),
      ).toBeInTheDocument();
    });
  });

  // ==================== SEARCH FUNCTIONALITY ====================

  /**
   * Test: Search input is rendered
   * Expected: Search box is visible
   */
  test("renders search input", async () => {
    // Arrange
    facilityService.getAll.mockResolvedValue(mockFacilitiesPage);

    // Act
    render(<FacilityList />);

    // Assert
    await waitFor(() => {
      expect(
        screen.getByPlaceholderText(/search by name or code/i),
      ).toBeInTheDocument();
    });
  });

  /**
   * Test: Typing in search box updates the input value
   * Expected: Search input reflects what user types
   */
  test("updates search input when user types", async () => {
    // Arrange
    facilityService.getAll.mockResolvedValue(mockFacilitiesPage);

    // Act
    render(<FacilityList />);

    await waitFor(() => {
      expect(
        screen.getByPlaceholderText(/search by name or code/i),
      ).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/search by name or code/i);
    fireEvent.change(searchInput, { target: { value: "Athens" } });

    // Assert
    expect(searchInput.value).toBe("Athens");
  });

  // ==================== FILTER FUNCTIONALITY ====================

  /**
   * Test: Filter dropdowns are rendered
   * Expected: Region, Type and Status dropdowns are visible
   */
  test("renders filter dropdowns", async () => {
    // Arrange
    facilityService.getAll.mockResolvedValue(mockFacilitiesPage);

    // Act
    render(<FacilityList />);

    // Assert
    await waitFor(() => {
      expect(screen.getByLabelText(/region/i)).toBeInTheDocument();
    });

    expect(screen.getByLabelText(/type/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/status/i)).toBeInTheDocument();
  });

  /**
   * Test: Clear filters button appears when filters are active
   * Expected: Clear button shows after typing in search
   */
  test("shows clear filters button when filters are active", async () => {
    // Arrange
    facilityService.getAll.mockResolvedValue(mockFacilitiesPage);

    // Act
    render(<FacilityList />);

    await waitFor(() => {
      expect(
        screen.getByPlaceholderText(/search by name or code/i),
      ).toBeInTheDocument();
    });

    // Type in search box to activate filter
    const searchInput = screen.getByPlaceholderText(/search by name or code/i);
    fireEvent.change(searchInput, { target: { value: "Athens" } });

    // Assert
    expect(screen.getByText(/clear filters/i)).toBeInTheDocument();
  });

  /**
   * Test: Clear filters button resets search input
   * Expected: Clicking clear filters empties the search box
   */
  test("clears filters when clear button is clicked", async () => {
    // Arrange
    facilityService.getAll.mockResolvedValue(mockFacilitiesPage);

    // Act
    render(<FacilityList />);

    await waitFor(() => {
      expect(
        screen.getByPlaceholderText(/search by name or code/i),
      ).toBeInTheDocument();
    });

    // Type in search to activate filter
    const searchInput = screen.getByPlaceholderText(/search by name or code/i);
    fireEvent.change(searchInput, { target: { value: "Athens" } });

    // Click clear filters
    const clearButton = screen.getByText(/clear filters/i);
    fireEvent.click(clearButton);

    // Assert - search input should be empty
    expect(searchInput.value).toBe("");
  });

  // ==================== TABLE STRUCTURE ====================

  /**
   * Test: Table headers are rendered correctly
   * Expected: All column headers appear in the table
   */
  test("renders table with correct headers", async () => {
    // Arrange
    facilityService.getAll.mockResolvedValue(mockFacilitiesPage);

    // Act
    render(<FacilityList />);

    // Assert
    await waitFor(() => {
      expect(screen.getByText("Code")).toBeInTheDocument();
    });

    expect(screen.getByText("Name")).toBeInTheDocument();
    expect(screen.getByText("Type")).toBeInTheDocument();
    expect(screen.getByText("Region")).toBeInTheDocument();
    expect(screen.getByText("District")).toBeInTheDocument();
    expect(screen.getByText("Status")).toBeInTheDocument();
    expect(screen.getByText("Actions")).toBeInTheDocument();
  });

  /**
   * Test: Action buttons are rendered for each facility
   * Expected: View and Edit buttons appear for each row
   */
  test("renders view and edit buttons for each facility", async () => {
    // Arrange
    facilityService.getAll.mockResolvedValue(mockFacilitiesPage);

    // Act
    render(<FacilityList />);

    // Assert
    await waitFor(() => {
      const viewButtons = screen.getAllByText("View");
      const editButtons = screen.getAllByText("Edit");
      expect(viewButtons).toHaveLength(2);
      expect(editButtons).toHaveLength(2);
    });
  });

  // ==================== RESULTS INFO ====================

  /**
   * Test: Results count is displayed
   * Expected: Shows "Showing X - Y of Z facilities"
   */
  test("displays results count information", async () => {
    // Arrange
    facilityService.getAll.mockResolvedValue(mockFacilitiesPage);

    // Act
    render(<FacilityList />);

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/of 2/i)).toBeInTheDocument();
    });
  });
});
