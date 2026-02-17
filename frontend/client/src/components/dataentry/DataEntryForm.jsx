/**
 * DataEntryForm Component
 * Form for submitting new health metrics data values
 *
 * Features:
 * - Fetches facilities and indicators on load
 * - Validates form inputs before submission
 * - Submits data to backend API
 * - Shows success/error messages
 * - Resets form after successful submission
 */

import React, { useEffect, useState } from "react";
import {
  facilityService,
  indicatorService,
  dataValueService,
} from "../../services";
import "./DataEntryForm.css";

function DataEntryForm() {
  // State for dropdown options
  const [facilities, setFacilities] = useState([]);
  const [indicators, setIndicators] = useState([]);

  // State for form inputs
  const [formData, setFormData] = useState({
    facilityId: "",
    indicatorId: "",
    periodStart: "",
    periodEnd: "",
    periodType: "MONTHLY",
    value: "",
    comment: "",
  });

  // State for UI feedback
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [validationErrors, setValidationErrors] = useState({});

  /**
   * Load facilities and indicators when component mounts
   * Similar to OnInitializedAsync in Blazor
   */
  useEffect(() => {
    fetchDropdownData();
  }, []);

  /**
   * Fetch facilities and indicators for dropdowns
   */
  const fetchDropdownData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Fetch both in parallel (like Task.WhenAll in C#)
      const [facilitiesResponse, indicatorsResponse] = await Promise.all([
        facilityService.getAll(),
        indicatorService.getAll(),
      ]);

      // facilityService returns a Page object with .content array
      const facilitiesData =
        facilitiesResponse.data.content || facilitiesResponse.data;
      const indicatorsData = indicatorsResponse.data;

      // Filter only active facilities and indicators
      setFacilities(facilitiesData.filter((f) => f.active));
      setIndicators(indicatorsData.filter((i) => i.active));

      console.log("✅ Loaded dropdown data");
    } catch (err) {
      console.error("❌ Error loading dropdown data:", err);
      setError(
        "Failed to load form data. Please ensure the backend is running.",
      );
    } finally {
      setLoading(false);
    }
  };

  /**
   * Handle input changes
   * Updates form state when user types or selects options
   */
  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // Clear validation error for this field when user starts typing
    if (validationErrors[name]) {
      setValidationErrors((prev) => ({
        ...prev,
        [name]: null,
      }));
    }
  };

  /**
   * Validate form before submission
   * Returns true if valid, false otherwise
   */
  const validateForm = () => {
    const errors = {};

    if (!formData.facilityId) {
      errors.facilityId = "Please select a facility";
    }

    if (!formData.indicatorId) {
      errors.indicatorId = "Please select an indicator";
    }

    if (!formData.periodStart) {
      errors.periodStart = "Please select a start date";
    }

    if (!formData.periodEnd) {
      errors.periodEnd = "Please select an end date";
    }

    if (!formData.value) {
      errors.value = "Please enter a value";
    } else if (isNaN(formData.value) || Number(formData.value) < 0) {
      errors.value = "Please enter a valid positive number";
    }

    // Check if end date is after start date
    if (formData.periodStart && formData.periodEnd) {
      if (new Date(formData.periodEnd) < new Date(formData.periodStart)) {
        errors.periodEnd = "End date must be after start date";
      }
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  /**
   * Handle form submission
   */
  const handleSubmit = async (e) => {
    e.preventDefault(); // Prevent page reload

    // Validate form
    if (!validateForm()) {
      setError("Please fix the errors before submitting");
      return;
    }

    try {
      setSubmitting(true);
      setError(null);
      setSuccess(false);

      // Prepare data for backend
      const submitData = {
        facilityId: Number(formData.facilityId),
        indicatorId: Number(formData.indicatorId),
        periodStart: formData.periodStart,
        periodEnd: formData.periodEnd,
        periodType: formData.periodType,
        value: Number(formData.value),
        comment: formData.comment || null,
      };

      // Submit to backend
      await dataValueService.submit(submitData);

      console.log("✅ Data submitted successfully");

      // Show success message
      setSuccess(true);

      // Reset form
      setFormData({
        facilityId: "",
        indicatorId: "",
        periodStart: "",
        periodEnd: "",
        periodType: "MONTHLY",
        value: "",
        comment: "",
      });

      // Hide success message after 5 seconds
      setTimeout(() => setSuccess(false), 5000);
    } catch (err) {
      console.error("❌ Error submitting data:", err);

      // Handle specific error messages from backend
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError("Failed to submit data. Please try again.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * Reset form to initial state
   */
  const handleReset = () => {
    setFormData({
      facilityId: "",
      indicatorId: "",
      periodStart: "",
      periodEnd: "",
      periodType: "MONTHLY",
      value: "",
      comment: "",
    });
    setValidationErrors({});
    setError(null);
    setSuccess(false);
  };

  /**
   * Render loading state
   */
  if (loading) {
    return (
      <div className="data-entry-container">
        <div className="loading">
          <div className="spinner"></div>
          <p>Loading form data...</p>
        </div>
      </div>
    );
  }

  /**
   * Render main form
   */
  return (
    <div className="data-entry-container">
      <div className="form-header">
        <h2>📊 Data Entry</h2>
        <p className="form-subtitle">
          Submit health metrics data for facilities
        </p>
      </div>

      {/* Success message */}
      {success && (
        <div className="alert alert-success">
          <strong>✅ Success!</strong> Data submitted successfully.
        </div>
      )}

      {/* Error message */}
      {error && (
        <div className="alert alert-error">
          <strong>⚠️ Error:</strong> {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="data-entry-form">
        {/* Facility Selection */}
        <div className="form-group">
          <label htmlFor="facilityId">
            Facility <span className="required">*</span>
          </label>
          <select
            id="facilityId"
            name="facilityId"
            value={formData.facilityId}
            onChange={handleChange}
            className={validationErrors.facilityId ? "error" : ""}
          >
            <option value="">-- Select Facility --</option>
            {facilities.map((facility) => (
              <option key={facility.id} value={facility.id}>
                {facility.code} - {facility.name}
              </option>
            ))}
          </select>
          {validationErrors.facilityId && (
            <span className="error-text">{validationErrors.facilityId}</span>
          )}
        </div>

        {/* Indicator Selection */}
        <div className="form-group">
          <label htmlFor="indicatorId">
            Health Indicator <span className="required">*</span>
          </label>
          <select
            id="indicatorId"
            name="indicatorId"
            value={formData.indicatorId}
            onChange={handleChange}
            className={validationErrors.indicatorId ? "error" : ""}
          >
            <option value="">-- Select Indicator --</option>
            {indicators.map((indicator) => (
              <option key={indicator.id} value={indicator.id}>
                {indicator.name} ({indicator.unit})
              </option>
            ))}
          </select>
          {validationErrors.indicatorId && (
            <span className="error-text">{validationErrors.indicatorId}</span>
          )}
        </div>

        {/* Period Type */}
        <div className="form-group">
          <label htmlFor="periodType">
            Period Type <span className="required">*</span>
          </label>
          <select
            id="periodType"
            name="periodType"
            value={formData.periodType}
            onChange={handleChange}
          >
            <option value="DAILY">Daily</option>
            <option value="WEEKLY">Weekly</option>
            <option value="MONTHLY">Monthly</option>
            <option value="QUARTERLY">Quarterly</option>
            <option value="YEARLY">Yearly</option>
          </select>
        </div>

        {/* Date Range */}
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="periodStart">
              Start Date <span className="required">*</span>
            </label>
            <input
              type="date"
              id="periodStart"
              name="periodStart"
              value={formData.periodStart}
              onChange={handleChange}
              className={validationErrors.periodStart ? "error" : ""}
            />
            {validationErrors.periodStart && (
              <span className="error-text">{validationErrors.periodStart}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="periodEnd">
              End Date <span className="required">*</span>
            </label>
            <input
              type="date"
              id="periodEnd"
              name="periodEnd"
              value={formData.periodEnd}
              onChange={handleChange}
              className={validationErrors.periodEnd ? "error" : ""}
            />
            {validationErrors.periodEnd && (
              <span className="error-text">{validationErrors.periodEnd}</span>
            )}
          </div>
        </div>

        {/* Value Input */}
        <div className="form-group">
          <label htmlFor="value">
            Value <span className="required">*</span>
          </label>
          <input
            type="number"
            id="value"
            name="value"
            value={formData.value}
            onChange={handleChange}
            placeholder="Enter numeric value"
            step="0.01"
            min="0"
            className={validationErrors.value ? "error" : ""}
          />
          {validationErrors.value && (
            <span className="error-text">{validationErrors.value}</span>
          )}
        </div>

        {/* Comment (Optional) */}
        <div className="form-group">
          <label htmlFor="comment">Comment (Optional)</label>
          <textarea
            id="comment"
            name="comment"
            value={formData.comment}
            onChange={handleChange}
            placeholder="Add any additional notes..."
            rows="3"
            maxLength="500"
          />
          <span className="char-count">{formData.comment.length}/500</span>
        </div>

        {/* Form Actions */}
        <div className="form-actions">
          <button type="submit" className="btn-submit" disabled={submitting}>
            {submitting ? "⏳ Submitting..." : "✅ Submit Data"}
          </button>
          <button
            type="button"
            className="btn-reset"
            onClick={handleReset}
            disabled={submitting}
          >
            🔄 Reset Form
          </button>
        </div>
      </form>
    </div>
  );
}

export default DataEntryForm;
