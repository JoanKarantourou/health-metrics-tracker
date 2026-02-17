/**
 * Dashboard Component Tests
 * Tests the main dashboard page with statistics and charts
 *
 * Note: recharts components are mocked since they require
 * a real browser environment to render SVG properly
 */

import React from "react";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import Dashboard from "./Dashboard";
import {
  facilityService,
  indicatorService,
  dataValueService,
} from "../services";

// Mock all service modules
jest.mock("../services", () => ({
  facilityService: {
    getAll: jest.fn(),
  },
  indicatorService: {
    getAll: jest.fn(),
  },
  dataValueService: {
    getByIndicator: jest.fn(),
  },
}));

// Mock recharts - charts need a real browser to render SVG
// Without this mock, tests would fail due to missing SVG support
jest.mock("recharts", () => ({
  LineChart: ({ children }) => <div data-testid="line-chart">{children}</div>,
  Line: () => <div />,
  BarChart: ({ children }) => <div data-testid="bar-chart">{children}</div>,
  Bar: () => <div />,
  XAxis: () => <div />,
  YAxis: () => <div />,
  CartesianGrid: () => <div />,
  Tooltip: () => <div />,
  Legend: () => <div />,
  ResponsiveContainer: ({ children }) => (
    <div data-testid="responsive-container">{children}</div>
  ),
}));

// Sample test data
const mockFacilitiesResponse = {
  data: {
    content: [
      { id: 1, code: "FAC001", name: "Athens General Hospital", active: true },
      {
        id: 2,
        code: "FAC002",
        name: "Thessaloniki Health Center",
        active: false,
      },
    ],
  },
};

const mockIndicatorsResponse = {
  data: [
    {
      id: 1,
      code: "IND001",
      name: "Malaria Cases",
      unit: "cases",
      active: true,
    },
    {
      id: 2,
      code: "IND002",
      name: "Vaccination Coverage",
      unit: "%",
      active: true,
    },
  ],
};

const mockDataValuesResponse = {
  data: [
    {
      id: 1,
      value: "50",
      periodStart: "2026-01-01",
      facilityRegion: "Attica",
    },
    {
      id: 2,
      value: "75",
      periodStart: "2026-02-01",
      facilityRegion: "Central Macedonia",
    },
  ],
};

describe("Dashboard Component", () => {
  // Reset mocks before each test
  beforeEach(() => {
    jest.clearAllMocks();
    // Default successful responses
    facilityService.getAll.mockResolvedValue(mockFacilitiesResponse);
    indicatorService.getAll.mockResolvedValue(mockIndicatorsResponse);
    dataValueService.getByIndicator.mockResolvedValue(mockDataValuesResponse);
  });

  // ==================== LOADING STATE ====================

  /**
   * Test: Shows loading state initially
   * Expected: Loading message appears while data is fetching
   */
  test("shows loading state while fetching data", () => {
    // Arrange - Never resolve
    facilityService.getAll.mockReturnValue(new Promise(() => {}));
    indicatorService.getAll.mockReturnValue(new Promise(() => {}));

    // Act
    render(<Dashboard />);

    // Assert
    expect(screen.getByText(/loading dashboard/i)).toBeInTheDocument();
  });

  // ==================== ERROR STATE ====================

  /**
   * Test: Shows error message when API fails
   * Expected: Error message and retry button appear
   */
  test("shows error state when data fetch fails", async () => {
    // Arrange
    facilityService.getAll.mockRejectedValue(new Error("Network error"));

    // Act
    render(<Dashboard />);

    // Assert
    await waitFor(() => {
      expect(
        screen.getByText(/failed to load dashboard data/i),
      ).toBeInTheDocument();
    });

    expect(screen.getByText(/retry/i)).toBeInTheDocument();
  });

  // ==================== STATISTICS CARDS ====================

  /**
   * Test: Renders all statistics cards
   * Expected: All 4 stat cards appear with labels
   */
  test("renders all statistics cards", async () => {
    // Act
    render(<Dashboard />);

    // Assert
    await waitFor(() => {
      expect(screen.getByText("Total Facilities")).toBeInTheDocument();
    });

    expect(screen.getByText("Active Facilities")).toBeInTheDocument();
    expect(screen.getByText("Health Indicators")).toBeInTheDocument();
    expect(screen.getByText("Data Points (Selected)")).toBeInTheDocument();
  });

  /**
   * Test: Shows correct total facilities count
   * Expected: Shows 2 total facilities from mock data
   */
  test("displays correct total facilities count", async () => {
    // Act
    render(<Dashboard />);

    // Assert - 2 facilities total in mock data
    await waitFor(() => {
      expect(screen.getByText("Total Facilities")).toBeInTheDocument();
    });

    // The stat value "2" should appear for total facilities
    const statValues = screen.getAllByText("2");
    expect(statValues.length).toBeGreaterThan(0);
  });

  /**
   * Test: Shows correct active facilities count
   * Expected: Shows 1 active facility (only FAC001 is active)
   */
  test("displays correct active facilities count", async () => {
    // Act
    render(<Dashboard />);

    // Assert - 1 active facility in mock data (FAC002 is inactive)
    await waitFor(() => {
      expect(screen.getByText("Active Facilities")).toBeInTheDocument();
    });

    const statValues = screen.getAllByText("1");
    expect(statValues.length).toBeGreaterThan(0);
  });

  // ==================== INDICATOR SELECTOR ====================

  /**
   * Test: Renders indicator selection dropdown
   * Expected: Dropdown appears with indicator options
   */
  test("renders indicator selection dropdown", async () => {
    // Act
    render(<Dashboard />);

    // Assert
    await waitFor(() => {
      expect(
        screen.getByLabelText(/select health indicator/i),
      ).toBeInTheDocument();
    });
  });

  /**
   * Test: Indicator dropdown contains active indicators
   * Expected: Active indicator names appear as options
   */
  test("populates indicator dropdown with active indicators", async () => {
    // Act
    render(<Dashboard />);

    // Assert - use getAllByText since indicator names appear in both dropdown and chart subtitle
    await waitFor(() => {
      expect(screen.getAllByText(/malaria cases/i).length).toBeGreaterThan(0);
    });

    expect(screen.getAllByText(/vaccination coverage/i).length).toBeGreaterThan(0);
  });

  /**
   * Test: Changing indicator triggers new data fetch
   * Expected: getByIndicator is called when selection changes
   */
  test("fetches new data when indicator selection changes", async () => {
    // Act
    render(<Dashboard />);

    await waitFor(() => {
      expect(
        screen.getByLabelText(/select health indicator/i),
      ).toBeInTheDocument();
    });

    // Change indicator selection
    const select = screen.getByLabelText(/select health indicator/i);
    fireEvent.change(select, { target: { value: "2" } });

    // Assert - service should be called with new indicator ID
    await waitFor(() => {
      expect(dataValueService.getByIndicator).toHaveBeenCalledWith(2);
    });
  });

  // ==================== CHARTS ====================

  /**
   * Test: Renders chart containers
   * Expected: Chart containers appear in the dashboard
   */
  test("renders chart containers", async () => {
    // Act
    render(<Dashboard />);

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/trend over time/i)).toBeInTheDocument();
    });

    expect(screen.getByText(/regional comparison/i)).toBeInTheDocument();
  });

  /**
   * Test: Renders line chart and bar chart
   * Expected: Both mocked chart components appear
   */
  test("renders line chart and bar chart", async () => {
    // Act
    render(<Dashboard />);

    // Assert - Our mocked recharts components render test IDs
    await waitFor(() => {
      expect(screen.getByTestId("line-chart")).toBeInTheDocument();
    });

    expect(screen.getByTestId("bar-chart")).toBeInTheDocument();
  });

  // ==================== DASHBOARD HEADER ====================

  /**
   * Test: Renders dashboard header
   * Expected: Dashboard title and subtitle appear
   */
  test("renders dashboard header", async () => {
    // Act
    render(<Dashboard />);

    // Assert - wait for loading to complete so the header is visible
    await waitFor(() => {
      expect(screen.getByText(/overview of health metrics/i)).toBeInTheDocument();
    });

    expect(screen.getByText(/dashboard/i)).toBeInTheDocument();
  });

  // ==================== QUICK TIPS ====================

  /**
   * Test: Renders quick tips section
   * Expected: Tips section with helpful info appears
   */
  test("renders quick tips section", async () => {
    // Act
    render(<Dashboard />);

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/quick tips/i)).toBeInTheDocument();
    });
  });

  // ==================== RETRY FUNCTIONALITY ====================

  /**
   * Test: Retry button refetches data after error
   * Expected: Clicking retry calls the API again
   */
  test("retries data fetch when retry button is clicked", async () => {
    // Arrange - First call fails, second succeeds
    facilityService.getAll
      .mockRejectedValueOnce(new Error("Network error"))
      .mockResolvedValueOnce(mockFacilitiesResponse);

    // Act
    render(<Dashboard />);

    // Wait for error state
    await waitFor(() => {
      expect(screen.getByText(/retry/i)).toBeInTheDocument();
    });

    // Click retry
    fireEvent.click(screen.getByText(/retry/i));

    // Assert - API should be called again
    await waitFor(() => {
      expect(facilityService.getAll).toHaveBeenCalledTimes(2);
    });
  });
});
