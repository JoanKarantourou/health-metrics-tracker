/**
 * DataEntryForm Component Tests
 * Tests the health data submission form
 *
 * Tests cover: loading state, form rendering,
 * validation, and submission behavior
 */

import React from "react";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import DataEntryForm from "./DataEntryForm";
import {
  facilityService,
  indicatorService,
  dataValueService,
} from "../../services";

// Mock all service modules
jest.mock("../../services", () => ({
  facilityService: {
    getAll: jest.fn(),
  },
  indicatorService: {
    getAll: jest.fn(),
  },
  dataValueService: {
    submit: jest.fn(),
  },
}));

// Sample test data
const mockFacilitiesResponse = {
  data: {
    content: [
      {
        id: 1,
        code: "FAC001",
        name: "Athens General Hospital",
        active: true,
      },
      {
        id: 2,
        code: "FAC002",
        name: "Thessaloniki Health Center",
        active: true,
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

describe("DataEntryForm Component", () => {
  // Reset mocks before each test
  beforeEach(() => {
    jest.clearAllMocks();
    // Default: mock successful responses for both services
    facilityService.getAll.mockResolvedValue(mockFacilitiesResponse);
    indicatorService.getAll.mockResolvedValue(mockIndicatorsResponse);
  });

  // ==================== LOADING STATE ====================

  /**
   * Test: Shows loading state initially
   * Expected: Loading message appears while dropdown data is fetching
   */
  test("shows loading state while fetching form data", () => {
    // Arrange - Never resolve so we stay in loading state
    facilityService.getAll.mockReturnValue(new Promise(() => {}));
    indicatorService.getAll.mockReturnValue(new Promise(() => {}));

    // Act
    render(<DataEntryForm />);

    // Assert
    expect(screen.getByText(/loading form data/i)).toBeInTheDocument();
  });

  // ==================== FORM RENDERING ====================

  /**
   * Test: Form renders with all required fields
   * Expected: All form fields are visible after loading
   */
  test("renders all form fields after loading", async () => {
    // Act
    render(<DataEntryForm />);

    // Assert - Wait for loading to complete
    await waitFor(() => {
      expect(screen.getByLabelText(/facility/i)).toBeInTheDocument();
    });

    expect(screen.getByLabelText(/health indicator/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/period type/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/start date/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/end date/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/value/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/comment/i)).toBeInTheDocument();
  });

  /**
   * Test: Facilities are loaded into dropdown
   * Expected: Facility names appear as options
   */
  test("loads facilities into dropdown", async () => {
    // Act
    render(<DataEntryForm />);

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/athens general hospital/i)).toBeInTheDocument();
    });

    expect(screen.getByText(/thessaloniki health center/i)).toBeInTheDocument();
  });

  /**
   * Test: Indicators are loaded into dropdown
   * Expected: Indicator names appear as options
   */
  test("loads indicators into dropdown", async () => {
    // Act
    render(<DataEntryForm />);

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/malaria cases/i)).toBeInTheDocument();
    });

    expect(screen.getByText(/vaccination coverage/i)).toBeInTheDocument();
  });

  /**
   * Test: Submit and Reset buttons are rendered
   * Expected: Both action buttons appear in the form
   */
  test("renders submit and reset buttons", async () => {
    // Act
    render(<DataEntryForm />);

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/submit data/i)).toBeInTheDocument();
    });

    expect(screen.getByText(/reset form/i)).toBeInTheDocument();
  });

  // ==================== ERROR STATE ====================

  /**
   * Test: Shows error when API fails to load form data
   * Expected: Error message appears when services fail
   */
  test("shows error when form data fails to load", async () => {
    // Arrange
    facilityService.getAll.mockRejectedValue(new Error("Network error"));

    // Act
    render(<DataEntryForm />);

    // Assert
    await waitFor(() => {
      expect(screen.getByText(/failed to load form data/i)).toBeInTheDocument();
    });
  });

  // ==================== FORM VALIDATION ====================

  /**
   * Test: Shows validation errors when submitting empty form
   * Expected: Required field error messages appear
   */
  test("shows validation errors when submitting empty form", async () => {
    // Act
    render(<DataEntryForm />);

    await waitFor(() => {
      expect(screen.getByText(/submit data/i)).toBeInTheDocument();
    });

    // Click submit without filling the form
    const submitButton = screen.getByText(/submit data/i);
    fireEvent.click(submitButton);

    // Assert - validation errors should appear
    await waitFor(() => {
      expect(screen.getByText(/please select a facility/i)).toBeInTheDocument();
    });

    expect(screen.getByText(/please select an indicator/i)).toBeInTheDocument();
    expect(screen.getByText(/please enter a value/i)).toBeInTheDocument();
  });

  /**
   * Test: Shows validation error for negative value
   * Expected: Error message appears when value is negative
   */
  test("shows validation error for negative value", async () => {
    // Act
    render(<DataEntryForm />);

    await waitFor(() => {
      expect(screen.getByLabelText(/^value/i)).toBeInTheDocument();
    });

    // Enter a negative value
    const valueInput = screen.getByLabelText(/^value/i);
    fireEvent.change(valueInput, { target: { value: "-5" } });

    // Submit the form
    const submitButton = screen.getByText(/submit data/i);
    fireEvent.click(submitButton);

    // Assert
    await waitFor(() => {
      expect(
        screen.getByText(/please enter a valid positive number/i),
      ).toBeInTheDocument();
    });
  });

  // ==================== FORM INTERACTIONS ====================

  /**
   * Test: Form fields update when user interacts with them
   * Expected: Input values reflect user input
   */
  test("updates value input when user types", async () => {
    // Act
    render(<DataEntryForm />);

    await waitFor(() => {
      expect(screen.getByLabelText(/^value/i)).toBeInTheDocument();
    });

    // Type in value field
    const valueInput = screen.getByLabelText(/^value/i);
    fireEvent.change(valueInput, { target: { value: "150" } });

    // Assert
    expect(valueInput.value).toBe("150");
  });

  /**
   * Test: Reset button clears form fields
   * Expected: Form returns to initial state after reset
   */
  test("resets form when reset button is clicked", async () => {
    // Act
    render(<DataEntryForm />);

    await waitFor(() => {
      expect(screen.getByLabelText(/^value/i)).toBeInTheDocument();
    });

    // Type something in value field
    const valueInput = screen.getByLabelText(/^value/i);
    fireEvent.change(valueInput, { target: { value: "150" } });
    expect(valueInput.value).toBe("150");

    // Click reset
    const resetButton = screen.getByText(/reset form/i);
    fireEvent.click(resetButton);

    // Assert - value should be cleared
    expect(valueInput.value).toBe("");
  });

  /**
   * Test: Period type dropdown has correct options
   * Expected: All period types are available
   */
  test("renders all period type options", async () => {
    // Act
    render(<DataEntryForm />);

    await waitFor(() => {
      expect(screen.getByLabelText(/period type/i)).toBeInTheDocument();
    });

    // Assert all options exist
    expect(screen.getByText("Daily")).toBeInTheDocument();
    expect(screen.getByText("Weekly")).toBeInTheDocument();
    expect(screen.getByText("Monthly")).toBeInTheDocument();
    expect(screen.getByText("Quarterly")).toBeInTheDocument();
    expect(screen.getByText("Yearly")).toBeInTheDocument();
  });

  // ==================== FORM SUBMISSION ====================

  /**
   * Test: Successful submission shows success message
   * Expected: Success message appears after valid submission
   */
  test("shows success message after successful submission", async () => {
    // Arrange
    dataValueService.submit.mockResolvedValue({ data: { id: 1 } });

    // Act
    render(<DataEntryForm />);

    await waitFor(() => {
      expect(screen.getByLabelText(/facility/i)).toBeInTheDocument();
    });

    // Fill in required fields
    fireEvent.change(screen.getByLabelText(/facility/i), {
      target: { value: "1" },
    });
    fireEvent.change(screen.getByLabelText(/health indicator/i), {
      target: { value: "1" },
    });
    fireEvent.change(screen.getByLabelText(/start date/i), {
      target: { value: "2026-01-01" },
    });
    fireEvent.change(screen.getByLabelText(/end date/i), {
      target: { value: "2026-01-31" },
    });
    fireEvent.change(screen.getByLabelText(/^value/i), {
      target: { value: "150" },
    });

    // Submit
    fireEvent.click(screen.getByText(/submit data/i));

    // Assert - use getAllByText since "Success" appears in both the div and strong elements
    await waitFor(() => {
      expect(screen.getAllByText(/success/i).length).toBeGreaterThan(0);
    });
  });
});
