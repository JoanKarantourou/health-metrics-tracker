/**
 * FacilityList Component
 * Displays a list of all health facilities in a table format
 *
 * Features:
 * - Fetches facilities from API on load
 * - Displays loading state while fetching
 * - Shows error messages if fetch fails
 * - Displays facilities in a formatted table
 * - Provides View/Edit actions for each facility
 */

import React, { useEffect, useState } from "react";
import { facilityService } from "../../services";
import "./FacilityList.css";

/**
 * FacilityList functional component
 */
function FacilityList() {
  // State management
  const [facilities, setFacilities] = useState([]); // List of facilities
  const [loading, setLoading] = useState(true); // Loading indicator
  const [error, setError] = useState(null); // Error message

  /**
   * useEffect Hook - runs when component mounts
   * The empty array [] means "run once when component loads"
   */
  useEffect(() => {
    fetchFacilities();
  }, []); // Empty dependency array = run once on mount

  /**
   * Fetch facilities from the API
   */
  const fetchFacilities = async () => {
    try {
      setLoading(true);
      setError(null);

      // Call the API service
      const response = await facilityService.getAll();

      // Update state with the fetched data
      setFacilities(response.data);

      console.log("✅ Fetched facilities:", response.data.length);
    } catch (err) {
      // Handle errors (similar to try-catch in .NET)
      console.error("❌ Error fetching facilities:", err);
      setError(
        "Failed to load facilities. Please ensure the backend is running.",
      );
    } finally {
      // Always runs after try/catch
      setLoading(false);
    }
  };

  /**
   * Handle View button click
   * @param {number} id - Facility ID
   */
  const handleView = (id) => {
    console.log("View facility:", id);
    // TODO: Navigate to facility detail page
    alert(`View facility ${id} - Coming soon!`);
  };

  /**
   * Handle Edit button click
   * @param {number} id - Facility ID
   */
  const handleEdit = (id) => {
    console.log("Edit facility:", id);
    // TODO: Navigate to facility edit page
    alert(`Edit facility ${id} - Coming soon!`);
  };

  /**
   * Render loading state
   */
  if (loading) {
    return (
      <div className="facility-list-container">
        <div className="loading">
          <div className="spinner"></div>
          <p>Loading facilities...</p>
        </div>
      </div>
    );
  }

  /**
   * Render error state
   */
  if (error) {
    return (
      <div className="facility-list-container">
        <div className="error-message">
          <h3>⚠️ Error</h3>
          <p>{error}</p>
          <button onClick={fetchFacilities} className="btn-retry">
            🔄 Retry
          </button>
        </div>
      </div>
    );
  }

  /**
   * Render main content - the facilities table
   */
  return (
    <div className="facility-list-container">
      {/* Header section */}
      <div className="list-header">
        <h2>Health Facilities</h2>
        <button className="btn-primary" onClick={fetchFacilities}>
          🔄 Refresh
        </button>
      </div>

      {/* Statistics summary */}
      <div className="stats-summary">
        <div className="stat-card">
          <span className="stat-label">Total Facilities</span>
          <span className="stat-value">{facilities.length}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Active</span>
          <span className="stat-value">
            {facilities.filter((f) => f.active).length}
          </span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Inactive</span>
          <span className="stat-value">
            {facilities.filter((f) => !f.active).length}
          </span>
        </div>
      </div>

      {/* Facilities table */}
      <div className="table-container">
        <table className="facilities-table">
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>Type</th>
              <th>Region</th>
              <th>District</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {facilities.length === 0 ? (
              <tr>
                <td colSpan="7" className="no-data">
                  No facilities found
                </td>
              </tr>
            ) : (
              facilities.map((facility) => (
                <tr
                  key={facility.id}
                  className={!facility.active ? "inactive-row" : ""}
                >
                  <td className="code-cell">{facility.code}</td>
                  <td className="name-cell">{facility.name}</td>
                  <td>{facility.type}</td>
                  <td>{facility.region}</td>
                  <td>{facility.district}</td>
                  <td>
                    <span
                      className={`status-badge ${facility.active ? "active" : "inactive"}`}
                    >
                      {facility.active ? "✓ Active" : "✗ Inactive"}
                    </span>
                  </td>
                  <td className="actions-cell">
                    <button
                      className="btn-action btn-view"
                      onClick={() => handleView(facility.id)}
                      title="View details"
                    >
                      👁️ View
                    </button>
                    <button
                      className="btn-action btn-edit"
                      onClick={() => handleEdit(facility.id)}
                      title="Edit facility"
                    >
                      ✏️ Edit
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Footer info */}
      <div className="list-footer">
        <p>Showing {facilities.length} facilities</p>
      </div>
    </div>
  );
}

export default FacilityList;
